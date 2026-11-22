package com.pinwino.apigateway.security;

import com.pinwino.apigateway.config.SecurityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Valida el JWT (emitido por authservice) en cada request que llega al gateway.
 * Si la ruta está en la lista de rutas públicas, la deja pasar sin exigir token;
 * pero si viene un Bearer token válido (ej. un ADMINISTRADOR creando otro usuario
 * en POST /api/v1/usuarios), propaga igual su identidad verificada.
 * Si no es pública, exige "Authorization: Bearer <token>" válido y, de pasar la
 * validación, propaga la identidad a los microservicios de abajo mediante
 * headers (X-Usuario-Id, X-Usuario-Email, X-Usuario-Tipo) y un X-Correlation-Id
 * para trazabilidad distribuida.
 *
 * IMPORTANTE: siempre se eliminan los headers X-Usuario-* si vienen del cliente
 * antes de asignarlos de nuevo, para evitar ataques de "confused deputy".
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String HEADER_USUARIO_ID = "X-Usuario-Id";
    private static final String HEADER_USUARIO_EMAIL = "X-Usuario-Email";
    private static final String HEADER_USUARIO_TIPO = "X-Usuario-Tipo";
    private static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    private final SecurityProperties securityProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        String incomingCorrelationId = request.getHeaders().getFirst(HEADER_CORRELATION_ID);
        String correlationId = (incomingCorrelationId != null && !incomingCorrelationId.isBlank())
                ? incomingCorrelationId
                : UUID.randomUUID().toString();
        exchange.getResponse().getHeaders().set(HEADER_CORRELATION_ID, correlationId);

        // Sea pública o no, nunca confiamos en estos headers si vienen del cliente.
        ServerWebExchange limpio = exchange.mutate()
                .request(r -> r.headers(h -> {
                    h.remove(HEADER_USUARIO_ID);
                    h.remove(HEADER_USUARIO_EMAIL);
                    h.remove(HEADER_USUARIO_TIPO);
                    h.set(HEADER_CORRELATION_ID, correlationId);
                }))
                .build();

        String authHeader = request.getHeaders().getFirst("Authorization");

        if (esRutaPublica(method, path)) {
            // Si la ruta es pública pero el cliente envió un token válido (ej. admin
            // creando cuentas en POST /api/v1/usuarios), propagamos su identidad.
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    Claims claims = parsearClaims(authHeader.substring(7));
                    return chain.filter(agregarIdentidad(limpio, claims));
                } catch (JwtException | IllegalArgumentException ignored) {
                    // En ruta pública, un token inválido/expirado se trata como anónimo.
                }
            }
            return chain.filter(limpio);
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[{}] Acceso sin token a {} {}", correlationId, method, path);
            return rechazar(exchange, "Falta el token de autenticación (header Authorization: Bearer <token>)");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = parsearClaims(token);
            String tipoActor = String.valueOf(claims.get("tipoActor"));

            if (!rolPermitido(method, path, tipoActor)) {
                log.warn("[{}] Acceso denegado: {} {} - rol {} no autorizado", correlationId, method, path, tipoActor);
                return prohibir(exchange, "Tu rol (" + tipoActor + ") no tiene permiso para acceder a este recurso");
            }

            return chain.filter(agregarIdentidad(limpio, claims));

        } catch (ExpiredJwtException ex) {
            log.warn("[{}] Token expirado en {} {}", correlationId, method, path);
            return rechazar(exchange, "El token expiró, inicia sesión de nuevo");
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("[{}] Token inválido en {} {}: {}", correlationId, method, path, ex.getMessage());
            return rechazar(exchange, "Token inválido");
        }
    }

    private Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private ServerWebExchange agregarIdentidad(ServerWebExchange base, Claims claims) {
        String usuarioId = String.valueOf(claims.get("usuarioId"));
        String email = claims.getSubject();
        String tipoActor = String.valueOf(claims.get("tipoActor"));

        return base.mutate()
                .request(r -> r.headers(h -> {
                    h.set(HEADER_USUARIO_ID, usuarioId);
                    h.set(HEADER_USUARIO_EMAIL, email);
                    h.set(HEADER_USUARIO_TIPO, tipoActor);
                }))
                .build();
    }

    private boolean esRutaPublica(String method, String path) {
        for (SecurityProperties.PublicRoute ruta : securityProperties.getPublicRoutes()) {
            boolean metodoCoincide = "*".equals(ruta.getMethod()) || ruta.getMethod().equalsIgnoreCase(method);
            if (metodoCoincide && pathMatcher.match(ruta.getPath(), path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Si la ruta coincide con alguna entrada de "role-routes", el tipoActor
     * debe estar en esa lista de roles permitidos. Si no coincide con
     * ninguna, se permite (cualquier usuario autenticado, sin restricción
     * de rol).
     */
    private boolean rolPermitido(String method, String path, String tipoActor) {
        for (SecurityProperties.RoleRoute ruta : securityProperties.getRoleRoutes()) {
            boolean metodoCoincide = "*".equals(ruta.getMethod()) || ruta.getMethod().equalsIgnoreCase(method);
            if (metodoCoincide && pathMatcher.match(ruta.getPath(), path)) {
                return ruta.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase(tipoActor));
            }
        }
        return true;
    }

    private Mono<Void> rechazar(ServerWebExchange exchange, String mensaje) {
        return responder(exchange, HttpStatus.UNAUTHORIZED, mensaje);
    }

    private Mono<Void> prohibir(ServerWebExchange exchange, String mensaje) {
        return responder(exchange, HttpStatus.FORBIDDEN, mensaje);
    }

    private Mono<Void> responder(ServerWebExchange exchange, HttpStatus status, String mensaje) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("mensaje", mensaje);
        body.put("timestamp", LocalDateTime.now().toString());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            bytes = ("{\"status\":" + status.value() + ",\"mensaje\":\"" + mensaje + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
