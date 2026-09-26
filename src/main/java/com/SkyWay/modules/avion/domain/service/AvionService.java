package com.SkyWay.modules.avion.domain.service;

import com.SkyWay.modules.avion.domain.model.Avion;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AvionService {

	public List<Avion> findAll();
	public Optional<Avion> findById(Integer id);
	public Avion save(Avion vuelo);
	public boolean deleteById(Integer id);
	public Avion updateAvion(Avion avion);

	// Método para guardar avión y sincronizar CapacidadClase (que activa el Trigger de Asientos)
	public Avion saveConCapacidades(Avion avion, Map<Integer, Integer> capacidadesPorClase);
}