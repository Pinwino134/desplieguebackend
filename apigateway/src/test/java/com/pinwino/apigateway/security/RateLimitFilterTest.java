package com.pinwino.apigateway.security;

import com.pinwino.apigateway.config.RateLimitProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitFilterTest {

    @Test
    void bloqueaCon429CuandoSuperaLimiteEnLogin() {
        RateLimitProperties props = new RateLimitProperties();
        props.setEnabled(true);
        props.setAuthRequestsPerMinute(2);
        props.setApiRequestsPerMinute(10);

        RateLimitFilter filter = new RateLimitFilter(props);

        for (int i = 0; i < 2; i++) {
            MockServerWebExchange okExchange = MockServerWebExchange.from(
                    MockServerHttpRequest.post("/api/v1/auth/login")
                            .header("X-Forwarded-For", "192.168.1.50")
                            .build()
            );
            filter.filter(okExchange, ex -> Mono.empty()).block();
            assertNull(okExchange.getResponse().getStatusCode());
        }

        MockServerWebExchange blockedExchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/auth/login")
                        .header("X-Forwarded-For", "192.168.1.50")
                        .build()
        );
        filter.filter(blockedExchange, ex -> Mono.empty()).block();

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, blockedExchange.getResponse().getStatusCode());
        assertNotNull(blockedExchange.getResponse().getHeaders().getFirst("Retry-After"));
    }

    @Test
    void agregaCabecerasDeSeguridadOwasp() {
        SecurityHeadersFilter headersFilter = new SecurityHeadersFilter();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/productos").build()
        );

        headersFilter.filter(exchange, ex -> Mono.empty()).block();

        assertEquals("nosniff", exchange.getResponse().getHeaders().getFirst("X-Content-Type-Options"));
        assertEquals("DENY", exchange.getResponse().getHeaders().getFirst("X-Frame-Options"));
        assertNotNull(exchange.getResponse().getHeaders().getFirst("Content-Security-Policy"));
        assertNotNull(exchange.getResponse().getHeaders().getFirst("Strict-Transport-Security"));
    }
}
