package com.SkyWay.config.security;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/** Limita intentos fallidos de login por clave (IP + usuario). En memoria: no escala entre nodos. */
@Component
public class LoginThrottle {

	private static final int MAX_FALLOS = 5;
	private static final long VENTANA_MS = 15 * 60 * 1000L;

	private final ConcurrentHashMap<String, Deque<Long>> fallos = new ConcurrentHashMap<>();

	public static String clave(String ip, String usuario) {
		return ip + "|" + (usuario == null ? "" : usuario.trim().toLowerCase());
	}

	public boolean bloqueado(String clave) {
		Deque<Long> d = fallos.get(clave);
		if (d == null) return false;
		synchronized (d) {
			purgar(d);
			return d.size() >= MAX_FALLOS;
		}
	}

	public void registrarFallo(String clave) {
		if (fallos.size() > 50_000) fallos.clear(); // tope de memoria
		Deque<Long> d = fallos.computeIfAbsent(clave, k -> new ArrayDeque<>());
		synchronized (d) {
			purgar(d);
			d.addLast(System.currentTimeMillis());
		}
	}

	public void registrarExito(String clave) {
		fallos.remove(clave);
	}

	private void purgar(Deque<Long> d) {
		long limite = System.currentTimeMillis() - VENTANA_MS;
		while (!d.isEmpty() && d.peekFirst() < limite) d.removeFirst();
	}
}
