package com.SkyWay;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.SkyWay;")
public class SkyWayApplication {

	public static void main(String[] args) {




		SpringApplication.run(SkyWayApplication.class, args);
	}

}
