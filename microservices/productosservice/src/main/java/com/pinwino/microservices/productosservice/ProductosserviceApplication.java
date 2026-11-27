package com.pinwino.microservices.productosservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProductosserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductosserviceApplication.class, args);
	}

}
