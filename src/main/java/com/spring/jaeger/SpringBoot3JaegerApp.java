package com.spring.jaeger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SpringBoot3JaegerApp {

	public static void main(String[] args) {
		SpringApplication.run(SpringBoot3JaegerApp.class, args);
	}

}
