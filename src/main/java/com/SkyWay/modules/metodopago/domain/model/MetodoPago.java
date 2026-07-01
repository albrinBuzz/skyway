package com.SkyWay.modules.metodopago.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.pago.domain.model.Pago;
import jakarta.persistence.*;
import java.util.List;


/**
 * The persistent class for the metodo_pago database table.
 * 
 */
@Entity
@Table(name="metodo_pago")
@NamedQuery(name="MetodoPago.findAll", query="SELECT m FROM MetodoPago m")
public class MetodoPago implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "estado_vuelo_seq")
	@SequenceGenerator(name = "estado_vuelo_seq",sequenceName = "estado_vuelo_seq",allocationSize = 1)
	@Column(name="id_metodo_pago")
	private Integer idMetodoPago;

	private String descripcion;

	//bi-directional many-to-one association to Pago
	@OneToMany(mappedBy="metodoPago")
	private List<Pago> pagos;

	public MetodoPago() {
	}

	public Integer getIdMetodoPago() {
		return this.idMetodoPago;
	}

	public void setIdMetodoPago(Integer idMetodoPago) {
		this.idMetodoPago = idMetodoPago;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public List<Pago> getPagos() {
		return this.pagos;
	}

	public void setPagos(List<Pago> pagos) {
		this.pagos = pagos;
	}

	public Pago addPago(Pago pago) {
		getPagos().add(pago);
		pago.setMetodoPago(this);

		return pago;
	}

	public Pago removePago(Pago pago) {
		getPagos().remove(pago);
		pago.setMetodoPago(null);

		return pago;
	}

}