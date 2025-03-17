package com.SkyWay.config.dataBase;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaConfig {


    private EntityManagerFactory emf;

    @Bean
    public EntityManager getEntityManager() {
        emf = Persistence.createEntityManagerFactory("mainPU");
        return emf.createEntityManager();
    }
}
