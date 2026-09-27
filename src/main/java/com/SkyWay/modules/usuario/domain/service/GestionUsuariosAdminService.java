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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GestionUsuariosAdminService {

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