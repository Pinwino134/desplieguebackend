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

/**
 * Valida el JWT (emitido por authservice) en cada request que llega al gateway.
 * Si la ruta está en la lista de rutas públicas, la deja pasar sin más.
 * Si no, exige "Authorization: Bearer <token>" válido y, de pasar la
 * validación, propaga la identidad a los microservicios de abajo mediante
 * headers (X-Usuario-Id, X-Usuario-Email, X-Usuario-Tipo) para que ellos no
 * tengan que volver a decodificar el token.
 *
 * IMPORTANTE: siempre se eliminan esos headers si vienen del cliente antes de
 * asignarlos de nuevo, para que nadie pueda mandar "X-Usuario-Tipo: ADMINISTRADOR"
 * directamente y hacerse pasar por otro usuario (ataque de "confused deputy").
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String HEADER_USUARIO_ID = "X-Usuario-Id";
    private static final String HEADER_USUARIO_EMAIL = "X-Usuario-Email";
    private static final String HEADER_USUARIO_TIPO = "X-Usuario-Tipo";

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

        // Sea pública o no, nunca confiamos en estos headers si vienen del cliente.
        ServerWebExchange limpio = exchange.mutate()
                .request(r -> r.headers(h -> {
                    h.remove(HEADER_USUARIO_ID);
                    h.remove(HEADER_USUARIO_EMAIL);
                    h.remove(HEADER_USUARIO_TIPO);
                }))
                .build();

        if (esRutaPublica(method, path)) {
            return chain.filter(limpio);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return rechazar(exchange, "Falta el token de autenticación (header Authorization: Bearer <token>)");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String usuarioId = String.valueOf(claims.get("usuarioId"));
            String email = claims.getSubject();
            String tipoActor = String.valueOf(claims.get("tipoActor"));

            if (!rolPermitido(method, path, tipoActor)) {
                log.warn("Acceso denegado: {} {} - rol {} no autorizado", method, path, tipoActor);
                return prohibir(exchange, "Tu rol (" + tipoActor + ") no tiene permiso para acceder a este recurso");
            }

            ServerWebExchange conIdentidad = limpio.mutate()
                    .request(r -> r.headers(h -> {
                        h.set(HEADER_USUARIO_ID, usuarioId);
                        h.set(HEADER_USUARIO_EMAIL, email);
                        h.set(HEADER_USUARIO_TIPO, tipoActor);
                    }))
                    .build();

            return chain.filter(conIdentidad);

        } catch (ExpiredJwtException ex) {
            return rechazar(exchange, "El token expiró, inicia sesión de nuevo");
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Token inválido en {} {}: {}", method, path, ex.getMessage());
            return rechazar(exchange, "Token inválido");
        }
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
