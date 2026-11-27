package com.pinwino.apigateway.security;

import com.pinwino.apigateway.config.SecurityProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "c2FtcGxlLXNlY3JldC1jYW1iaWFyLWVuLXByb2R1Y2Npb24tZWNjb21lcmNlLXBpbndpbm8tMjAyNg==";

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityProperties props = new SecurityProperties();

        SecurityProperties.PublicRoute loginRoute = new SecurityProperties.PublicRoute();
        loginRoute.setMethod("POST");
        loginRoute.setPath("/api/v1/auth/login");

        SecurityProperties.PublicRoute registerRoute = new SecurityProperties.PublicRoute();
        registerRoute.setMethod("POST");
        registerRoute.setPath("/api/v1/usuarios");

        SecurityProperties.PublicRoute categoriasPublic = new SecurityProperties.PublicRoute();
        categoriasPublic.setMethod("GET");
        categoriasPublic.setPath("/api/v1/categorias/**");

        props.setPublicRoutes(List.of(loginRoute, registerRoute, categoriasPublic));

        SecurityProperties.RoleRoute adminUsuarios = new SecurityProperties.RoleRoute();
        adminUsuarios.setMethod("GET");
        adminUsuarios.setPath("/api/v1/usuarios/**");
        adminUsuarios.setRoles(List.of("ADMINISTRADOR", "GERENCIA"));

        SecurityProperties.RoleRoute solicitudesAdmin = new SecurityProperties.RoleRoute();
        solicitudesAdmin.setMethod("GET");
        solicitudesAdmin.setPath("/api/v1/solicitudes");
        solicitudesAdmin.setRoles(List.of("ADMINISTRADOR", "GERENCIA"));

        props.setRoleRoutes(List.of(adminUsuarios, solicitudesAdmin));

        filter = new JwtAuthenticationFilter(props);
        ReflectionTestUtils.setField(filter, "secret", SECRET);
    }

    private String crearToken(Long usuarioId, String email, String tipoActor) {
        return Jwts.builder()
                .subject(email)
                .claim("usuarioId", usuarioId)
                .claim("tipoActor", tipoActor)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    void rutaPublicaSinTokenPermitePasoYEliminaHeadersFalsificados() {
        MockServerHttpRequest req = MockServerHttpRequest.get("/api/v1/categorias")
                .header("X-Usuario-Tipo", "ADMINISTRADOR")
                .header("X-Usuario-Id", "999")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(req);

        AtomicReference<ServerWebExchange> capturado = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            capturado.set(ex);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertNotNull(capturado.get());
        assertNull(capturado.get().getRequest().getHeaders().getFirst("X-Usuario-Tipo"));
        assertNull(capturado.get().getRequest().getHeaders().getFirst("X-Usuario-Id"));
        assertNotNull(capturado.get().getRequest().getHeaders().getFirst("X-Correlation-Id"));
    }

    @Test
    void rutaPublicaConTokenValidoPropagaIdentidadAdmin() {
        String tokenAdmin = crearToken(1L, "admin@arteandino.pe", "ADMINISTRADOR");
        MockServerHttpRequest req = MockServerHttpRequest.post("/api/v1/usuarios")
                .header("Authorization", "Bearer " + tokenAdmin)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(req);

        AtomicReference<ServerWebExchange> capturado = new AtomicReference<>();
        filter.filter(exchange, ex -> {
            capturado.set(ex);
            return Mono.empty();
        }).block();

        assertNotNull(capturado.get());
        assertEquals("1", capturado.get().getRequest().getHeaders().getFirst("X-Usuario-Id"));
        assertEquals("ADMINISTRADOR", capturado.get().getRequest().getHeaders().getFirst("X-Usuario-Tipo"));
    }

    @Test
    void rutaProtegidaSinTokenDevuelve401() {
        MockServerHttpRequest req = MockServerHttpRequest.get("/api/v1/usuarios").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(req);

        filter.filter(exchange, ex -> Mono.empty()).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void rutaRestringidaConRolClienteDevuelve403() {
        String tokenCliente = crearToken(20L, "cliente@test.pe", "CLIENTE");
        MockServerHttpRequest req = MockServerHttpRequest.get("/api/v1/solicitudes")
                .header("Authorization", "Bearer " + tokenCliente)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(req);

        filter.filter(exchange, ex -> Mono.empty()).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void rutaRestringidaConRolGerenciaPermiteAcceso() {
        String tokenGerencia = crearToken(5L, "gerencia@arteandino.pe", "GERENCIA");
        MockServerHttpRequest req = MockServerHttpRequest.get("/api/v1/solicitudes")
                .header("Authorization", "Bearer " + tokenGerencia)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(req);

        AtomicReference<ServerWebExchange> capturado = new AtomicReference<>();
        filter.filter(exchange, ex -> {
            capturado.set(ex);
            return Mono.empty();
        }).block();

        assertNotNull(capturado.get());
        assertEquals("GERENCIA", capturado.get().getRequest().getHeaders().getFirst("X-Usuario-Tipo"));
        assertEquals("gerencia@arteandino.pe", capturado.get().getRequest().getHeaders().getFirst("X-Usuario-Email"));
    }
}
