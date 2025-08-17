
package com.SkyWay.modules.notificacion.domain.repository;

import java.util.List;

import com.SkyWay.modules.notificacion.domain.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;




@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer>{

		//List<Notificacion> findByRut(String rut);
	
}
