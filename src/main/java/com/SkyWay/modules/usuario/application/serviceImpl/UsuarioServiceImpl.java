package com.SkyWay.modules.usuario.application.serviceImpl;

import java.util.Date;
import java.util.List;
import java.util.Optional;


import com.SkyWay.modules.rolusuario.domain.model.Role;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.pasajero.domain.service.PasajeroService;
import com.SkyWay.modules.piloto.domain.service.PiloService;
import com.SkyWay.modules.rolusuario.domain.service.RolService;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import com.SkyWay.modules.usuario.domain.repository.UsuarioRepository;
import com.SkyWay.modules.usuario.domain.service.UsuarioService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl implements UsuarioService{

	private static final Logger LOGGER = LoggerFactory.getLogger(UsuarioServiceImpl.class);


	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private RolService rolService;

	@Autowired
	private PiloService piloService;

	@Autowired
	private PasajeroService pasajeroService;

	@PersistenceContext
	private EntityManager em;
	
	/*private EntityManagerFactory emf ;
	
	  public EntityManager getEntityManager() {
	  	emf = Persistence.createEntityManagerFactory("mainPU");
	    return emf.createEntityManager();
	}*/
	
	
	//@Autowired
	//private BCryptPasswordEncoder bCrypt;
	
    private PasswordEncoder passwordEncoder=new BCryptPasswordEncoder() ;
	
	@Override
	public Usuario save(Usuario us) {
		return usuarioRepository.save(us);
	}



	@Override
	public void update(Usuario us) {
		usuarioRepository.save(us);
	}

	@Override
	public void delete(Integer id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Usuario> findAll() {
		// TODO Auto-generated method stub
		return usuarioRepository.findAll();
	}

	@Override
	public void create(Usuario us) {
	    
		us.setContrasena(passwordEncoder.encode(us.getContrasena()));
		StoredProcedureQuery procedureQuery = em
	              .createStoredProcedureQuery("sp_insertUsuario");
	      procedureQuery.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
	      procedureQuery.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
	      procedureQuery.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
	      
	      procedureQuery.setParameter(1, us.getNombre());
	      procedureQuery.setParameter(2, us.getCorreoElectronico());
	      procedureQuery.setParameter(3, us.getContrasena());
	      
	      procedureQuery.execute();
		
	}

    @Override
    public Usuario findUserByEmail(String email) {
        return usuarioRepository.findByCorreoElectronico(email);
    }

	@Override
	public Usuario findByNombre(String nombre) {
		// TODO Auto-generated method stub
		return usuarioRepository.findByCorreoElectronico(nombre);
	}


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

	    Usuario usuario = usuarioRepository.findByCorreoElectronico(username);
	    
	    if (usuario == null) {

			throw new UsernameNotFoundException("Usuario o password invalido");
		}

	    return new UsuarioDetails(usuario);
	    
	    /*return User.builder()
	        .username(usuario.getNombre())
	        .password(usuario.getPassword()) // No vuelvas a codificar la contraseña
	        .roles(usuario.getRol())
	        .build();*/
	    
	    //return usuario;
	}



	@Override
	public Optional<Usuario> findByRut(String id) {
		// TODO Auto-generated method stub
		return usuarioRepository.findById(id);
	}

	@Transactional
	@Override
	public Usuario buscarPorCorreo(String correo) {
		// Buscar usuario por correo
		/*var usuario = findUserByEmail(correo);

		// Verificar si no se encontró el usuario
		if (usuario == null) {
			throw new IllegalArgumentException("Usuario no encontrado para el correo: " + correo);
		}

		// Si el usuario es admin, retornamos el objeto directamente
		if (usuario.getRol().getNombre().equalsIgnoreCase("admin")) {
			return usuario;
		}

		// Obtener correo y rol del usuario
		var rol = usuario.getRol();*/

        // Llamar a la fábrica para crear el usuario según el rol
		TypedQuery<Usuario> query = em.createQuery(
				"SELECT e FROM Usuario e WHERE e.correoElectronico = :valor", Usuario.class);
		query.setParameter("valor", correo);
		var usuario=query.getSingleResult();
		/*if(usuario instanceof Piloto piloto) {
			System.out.println(" ");
			com.SkyWay.util.Logger.logInfo("es un piloto");
			System.out.println();
			com.SkyWay.util.Logger.logInfo(piloto.toString());
		}else {
			System.out.println(" ");
			System.out.println("es un Pasajero");
			com.SkyWay.util.Logger.logInfo("es un Pasajero");
			System.out.println();
			com.SkyWay.util.Logger.logInfo(usuario.toString());
		}*/


        return usuario;
		//return usuarioFactory(correo);
	}


	public Usuario usuarioFactory(String correo) {
		// Preparar la consulta para el procedimiento almacenado
		try  {
			StoredProcedureQuery query = em.createStoredProcedureQuery("sp_obtener_usuario_por_correo");

			// Registrar los parámetros del procedimiento
			query.registerStoredProcedureParameter("p_correo_electronico", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("resultado_cursor", void.class, ParameterMode.REF_CURSOR);

			// Establecer el valor del parámetro
			query.setParameter("p_correo_electronico", correo);

			// Ejecutar el procedimiento y obtener el resultado
			List<Object[]> resultList = query.getResultList();

			// Mapeo de usuario
			Usuario usuario = mapearUsuario(resultList);

			// Obtener el rol desde el rolService
			Integer rolId = (Integer) resultList.get(0)[0]; // Suponiendo que el rolId está en la primera fila
			//usuario.setRol(rolService.getRoleById(rolId).orElseThrow(() -> new IllegalArgumentException("Rol no encontrado")));

			return usuario;  // Retornar el objeto Usuario (Piloto o Pasajero)
		} catch (Exception e) {
			throw new RuntimeException("Error al obtener el usuario por correo", e);
		}
	}


	private Usuario mapearUsuario(List<Object[]> resultList) {
		for (Object[] row : resultList) {
			Integer tipoUsuario = (Integer) row[1];  // Obtenemos el tipo de usuario
			String rut = (String) row[2];
			String apellido = (String) row[3];
			String contrasena = (String) row[4];
			String correoElectronico = (String) row[5];
			String documentoIdentidad = (String) row[6];
			Date fechaNacimiento = (Date) row[7];
			String nombre = (String) row[8];
			String rol = (String) row[9];
			String telefono = (String) row[10];

			// Mapear el usuario según el tipo
			/*if (tipoUsuario == 1) {
				Integer experienciaAnos = (Integer) row[11];
				String licencia = (String) row[12];
				return new Piloto(rut, apellido, contrasena, correoElectronico, documentoIdentidad,
						fechaNacimiento, nombre, rol, telefono, experienciaAnos, licencia);
			} else if (tipoUsuario == 2) {
				return new Pasajero(rut, apellido, contrasena, correoElectronico, documentoIdentidad,
						fechaNacimiento, nombre, rol, telefono);
			} else {
				return new Usuario(rut, apellido, contrasena, correoElectronico, documentoIdentidad,
						fechaNacimiento, nombre, telefono);
			}*/

		}
		return null;  // En caso de no encontrar ningún usuario
	}




	public Usuario usuarioFactory(String correo, Role rol) {
		LOGGER.info("Iniciando búsqueda de usuario por correo: {} y rol: {}", correo, rol.getNombre());

		// Validación previa del rol
		if (correo == null || correo.isEmpty()) {
			LOGGER.error("El correo proporcionado es nulo o vacío. No se puede buscar el usuario.");
			throw new IllegalArgumentException("El correo no puede ser nulo o vacío.");
		}

		if (rol == null || rol.getNombre().isEmpty()) {
			LOGGER.error("El rol proporcionado es nulo o vacío.");
			throw new IllegalArgumentException("El rol no puede ser nulo o vacío.");
		}

		// Realizamos la búsqueda dependiendo del rol
		try {
			switch (rol.getNombre().toLowerCase()) {
				case "piloto":
					LOGGER.debug("Buscando piloto con correo: {}", correo);
					// Usamos un parámetro con nombre en la consulta
					//return mapperUsuario("select p from Piloto p where p.correoElectronico = :correo", correo, "correo", Piloto.class);
				case "pasajero":
					LOGGER.debug("Buscando pasajero con correo: {}", correo);
					// Usamos un parámetro con nombre en la consulta
					//return mapperUsuario("select p from Pasajero p where p.correoElectronico = :correo", correo, "correo", Pasajero.class);
				case "admin":
					LOGGER.debug("Buscando admin con correo: {}", correo);
					return mapperUsuario("select u from Usuario u where u.correoElectronico = :correo", correo, "correo", Usuario.class);
				default:
					LOGGER.error("Rol no válido recibido: {}", rol.getNombre());
					throw new IllegalArgumentException("Rol no válido: " + rol.getNombre());
			}
		} catch (Exception e) {
			LOGGER.error("Error durante la creación del usuario. Correo: {}, Rol: {}", correo, rol.getNombre(), e);
			throw e;  // Re-lanzar la excepción para que la capa superior maneje el error
		}
	}

	private <T> T mapperUsuario(String query, String parametro, String filter, Class<T> entityClass) {
		TypedQuery<T> typedQuery = em.createQuery(query, entityClass);

		// Usamos el parámetro con nombre
		typedQuery.setParameter(filter, parametro);

		return typedQuery.getSingleResult();
	}

	/*public Usuario factoryUsuario(String correo, Rol rol) {
		LOGGER.info("Iniciando búsqueda de usuario por correo: {} y rol: {}", correo, rol.getNombre());

		// Validación previa del rol
		if (correo == null || correo.isEmpty()) {
			LOGGER.error("El correo proporcionado es nulo o vacío. No se puede buscar el usuario.");
			throw new IllegalArgumentException("El correo no puede ser nulo o vacío.");
		}

		if (rol == null || rol.getNombre().isEmpty()) {
			LOGGER.error("El rol proporcionado es nulo o vacío.");
			throw new IllegalArgumentException("El rol no puede ser nulo o vacío.");
		}

		// Realizamos la búsqueda dependiendo del rol
		try {
			switch (rol.getNombre().toLowerCase()) {
				case "piloto":
					LOGGER.debug("Buscando piloto con correo: {}", correo);
					var piloto = piloService.findByCorreo(correo);
					if (piloto.isPresent()) {
						LOGGER.info("Piloto encontrado: {}", piloto.get());
						return piloto.get();
					} else {
						LOGGER.error("Piloto no encontrado para el correo: {}", correo);
						throw new IllegalArgumentException("Piloto no encontrado para el correo: " + correo);
					}

				case "pasajero":
					LOGGER.debug("Buscando pasajero con correo: {}", correo);
					var pasajero = pasajeroService.findByCorreoElectronico(correo);
					if (pasajero.isPresent()) {
						LOGGER.info("Pasajero encontrado: {}", pasajero.get());
						return pasajero.get();
					} else {
						LOGGER.error("Pasajero no encontrado para el correo: {}", correo);
						throw new IllegalArgumentException("Pasajero no encontrado para el correo: " + correo);
					}

				case "admin":
					// No necesitas crear un objeto admin, ya que es el mismo usuario que el "admin" en el sistema.
					LOGGER.debug("Rol 'admin' detectado, no es necesario crear un nuevo objeto.");
					return null;  // Se asume que es el mismo usuario ya.

				default:
					LOGGER.error("Rol no válido recibido: {}", rol.getNombre());
					throw new IllegalArgumentException("Rol no válido: " + rol.getNombre());
			}
		} catch (Exception e) {
			LOGGER.error("Error durante la creación del usuario. Correo: {}, Rol: {}", correo, rol.getNombre(), e);
			throw e;  // Re-lanzar la excepción para que la capa superior maneje el error
		}
	}
	
	/*private Collection<? extends GrantedAuthority> mapearAutoridadesAroles(Collection<Ro>){
		
		
	};*/
	
	
	

}
