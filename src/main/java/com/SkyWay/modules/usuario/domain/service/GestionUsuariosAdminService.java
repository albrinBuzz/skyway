package com.SkyWay.modules.usuario.domain.service;

import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.pasajero.domain.repository.PasajeroRepository;
import com.SkyWay.modules.piloto.domain.model.Piloto;

import com.SkyWay.modules.piloto.domain.repository.PilotoRepositoy;
import com.SkyWay.modules.rolusuario.domain.model.Role;

import com.SkyWay.modules.rolusuario.domain.repository.RolRepository;
import com.SkyWay.modules.tripulacion.domain.model.Tripulacion;
import com.SkyWay.modules.tripulacion.domain.repository.TripulacionRepository;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.usuario.domain.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class GestionUsuariosAdminService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository roleRepository;

    @Autowired
    private PilotoRepositoy pilotoRepository;

    @Autowired
    private TripulacionRepository tripulacionRepository;

    @Autowired
    private PasajeroRepository pasajeroRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional(readOnly = true)
    public List<Usuario> listarTodosConRoles() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long contarUsuariosPaginados(String filtroGlobal) {
        String term = normalizarFiltro(filtroGlobal);
        if (term == null) {
            try {
                Object estObj = entityManager.createNativeQuery(
                        "SELECT reltuples::BIGINT FROM pg_class WHERE relname = 'usuario' LIMIT 1"
                ).getSingleResult();
                long est = estObj != null ? ((Number) estObj).longValue() : -1L;
                if (est > 10_000L) {
                    return est;
                }
            } catch (Exception ignored) {
            }
            Long exact = entityManager.createQuery("SELECT COUNT(u.rut) FROM Usuario u", Long.class).getSingleResult();
            return exact != null ? exact : 0L;
        }
        Long count = entityManager.createQuery(
                        "SELECT COUNT(u.rut) FROM Usuario u WHERE " + construirWhereBusquedaUsuario(), Long.class)
                .setParameter("term", "%" + term + "%")
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarUsuariosPaginados(int first, int pageSize, String filtroGlobal) {
        String term = normalizarFiltro(filtroGlobal);
        StringBuilder jpqlRuts = new StringBuilder("SELECT u.rut FROM Usuario u");
        if (term != null) {
            jpqlRuts.append(" WHERE ").append(construirWhereBusquedaUsuario());
        }
        jpqlRuts.append(" ORDER BY u.fechaRegistro DESC NULLS LAST, u.rut ASC");

        var qRuts = entityManager.createQuery(jpqlRuts.toString(), String.class);
        if (term != null) {
            qRuts.setParameter("term", "%" + term + "%");
        }
        qRuts.setFirstResult(Math.max(0, first));
        qRuts.setMaxResults(Math.max(1, pageSize));
        List<String> ruts = qRuts.getResultList();
        if (ruts == null || ruts.isEmpty()) {
            return new ArrayList<>();
        }

        List<Usuario> cargados = entityManager.createQuery(
                        "SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.rut IN :ruts",
                        Usuario.class)
                .setParameter("ruts", ruts)
                .getResultList();

        Map<String, Usuario> porRut = new LinkedHashMap<>();
        for (Usuario u : cargados) {
            porRut.put(u.getRut(), u);
        }
        List<Usuario> ordenados = new ArrayList<>(ruts.size());
        for (String rut : ruts) {
            Usuario u = porRut.get(rut);
            if (u != null) {
                ordenados.add(u);
            }
        }
        return ordenados;
    }

    private String normalizarFiltro(String filtro) {
        if (filtro == null) return null;
        String clean = filtro.trim().toLowerCase(Locale.ROOT);
        return clean.isEmpty() ? null : clean;
    }

    private String construirWhereBusquedaUsuario() {
        return "(" +
                "LOWER(COALESCE(u.rut, '')) LIKE :term " +
                "OR LOWER(COALESCE(u.nombre, '')) LIKE :term " +
                "OR LOWER(COALESCE(u.apellido, '')) LIKE :term " +
                "OR LOWER(COALESCE(u.correoElectronico, '')) LIKE :term " +
                "OR LOWER(COALESCE(u.documentoIdentidad, '')) LIKE :term" +
                ")";
    }

    @Transactional(readOnly = true)
    public List<Role> listarTodosLosRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorRut(String rut) {
        return usuarioRepository.findById(rut);
    }

    @Transactional(readOnly = true)
    public Optional<Piloto> buscarPilotoPorRut(String rut) {
        return pilotoRepository.findById(rut);
    }

    @Transactional(readOnly = true)
    public Optional<Tripulacion> buscarTripulacionPorRut(String rut) {
        return tripulacionRepository.findById(rut);
    }

    @Transactional(readOnly = true)
    public Optional<Pasajero> buscarPasajeroPorRut(String rut) {
        return pasajeroRepository.findById(rut);
    }

    @Transactional
    public void guardarUsuarioCompleto(Usuario usuario, List<Role> rolesSeleccionados, String contrasenaPlana,
                                       boolean esPiloto, Piloto piloto,
                                       boolean esTripulacion, Tripulacion tripulacion,
                                       boolean esPasajero, Pasajero pasajero) {

        // Encriptar contraseña solo si se especificó una nueva
        if (contrasenaPlana != null && !contrasenaPlana.isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(contrasenaPlana));
        }

        if (usuario.getFechaRegistro() == null) {
            usuario.setFechaRegistro(new Timestamp(System.currentTimeMillis()));
        }

        usuario.setRoles(rolesSeleccionados != null ? rolesSeleccionados : new ArrayList<>());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        String rut = usuarioGuardado.getRut();

        // 1. Manejo Perfil Piloto
        if (esPiloto && piloto != null) {
            piloto.setRut(rut);
            piloto.setUsuario(usuarioGuardado);
            pilotoRepository.save(piloto);
        } else {
            pilotoRepository.findById(rut).ifPresent(p -> pilotoRepository.deleteById(rut));
        }

        // 2. Manejo Perfil Tripulación
        if (esTripulacion && tripulacion != null) {
            tripulacion.setRut(rut);
            tripulacion.setUsuario(usuarioGuardado);
            if (tripulacion.getFechaIngreso() == null) {
                tripulacion.setFechaIngreso(new Timestamp(System.currentTimeMillis()));
            }
            tripulacionRepository.save(tripulacion);
        } else {
            tripulacionRepository.findById(rut).ifPresent(t -> tripulacionRepository.deleteById(rut));
        }

        // 3. Manejo Perfil Pasajero
        if (esPasajero && pasajero != null) {
            pasajero.setRut(rut);
            pasajero.setUsuario(usuarioGuardado);
            if (pasajero.getFechaNacimiento() == null) {
                pasajero.setFechaNacimiento(usuarioGuardado.getFechaNacimiento());
            }
            pasajeroRepository.save(pasajero);
        } else {
            pasajeroRepository.findById(rut).ifPresent(p -> pasajeroRepository.deleteById(rut));
        }
    }

    @Transactional
    public void eliminarUsuarioCompleto(String rut) {
        pilotoRepository.findById(rut).ifPresent(p -> pilotoRepository.deleteById(rut));
        tripulacionRepository.findById(rut).ifPresent(t -> tripulacionRepository.deleteById(rut));
        pasajeroRepository.findById(rut).ifPresent(p -> pasajeroRepository.deleteById(rut));
        usuarioRepository.deleteById(rut);
    }
}