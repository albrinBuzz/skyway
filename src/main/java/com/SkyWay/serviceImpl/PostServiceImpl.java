package com.SkyWay.serviceImpl;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.model.Post;
import com.SkyWay.repository.PostRepository;
import com.SkyWay.service.PostService;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    public Optional<Post> findById(int id) {
        return postRepository.findById(id);
    }

    @Override
    public Post save(Post post) {
        return postRepository.save(post);
    }

    @Override
    public void deleteById(int id) {
    	
        postRepository.deleteById(id);
    }

	@Override
	public List<Post> findByTitulo(String titulo) {
		// TODO Auto-generated method stub
		return postRepository.findByTitulo(titulo);
	}
}
