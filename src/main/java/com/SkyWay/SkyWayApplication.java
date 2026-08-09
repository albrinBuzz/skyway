package com.SkyWay;


import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.util.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.sql.SQLException;
import java.util.Arrays;

@SpringBootApplication
@ComponentScan(basePackages = "com.SkyWay;")
public class SkyWayApplication {
	public static void main(String[] args) {

		 SpringApplication.run(SkyWayApplication.class, args);

	}
}
