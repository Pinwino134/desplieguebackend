package com.pinwino.microservice.gerenciaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GerenciaserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GerenciaserviceApplication.class, args);
	}

}
