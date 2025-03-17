package com.SkyWay.service;

import java.util.List;
import java.util.Optional;

import com.SkyWay.model.Post;

public interface PostService {
    // Métodos CRUD
    List<Post> findAll();
    Optional<Post> findById(int id);
    Post save(Post post);
    void deleteById(int id);
    
	List<Post> findByTitulo(String titulo);
}
