package com.SkyWay.modules.asiento.presentation.dto;

import java.io.Serializable;
import java.util.Objects;

public class InfoAsientoDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private int idAsiento;
	private String numeroAsiento;
	private String estado;
	private int precio;
	private String clase;
	private String reservadoPor;

	// Campos Espaciales para Cabina Dinámica
	private Integer fila;
	private String letra;
	private Boolean esVentana = false;
	private Boolean esPasillo = false;
	private Boolean esEmergencia = false;

	public InfoAsientoDTO() {}

	public InfoAsientoDTO(int idAsiento, String numeroAsiento, String estado, int precio, String clase,
	                      Integer fila, String letra, Boolean esVentana, Boolean esPasillo, Boolean esEmergencia) {
		this.idAsiento = idAsiento;
		this.numeroAsiento = numeroAsiento;
		this.estado = estado;
		this.precio = precio;
		this.clase = clase;
		this.fila = fila;
		this.letra = letra;
		this.esVentana = esVentana;
		this.esPasillo = esPasillo;
		this.esEmergencia = esEmergencia;
	}

	public InfoAsientoDTO(int idAsiento, String numeroAsiento, String estado, int precio, String clase) {
		this.idAsiento = idAsiento;
		this.numeroAsiento = numeroAsiento;
		this.estado = estado;
		this.precio = precio;
		this.clase = clase;
	}

	public String getEstadoVisual(String miSessionId) {
		if ("OCUPADO".equalsIgnoreCase(this.estado)) {
			return "OCUPADO";
		}
		if ("SELECCIONADO".equalsIgnoreCase(this.estado)) {
			if (miSessionId != null && miSessionId.equals(this.reservadoPor)) {
				return "SELECCIONADO";
			} else {
				return "EN_PROCESO";
			}
		}
		return "libre";
	}

	// --- Getters y Setters ---
	public int getIdAsiento() { return idAsiento; }
	public void setIdAsiento(int idAsiento) { this.idAsiento = idAsiento; }

	public String getNumeroAsiento() { return numeroAsiento; }
	public void setNumeroAsiento(String numeroAsiento) { this.numeroAsiento = numeroAsiento; }

	public String getEstado() { return estado != null ? estado.toLowerCase() : "libre"; }
	public void setEstado(String estado) { this.estado = estado; }

	public int getPrecio() { return precio; }
	public void setPrecio(int precio) { this.precio = precio; }

	public String getClase() { return clase; }
	public void setClase(String clase) { this.clase = clase; }

	public String getReservadoPor() { return reservadoPor; }
	public void setReservadoPor(String reservadoPor) { this.reservadoPor = reservadoPor; }

	public Integer getFila() { return fila; }
	public void setFila(Integer fila) { this.fila = fila; }

	public String getLetra() { return letra; }
	public void setLetra(String letra) { this.letra = letra; }

	public Boolean getEsVentana() { return esVentana; }
	public void setEsVentana(Boolean esVentana) { this.esVentana = esVentana; }

	public Boolean getEsPasillo() { return esPasillo; }
	public void setEsPasillo(Boolean esPasillo) { this.esPasillo = esPasillo; }

	public Boolean getEsEmergencia() { return esEmergencia; }
	public void setEsEmergencia(Boolean esEmergencia) { this.esEmergencia = esEmergencia; }

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		InfoAsientoDTO that = (InfoAsientoDTO) obj;
		return this.idAsiento == that.idAsiento;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idAsiento);
	}
}