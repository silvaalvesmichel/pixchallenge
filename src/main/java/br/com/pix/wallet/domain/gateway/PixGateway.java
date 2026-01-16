package br.com.pix.wallet.domain.gateway;

import br.com.pix.wallet.domain.model.PixTransfer;

import java.util.Optional;

public interface PixGateway {
    PixTransfer save(PixTransfer transfer);
    Optional<PixTransfer> findByEndToEndId(String endToEndId);
}
