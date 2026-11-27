package com.pinwino.apigateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Añade cabeceras de seguridad HTTP estándar (OWASP) a todas las respuestas
 * que pasan por el API Gateway para mitigar XSS, clickjacking, MIME sniffing
 * y degradación de protocolo.
 */
@Component
public class SecurityHeadersFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getResponse().getHeaders();
        agregarSiFalta(headers, "X-Content-Type-Options", "nosniff");
        agregarSiFalta(headers, "X-Frame-Options", "DENY");
        agregarSiFalta(headers, "Referrer-Policy", "strict-origin-when-cross-origin");
        agregarSiFalta(headers, "Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        agregarSiFalta(headers, "Content-Security-Policy", "default-src 'self'; frame-ancestors 'none'");
        agregarSiFalta(headers, "Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        return chain.filter(exchange);
    }

    private void agregarSiFalta(HttpHeaders headers, String name, String value) {
        if (headers.getFirst(name) == null) {
            headers.set(name, value);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }
}
