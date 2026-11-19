package com.SkyWay.modules.usuario.domain.model;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Entidad JPA que almacena los hashes SHA-256 de tokens criptográficos de un solo uso
 * para la recuperación segura de contraseñas.
 */
@Entity
@Table(name = "token_recuperacion")
public class TokenRecuperacion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "token_recuperacion_gen")
    @SequenceGenerator(
            name = "token_recuperacion_gen",
            sequenceName = "token_recuperacion_seq",
            allocationSize = 1
    )
    @Column(name = "id_token", nullable = false)
    private Long idToken;

    @Column(name = "rut_usuario", nullable = false, length = 12)
    private String rutUsuario;

    @Column(name = "correo_electronico", nullable = false, length = 100)
    private String correoElectronico;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private OffsetDateTime fechaExpiracion;

    @Column(name = "usado", nullable = false)
    private boolean usado;

    @Column(name = "revocado", nullable = false)
    private boolean revocado;

    @Column(name = "fecha_uso")
    private OffsetDateTime fechaUso;

    @Column(name = "ip_solicitud", length = 45)
    private String ipSolicitud;

    @Column(name = "ip_uso", length = 45)
    private String ipUso;

    public Long getIdToken() {
        return idToken;
    }

    public void setIdToken(Long idToken) {
        this.idToken = idToken;
    }

    public String getRutUsuario() {
        return rutUsuario;
    }

    public void setRutUsuario(String rutUsuario) {
        this.rutUsuario = rutUsuario;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public OffsetDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(OffsetDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public boolean isUsado() {
        return usado;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }

    public boolean isRevocado() {
        return revocado;
    }

    public void setRevocado(boolean revocado) {
        this.revocado = revocado;
    }

    public OffsetDateTime getFechaUso() {
        return fechaUso;
    }

    public void setFechaUso(OffsetDateTime fechaUso) {
        this.fechaUso = fechaUso;
    }

    public String getIpSolicitud() {
        return ipSolicitud;
    }

    public void setIpSolicitud(String ipSolicitud) {
        this.ipSolicitud = ipSolicitud;
    }

    public String getIpUso() {
        return ipUso;
    }

    public void setIpUso(String ipUso) {
        this.ipUso = ipUso;
    }
}
