package br.com.pix.wallet.infrastructure.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConfigTest {

    @Test
    void shouldConfigureObjectMapperCorrectly() throws Exception {
        JacksonConfig config = new JacksonConfig();
        ObjectMapper mapper = config.objectMapper();

        assertThat(mapper).isNotNull();

        // Testa serialização de Data (deve ser String ISO, não array)
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
        String json = mapper.writeValueAsString(now);
        assertThat(json).contains("2026-01-01T12:00:00");
    }
}
