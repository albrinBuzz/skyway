package com.SkyWay;

import com.SkyWay.util.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

@SpringBootApplication
@ComponentScan(basePackages = "com.SkyWay")
public class SkyWayApplication {

	public static void main(String[] args) {
		// Hilo en segundo plano para monitorear la memoria durante el arranque
		Thread bootMonitorThread = new Thread(() -> {
			MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

			while (!Thread.currentThread().isInterrupted()) {
				try {
					long heapUsed = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
					long heapCommitted = memoryBean.getHeapMemoryUsage().getCommitted() / (1024 * 1024);
					long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed() / (1024 * 1024); // Metaspace + CodeCache

					Logger.logInfo(String.format(
							"⏱️ [ARRANCANDO] Heap: %dMB/%dMB | Metaspace/CodeCache: %dMB | Total JVM: %dMB",
							heapUsed, heapCommitted, nonHeapUsed, (heapUsed + nonHeapUsed)
					));

					Thread.sleep(1000); // Muestra métricas cada 1 segundo
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		});

		bootMonitorThread.setDaemon(true);
		bootMonitorThread.setName("boot-memory-monitor");
		bootMonitorThread.start();

		// Inicia el proceso de Spring Boot
		SpringApplication.run(SkyWayApplication.class, args);

		// Detener el hilo de inicio limpiamente
		bootMonitorThread.interrupt();
		Logger.logInfo(" Arranque finalizado. Monitor de inicio detenido.");
	}
}