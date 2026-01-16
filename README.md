# Pix Wallet Microservice

Microsserviço de carteira digital com suporte a transferências Pix, focado em consistência, concorrência e idempotência. Este projeto foi desenvolvido como um **Code Assessment** simulando um ambiente de produção de missão crítica.

## 🛠 Tecnologias

* **Linguagem:** Java 21
* **Framework:** Spring Boot 3.3+
* **Banco de Dados:** PostgreSQL 16
* **Migração:** Flyway
* **Testes:** JUnit 5, Mockito, Testcontainers (conceitual), H2 (para testes unitários rápidos)
* **Containerização:** Docker & Docker Compose

---

## 🚀 Como Executar

### Pré-requisitos
* Docker e Docker Compose instalados.
* JDK 21 instalado (ou utilize o wrapper do Maven).

### Passo a Passo

1.  **Subir Infraestrutura (Postgres):**
    ```bash
    docker-compose up -d
    ```

2.  **Executar a Aplicação:**
    ```bash
    ./mvnw spring-boot:run
    ```
    A API estará disponível em `http://localhost:8080`.
    *Swagger/Actuator (se habilitados):* `http://localhost:8080/actuator`

3.  **Rodar os Testes (Unitários e Integração):**
    ```bash
    ./mvnw test
    ```

---

## 🏗 Arquitetura & Decisões de Design

O projeto segue estritamente a **Clean Architecture** (Arquitetura Limpa) combinada com **Domain-Driven Design (DDD)**.

### Estrutura de Pacotes
* **`domain`**: O coração do software. Contém Entidades (`Wallet`, `PixKey`), Value Objects e Interfaces de Gateways. **Zero dependência de Frameworks.**
* **`application`**: Casos de Uso (`UseCases`) que orquestram o fluxo de dados e regras de aplicação.
* **`infrastructure`**: Implementação técnica. Controllers REST, Persistência JPA, Configurações de Beans.

### Soluções para Requisitos Não Funcionais (RNF)

#### 1. Concorrência e Race Conditions (Missão Crítica)
* **Desafio:** Evitar "Double Spending" quando múltiplas threads tentam sacar da mesma conta simultaneamente.
* **Solução:** **Pessimistic Locking** (`SELECT ... FOR UPDATE`) no `WalletJpaRepository`.
* **Por que?** Em sistemas financeiros, a consistência forte (ACID) é prioritária sobre a vazão (throughput) absoluta. O Lock Pessimista garante que as transações sejam serializadas no nível do banco de dados, impedindo saldo negativo matematicamente.

#### 2. Idempotência (Segurança)
* **Desafio:** Falhas de rede podem fazer clientes reenviarem a mesma requisição de transferência.
* **Solução:** Implementação do padrão **Idempotency-Key** com tabela dedicada e restrição de unicidade (`UNIQUE CONSTRAINT`).
* **Fluxo:** O sistema verifica se a chave já existe antes de processar. Se existir, retorna o resultado cacheado sem tocar no saldo. Se ocorrer concorrência na inserção da chave, a constraint do banco garante que apenas uma vença ("first-write-wins").

#### 3. Auditoria e Rastreabilidade
* **Desafio:** Reconstruir o histórico e garantir que o saldo seja auditável.
* **Solução:** Padrão **Ledger** (Razão Contábil).
* **Detalhe:** A tabela `ledger` é *append-only* (apenas inserção). O saldo atual é um snapshot otimizado, mas a "verdade" reside na soma de créditos e débitos do Ledger. Isso permitiu a implementação do **Saldo Histórico** (`time-travel`) via SQL nativo.

#### 4. Consistência Eventual (Pix Webhook)
* **Desafio:** Webhooks duplicados ou fora de ordem.
* **Solução:** Máquina de Estados na entidade `PixTransfer` (`PENDING` -> `CONFIRMED` | `REJECTED`) e tabela de log de eventos (`pix_events`).
* **Estorno:** Se um evento `REJECTED` é recebido, o sistema executa automaticamente uma **Transação de Compensação**, devolvendo o valor à carteira e registrando no Ledger.

---

## ⚖️ Trade-offs e Compromissos

Devido ao escopo e limite de tempo, as seguintes decisões foram tomadas:

1.  **Lock Pessimista vs. Escalabilidade:**
    * *Decisão:* Usar Lock Pessimista.
    * *Compromisso:* Isso limita a concorrência em uma única carteira ("Hot Wallet").
    * *Idealmente:* Para contas de altíssima frequência, usaríamos uma arquitetura assíncrona baseada em eventos (Event Sourcing) ou Optimistic Locking com backoff/retry.

2.  **Processamento Síncrono de Webhooks:**
    * *Decisão:* Processar o webhook na thread HTTP.
    * *Compromisso:* Se o banco estiver lento, podemos dar timeout no PSP.
    * *Idealmente:* O endpoint do webhook apenas publicaria em uma fila (RabbitMQ/Kafka) para processamento assíncrono e resiliente (DLQ).

3.  **Segurança (AuthN/AuthZ):**
    * *Decisão:* Não implementar OAuth2/JWT.
    * *Compromisso:* A API está aberta.
    * *Idealmente:* Integração com Keycloak ou Spring Security Resource Server.

4.  **Chaves Pix Internas:**
    * *Decisão:* Simulação simplificada onde chaves não encontradas localmente são consideradas "Externas" e aprovadas.
    * *Compromisso:* Não há validação real junto ao DICT (Bacen).

---

## ✅ Cobertura de Testes

O projeto possui alta cobertura de testes automatizados:

* **Testes Unitários:** Validam regras de domínio, estados das entidades e lógica dos UseCases com Mocks.
* **Testes de Integração:** `ConcurrencyTest` valida o Lock Pessimista simulando 20 threads concorrentes.
* **Testes de Controller:** `@WebMvcTest` valida contratos de API e tratamento de exceções.
* **Validação Manual:** Script `validate.sh` incluso para teste E2E via `curl`.

---