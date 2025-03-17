package com.SkyWay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SkyWay.model.Post;


@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    // Puedes agregar consultas personalizadas aquí si es necesario
	
	List<Post> findByTitulo(String titulo);
	
}
