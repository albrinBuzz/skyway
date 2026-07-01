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




		//SpringApplication.run(SkyWayApplication.class, args);
		var context = SpringApplication.run(SkyWayApplication.class, args);

		// Obtener los servicios a través de Spring Boot
		ReservaService reservaService = context.getBean(ReservaService.class);
		AsientoService asientoService = context.getBean(AsientoService.class);  // Suponiendo que AsientoService existe
		//BoletoService boletoService = context.getBean(BoletoService.class);    // Suponiendo que BoletoService existe

		// Parámetros de prueba
		int[] asientosPrimitivo = {181, 161, 163};

// Convertir int[] a Integer[]
		Integer[] asientos = Arrays.stream(asientosPrimitivo)
				.boxed()
				.toArray(Integer[]::new);
		// Llamar al método confirmarReserva
		//reservaService.confirmarReserva(6, asientos, "12345678-0");
		try {
			//String mensaje =
			//reservaService.confirmarReserva(6, asientos, "12345678-0",1);

			Logger.logInfo("exito");


		} catch (Exception ex) {
			Logger.logError("Error inesperado: " + ex.getMessage());

		}
	}

}
