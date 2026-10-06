package com.pinwino.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Rutas que el filtro de JWT deja pasar sin exigir token.
 * Se configuran en config/apigateway.yml bajo "app.security.public-routes".
 */
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private List<PublicRoute> publicRoutes = new ArrayList<>();
    private List<RoleRoute> roleRoutes = new ArrayList<>();

    public List<PublicRoute> getPublicRoutes() {
        return publicRoutes;
    }

    public void setPublicRoutes(List<PublicRoute> publicRoutes) {
        this.publicRoutes = publicRoutes;
    }

    public List<RoleRoute> getRoleRoutes() {
        return roleRoutes;
    }

    public void setRoleRoutes(List<RoleRoute> roleRoutes) {
        this.roleRoutes = roleRoutes;
    }

    public static class PublicRoute {
        /** GET, POST, PUT, DELETE... o "*" para cualquier método. */
        private String method = "*";
        /** Patrón Ant, ej: /api/v1/auth/login o /api/v1/productos/** */
        private String path;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    /**
     * Ruta que exige, además de un token válido, que el tipoActor del usuario
     * esté en la lista de roles permitidos. Si una ruta autenticada no
     * coincide con ninguna entrada de esta lista, cualquier usuario logueado
     * puede acceder (sin importar su rol).
     */
    public static class RoleRoute {
        private String method = "*";
        private String path;
        private List<String> roles = new ArrayList<>();

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
