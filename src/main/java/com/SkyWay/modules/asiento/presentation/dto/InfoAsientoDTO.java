package com.SkyWay.modules.asiento.presentation.dto;


import java.util.Objects;

public class InfoAsientoDTO {
    private int idAsiento;
    private String numeroAsiento;
    private String estado;
	private int precio;
	private String clase;
	private String reservadoPor;  // sessionId que tiene el lock temporal (null si libre/ocupado)

	// Constructor


    public InfoAsientoDTO() {
		// TODO Auto-generated constructor stub
	}

	public InfoAsientoDTO(int idAsiento, String numeroAsiento, String estado, int precio, String clase) {
		this.idAsiento = idAsiento;
		this.numeroAsiento = numeroAsiento;
		this.estado = estado;
		this.precio = precio;
		this.clase = clase;
	}
	/**
	 * Estado VISUAL relativo a quién está mirando: el mismo dato crudo
	 * "SELECCIONADO" se interpreta distinto si el sessionId coincide o no.
	 */
	public String getEstadoVisual(String miSessionId) {
		if ("OCUPADO".equalsIgnoreCase(estado)) return "OCUPADO";
		if ("SELECCIONADO".equalsIgnoreCase(estado)) {
			if (reservadoPor != null && reservadoPor.equals(miSessionId)) {
				return "SELECCIONADO"; // lo elegí yo
			}
			return "EN_PROCESO"; // lo eligió otro usuario, temporalmente
		}
		return "DISPONIBLE";
	}

	public int getIdAsiento() {
		return idAsiento;
	}

	public void setIdAsiento(int idAsiento) {
		this.idAsiento = idAsiento;
	}

	public String getNumeroAsiento() {
		return numeroAsiento;
	}

	public void setNumeroAsiento(String numeroAsiento) {
		this.numeroAsiento = numeroAsiento;
	}

	public String getEstado() {
		return estado.toLowerCase();
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public int getPrecio() {
		return precio;
	}

	public void setPrecio(int precio) {
		this.precio = precio;
	}

	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
	}
	public String getReservadoPor() { return reservadoPor; }
	public void setReservadoPor(String reservadoPor) { this.reservadoPor = reservadoPor; }


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
	@Override
	public String toString() {
		return "InfoAsientoDTO{" +
				"idAsiento=" + idAsiento +
				", numeroAsiento='" + numeroAsiento + '\'' +
				", estado='" + estado + '\'' +
				", precio=" + precio +
				", clase='" + clase + '\'' +
				'}';
	}
}


