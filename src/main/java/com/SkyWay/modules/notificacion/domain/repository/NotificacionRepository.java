
package com.SkyWay.modules.notificacion.domain.repository;

import java.util.List;

import com.SkyWay.modules.notificacion.domain.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;




@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer>{

    org.springframework.data.domain.Page<Notificacion> findByUsuario_RutOrderByFechaDescIdNotificacionDesc(
            String rut, org.springframework.data.domain.Pageable pageable);

    long countByUsuario_RutAndLeidoFalse(String rut);

    java.util.Optional<Notificacion> findByIdNotificacionAndUsuario_Rut(Integer id, String rut);


    //List<Notificacion> findByRut(String rut);
	
}
