package com.SkyWay;

import com.SkyWay.util.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

@SpringBootApplication
@ComponentScan(basePackages = "com.SkyWay")
@EnableScheduling
@EnableAsync
public class SkyWayApplication {

	public static void main(String[] args) {

		SpringApplication.run(SkyWayApplication.class, args);

	}
}