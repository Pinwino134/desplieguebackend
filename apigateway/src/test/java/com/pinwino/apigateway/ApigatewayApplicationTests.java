package com.pinwino.apigateway;

import com.pinwino.apigateway.config.RateLimitProperties;
import com.pinwino.apigateway.config.SecurityProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApigatewayApplicationTests {

    @Test
    void configuracionesPorDefectoSonSeguras() {
        RateLimitProperties rateLimit = new RateLimitProperties();
        assertTrue(rateLimit.isEnabled());
        assertTrue(rateLimit.getAuthRequestsPerMinute() > 0);
        assertTrue(rateLimit.getApiRequestsPerMinute() >= rateLimit.getAuthRequestsPerMinute());

        SecurityProperties security = new SecurityProperties();
        assertNotNull(security.getPublicRoutes());
        assertNotNull(security.getRoleRoutes());
    }
}
