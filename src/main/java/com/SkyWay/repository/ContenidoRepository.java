package com.SkyWay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SkyWay.model.Contenido;

public interface ContenidoRepository extends JpaRepository<Contenido, Integer>{

	 List<Contenido> findByTipo(String tipo);
	                          
}
