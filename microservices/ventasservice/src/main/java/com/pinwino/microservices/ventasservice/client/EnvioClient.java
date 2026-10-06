package com.pinwino.microservices.ventasservice.client;

import com.pinwino.microservices.ventasservice.client.dto.EnvioRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
	    name = "logisticaservice",
	    contextId = "envioClient"
)
public interface EnvioClient {

    @PostMapping("/api/v1/envios")
    Object crear(@RequestParam("almacenId") Long almacenId,
                 @RequestBody EnvioRequestDTO envio);
}
