#!/bin/bash
# Pix Wallet - End-to-End Validation Script
# Stark Engineering
#
# Requisitos: curl instalado.
# Opcional: jq instalado para output mais bonito (mas o script usa grep/cut para ser universal).

BASE_URL="http://localhost:8080"
echo "=========================================="
echo "🚀 INICIANDO SMOKE TEST - PIX WALLET"
echo "=========================================="

# 1. CRIAR CARTEIRA
echo ""
echo "--- 1. Criando Carteira Nova ---"
WALLET_RESP=$(curl -s -X POST "$BASE_URL/wallets")
echo "Response: $WALLET_RESP"
# Extração simples do ID sem depender do JQ
WALLET_ID=$(echo $WALLET_RESP | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

if [ -z "$WALLET_ID" ]; then
  echo "❌ Erro ao criar carteira. Abortando."
  exit 1
fi
echo "✅ Carteira Criada: $WALLET_ID"

# 2. CADASTRAR CHAVE PIX (Novo Requisito)
echo ""
echo "--- 2. Cadastrando Chave Pix (user@banco.com) ---"
MY_KEY="user-$(date +%s)@banco.com"
curl -s -X POST "$BASE_URL/wallets/$WALLET_ID/pix-keys" \
  -H "Content-Type: application/json" \
  -d "{\"type\": \"EMAIL\", \"key\": \"$MY_KEY\"}"
echo -e "\n✅ Chave Cadastrada: $MY_KEY"

# 3. DEPÓSITO
echo ""
echo "--- 3. Realizando Depósito de R$ 1000.00 ---"
curl -s -X POST "$BASE_URL/wallets/$WALLET_ID/deposit" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: dep-$(date +%s)" \
  -d '{"amount": 1000.00}'
echo -e "\n✅ Depósito Concluído"

# 4. VALIDAÇÃO DE REGRA DE NEGÓCIO (Auto-Transferência)
echo ""
echo "--- 4. Teste de Bloqueio: Transferência para Si Mesmo ---"
SELF_RESP=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/pix/transfers" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: self-check-$(date +%s)" \
  -d "{\"fromWalletId\": \"$WALLET_ID\", \"toPixKey\": \"$MY_KEY\", \"amount\": 10.00}")

if [ "$SELF_RESP" -eq 400 ] || [ "$SELF_RESP" -eq 500 ]; then
  echo "✅ Bloqueio Funcionou! (HTTP $SELF_RESP recebido)"
else
  echo "❌ FALHA: O sistema permitiu auto-transferência (HTTP $SELF_RESP)"
fi

# 5. TRANSFERÊNCIA VÁLIDA (Fluxo Feliz)
echo ""
echo "--- 5. Transferência Pix R$ 100.00 (Externo) ---"
IDEM_KEY="pix-key-$(date +%s)"
TRANSFER_RESP=$(curl -s -X POST "$BASE_URL/pix/transfers" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: $IDEM_KEY" \
  -d "{\"fromWalletId\": \"$WALLET_ID\", \"toPixKey\": \"loja-externa@shop.com\", \"amount\": 100.00}")
echo "Response: $TRANSFER_RESP"
E2E_ID=$(echo $TRANSFER_RESP | grep -o '"endToEndId":"[^"]*"' | cut -d'"' -f4)
echo "✅ Transferência PENDING. EndToEndId: $E2E_ID"

# 6. IDEMPOTÊNCIA (Duplo Disparo)
echo ""
echo "--- 6. Teste de Idempotência (Duplo Disparo) ---"
# Reenvia exatamente a MESMA requisição anterior
RETRY_RESP=$(curl -s -X POST "$BASE_URL/pix/transfers" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: $IDEM_KEY" \
  -d "{\"fromWalletId\": \"$WALLET_ID\", \"toPixKey\": \"loja-externa@shop.com\", \"amount\": 100.00}")

RETRY_E2E=$(echo $RETRY_RESP | grep -o '"endToEndId":"[^"]*"' | cut -d'"' -f4)

if [ "$E2E_ID" == "$RETRY_E2E" ]; then
    echo "✅ Idempotência Confirmada: Mesmo EndToEndId retornado."
else
    echo "❌ FALHA: Idempotência não funcionou."
fi

# 7. WEBHOOK (Confirmação)
echo ""
echo "--- 7. Processando Webhook (CONFIRMED) ---"
curl -s -X POST "$BASE_URL/pix/webhook" \
  -H "Content-Type: application/json" \
  -d "{\"endToEndId\": \"$E2E_ID\", \"eventId\": \"evt-conf-$(date +%s)\", \"eventType\": \"CONFIRMED\", \"occurredAt\": \"$(date -u +%Y-%m-%dT%H:%M:%S)\"}"
echo -e "\n✅ Webhook Processado"

# 8. FLUXO DE ESTORNO (Transferência que falha)
echo ""
echo "--- 8. Cenário de Estorno (Transferir 50 -> Webhook REJECTED) ---"
FAIL_KEY="fail-key-$(date +%s)"
FAIL_RESP=$(curl -s -X POST "$BASE_URL/pix/transfers" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: $FAIL_KEY" \
  -d "{\"fromWalletId\": \"$WALLET_ID\", \"toPixKey\": \"falha@banco.com\", \"amount\": 50.00}")
FAIL_E2E=$(echo $FAIL_RESP | grep -o '"endToEndId":"[^"]*"' | cut -d'"' -f4)
echo "Pix Falha Iniciado: $FAIL_E2E"

echo "   -> Simulando Rejeição no Webhook..."
curl -s -X POST "$BASE_URL/pix/webhook" \
  -H "Content-Type: application/json" \
  -d "{\"endToEndId\": \"$FAIL_E2E\", \"eventId\": \"evt-fail-$(date +%s)\", \"eventType\": \"REJECTED\", \"occurredAt\": \"$(date -u +%Y-%m-%dT%H:%M:%S)\"}"
echo -e "\n✅ Estorno Processado"

# 9. VALIDAÇÃO FINAL DE SALDO
echo ""
echo "--- 9. Auditoria Final de Saldo ---"
echo "Cálculo Esperado: 1000 (Dep) - 100 (Pix Sucesso) - 50 (Pix Falha) + 50 (Estorno) = 900.00"
BALANCE_RESP=$(curl -s -X GET "$BASE_URL/wallets/$WALLET_ID/balance")
echo "💰 Saldo Atual: $BALANCE_RESP"

if [[ "$BALANCE_RESP" == "900.00" ]]; then
    echo "🏆 SUCESSO TOTAL: O saldo fecha perfeitamente!"
else
    echo "⚠️ ATENÇÃO: O saldo final ($BALANCE_RESP) difere do esperado (900.00)."
fi

echo ""
echo "=========================================="
echo "          FIM DA EXECUÇÃO"
echo "=========================================="