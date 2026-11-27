package com.pinwino.discoveryserver;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscoveryserverApplicationTests {

    @Test
    void aplicacionTieneAnotacionEurekaServer() {
        assertTrue(DiscoveryserverApplication.class.isAnnotationPresent(EnableEurekaServer.class));
    }
}
