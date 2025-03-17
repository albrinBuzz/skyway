package com.SkyWay.dto;

import java.sql.Timestamp;

public class ReservaVueloDTO {

	 
    private int idVuelo;
    private int id_reserva;
    private String numeroVuelo;
    private Timestamp fechaHoraSalida;
    private Timestamp fechaHoraLlegada;
    private String aeropuertoSalida;
    private String aeropuertoLlegada;
    private Timestamp fechaReserva;
    private String estadoReserva;
    private int precio;

    public ReservaVueloDTO() {
		// TODO Auto-generated constructor stub
	}

	public ReservaVueloDTO(int idVuelo, int id_reserva, String numeroVuelo, Timestamp fechaHoraSalida,
			Timestamp fechaHoraLlegada, String aeropuertoSalida, String aeropuertoLlegada, Timestamp fechaReserva,
			String estadoReserva, int precio) {
		super();
		this.idVuelo = idVuelo;
		this.id_reserva = id_reserva;
		this.numeroVuelo = numeroVuelo;
		this.fechaHoraSalida = fechaHoraSalida;
		this.fechaHoraLlegada = fechaHoraLlegada;
		this.aeropuertoSalida = aeropuertoSalida;
		this.aeropuertoLlegada = aeropuertoLlegada;
		this.fechaReserva = fechaReserva;
		this.estadoReserva = estadoReserva;
		this.precio = precio;
	}

	public int getIdVuelo() {
		return idVuelo;
	}

	public void setIdVuelo(int idVuelo) {
		this.idVuelo = idVuelo;
	}

	public int getId_reserva() {
		return id_reserva;
	}

	public void setId_reserva(int id_reserva) {
		this.id_reserva = id_reserva;
	}

	public String getNumeroVuelo() {
		return numeroVuelo;
	}

	public void setNumeroVuelo(String numeroVuelo) {
		this.numeroVuelo = numeroVuelo;
	}

	public Timestamp getFechaHoraSalida() {
		return fechaHoraSalida;
	}

	public void setFechaHoraSalida(Timestamp fechaHoraSalida) {
		this.fechaHoraSalida = fechaHoraSalida;
	}

	public Timestamp getFechaHoraLlegada() {
		return fechaHoraLlegada;
	}

	public void setFechaHoraLlegada(Timestamp fechaHoraLlegada) {
		this.fechaHoraLlegada = fechaHoraLlegada;
	}

	public String getAeropuertoSalida() {
		return aeropuertoSalida;
	}

	public void setAeropuertoSalida(String aeropuertoSalida) {
		this.aeropuertoSalida = aeropuertoSalida;
	}

	public String getAeropuertoLlegada() {
		return aeropuertoLlegada;
	}

	public void setAeropuertoLlegada(String aeropuertoLlegada) {
		this.aeropuertoLlegada = aeropuertoLlegada;
	}

	public Timestamp getFechaReserva() {
		return fechaReserva;
	}

	public void setFechaReserva(Timestamp fechaReserva) {
		this.fechaReserva = fechaReserva;
	}

	public String getEstadoReserva() {
		return estadoReserva;
	}

	public void setEstadoReserva(String estadoReserva) {
		this.estadoReserva = estadoReserva;
	}

	public int getPrecio() {
		return precio;
	}

	public void setPrecio(int precio) {
		this.precio = precio;
	}

    
    
	
    
	
}
