package com.SkyWay.dto;



public class InfoAsientoDTO {
    private int idAsiento;
    private String numeroAsiento;
    private String estado;
	private int precio;
	private String clase;
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
		return estado;
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


