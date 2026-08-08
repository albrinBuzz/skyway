package com.SkyWay.modules.rolusuario.domain.model;

import java.io.Serializable;



import com.SkyWay.modules.usuario.domain.model.Usuario;
import jakarta.persistence.*;


/**
 * The persistent class for the rolusuario database table.
 * 
 */
@Entity
@NamedQuery(name="Rolusuario.findAll", query="SELECT r FROM Rolusuario r")
public class Rolusuario implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rolusuario_id_seq")
	@SequenceGenerator(name = "rolusuario_id_seq", sequenceName = "rolusuario_id_seq", allocationSize = 1)
	@Column(name = "id_rol_usuario")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rut_usuario", referencedColumnName = "RUT")  // Cambiar aquí a "RUT"
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_rol", referencedColumnName = "id_rol")
	private Role role;

	public Rolusuario() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Role getRole() {
		return this.role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

}