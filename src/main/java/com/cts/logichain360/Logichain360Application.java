package com.cts.logichain360;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class Logichain360Application {
	public static void main(String[] args) {
		SpringApplication.run(Logichain360Application.class, args);
	}
}