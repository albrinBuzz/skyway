package com.SkyWay.service;

import java.util.List;
import java.util.Optional;

import com.SkyWay.model.CategoriaPost;

public interface CategoriaPostService {
    // Métodos CRUD
    List<CategoriaPost> findAll();
    Optional<CategoriaPost> findById(int id);
    CategoriaPost save(CategoriaPost categoriaPost);
    void deleteById(int id);
}
