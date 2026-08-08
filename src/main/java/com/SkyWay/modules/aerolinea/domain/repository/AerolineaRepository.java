package com.SkyWay.modules.aerolinea.domain.repository;




import com.SkyWay.modules.aerolinea.domain.model.Aerolinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AerolineaRepository extends JpaRepository<Aerolinea, Integer> {
    // Puedes agregar métodos personalizados aquí si necesitas
}
