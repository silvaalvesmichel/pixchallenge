package br.com.pix.wallet.domain.gateway;

import br.com.pix.wallet.domain.model.PixEvent;

public interface PixEventGateway {
    boolean existsByEventId(String eventId);
    void save(PixEvent event);
}
