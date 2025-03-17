package com.SkyWay.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.model.CategoriaPost;
import com.SkyWay.repository.CategoriaPostRepository;
import com.SkyWay.service.CategoriaPostService;

@Service
public class CategoriaPostServiceImpl implements CategoriaPostService {

    private final CategoriaPostRepository categoriaPostRepository;

    @Autowired
    public CategoriaPostServiceImpl(CategoriaPostRepository categoriaPostRepository) {
        this.categoriaPostRepository = categoriaPostRepository;
    }

    @Override
    public List<CategoriaPost> findAll() {
        return categoriaPostRepository.findAll();
    }

    @Override
    public Optional<CategoriaPost> findById(int id) {
        return categoriaPostRepository.findById(id);
    }

    @Override
    public CategoriaPost save(CategoriaPost categoriaPost) {
        return categoriaPostRepository.save(categoriaPost);
    }

    @Override
    public void deleteById(int id) {
        categoriaPostRepository.deleteById(id);
    }
}
