package com.SkyWay.model;

import java.io.Serializable;

import jakarta.persistence.*;

/*@Entity
@Table(name = "precio_asiento")*/
public class PrecioAsiento implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "precio_asiento_seq")
	@SequenceGenerator(name = "precio_asiento_seq",sequenceName = "precio_asiento_seq",allocationSize = 1)
    @Column(name = "id_precio_asiento")
    private Integer idPrecioAsiento;
    private Integer precio;
    
    @ManyToOne
    @JoinColumn(name = "id_vuelo")
    private Vuelo vuelo;  // Relación con la tabla Vuelo
 
    @ManyToOne
    @JoinColumn(name = "ID_CLASE")
    private ClaseAsiento claseAsiento;  // Relación con la tabla Clase_asiento

    // Constructor por defecto
    public PrecioAsiento() {
    }

    // Getters y setters
    public Integer getIdPrecioAsiento() {
        return idPrecioAsiento;
    }

    public void setIdPrecioAsiento(Integer idPrecioAsiento) {
        this.idPrecioAsiento = idPrecioAsiento;
    }

    public Integer getPrecio() {
        return precio;
    }

    public void setPrecio(Integer precio) {
        this.precio = precio;
    }

	public Vuelo getVuelo() {
		return vuelo;
	}

	public void setVuelo(Vuelo vuelo) {
		this.vuelo = vuelo;
	}

	public ClaseAsiento getClaseAsiento() {
		return claseAsiento;
	}

	public void setClaseAsiento(ClaseAsiento claseAsiento) {
		this.claseAsiento = claseAsiento;
	}

	@Override
	public String toString() {
		return "PrecioAsiento [idPrecioAsiento=" + idPrecioAsiento + ", precio=" + precio + ", vuelo=" + vuelo
				+ ", claseAsiento=" + claseAsiento + "]";
	}


    
  
}
