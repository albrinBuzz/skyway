package com.SkyWay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SkyWay.model.CategoriaPost;

@Repository
public interface CategoriaPostRepository extends JpaRepository<CategoriaPost, Integer> {
    // Puedes agregar consultas personalizadas aquí si es necesario
}
