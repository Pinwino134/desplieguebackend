package com.pinwino.apigateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinwino.apigateway.config.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filtro de Rate Limiting por IP y tipo de ruta (ventana fija de 60 segundos).
 * Protege contra ataques de fuerza bruta en login/registro y abuso de la API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final long WINDOW_MILLIS = 60_000L;
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!properties.isEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String path = request.getURI().getPath();
        String method = request.getMethod() != null ? request.getMethod().name() : "GET";
        boolean esRutaAuth = esEndpointSensibleAuth(method, path);

        int limit = esRutaAuth
                ? properties.getAuthRequestsPerMinute()
                : properties.getApiRequestsPerMinute();

        String clientIp = extraerIp(request);
        String bucketKey = clientIp + ":" + (esRutaAuth ? "AUTH" : "API");

        long now = Instant.now().toEpochMilli();
        limpiarSiExcedeCapacidad(now);

        WindowCounter counter = counters.compute(bucketKey, (k, existing) -> {
            if (existing == null || (now - existing.windowStart) >= WINDOW_MILLIS) {
                return new WindowCounter(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        int currentCount = counter.count.get();
        int remaining = Math.max(0, limit - currentCount);
        long retryAfterSeconds = Math.max(1L, (WINDOW_MILLIS - (now - counter.windowStart)) / 1000L);

        exchange.getResponse().getHeaders().set("X-RateLimit-Limit", String.valueOf(limit));
        exchange.getResponse().getHeaders().set("X-RateLimit-Remaining", String.valueOf(remaining));

        if (currentCount > limit) {
            log.warn("Rate limit excedido: ip={}, ruta={} {}, count={}/{}", clientIp, method, path, currentCount, limit);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().set("Content-Type", "application/json");
            exchange.getResponse().getHeaders().set("Retry-After", String.valueOf(retryAfterSeconds));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 429);
            body.put("mensaje", "Demasiadas solicitudes desde tu dirección IP. Intenta nuevamente en " + retryAfterSeconds + " segundos.");
            body.put("retryAfterSeconds", retryAfterSeconds);
            body.put("timestamp", LocalDateTime.now().toString());

            byte[] bytes;
            try {
                bytes = objectMapper.writeValueAsBytes(body);
            } catch (Exception e) {
                bytes = "{\"status\":429,\"mensaje\":\"Demasiadas solicitudes\"}".getBytes(StandardCharsets.UTF_8);
            }
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }

        return chain.filter(exchange);
    }

    private boolean esEndpointSensibleAuth(String method, String path) {
        return path.startsWith("/api/v1/auth/")
                || ("POST".equalsIgnoreCase(method) && "/api/v1/usuarios".equals(path));
    }

    private String extraerIp(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "unknown";
    }

    private void limpiarSiExcedeCapacidad(long now) {
        if (counters.size() > MAX_TRACKED_KEYS) {
            counters.entrySet().removeIf(e -> (now - e.getValue().windowStart) >= WINDOW_MILLIS);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }

    private record WindowCounter(long windowStart, AtomicInteger count) {}
}
