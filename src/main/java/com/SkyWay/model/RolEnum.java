package com.SkyWay.model;

public enum RolEnum {

	PASAJERO("Pasajero"),
	PILOTO("Piloto"),
	ADMIN("admin");
	
	private String descripcion;
	
		private RolEnum(String descripcion) {
        this.descripcion = descripcion;
    }

	
	
	public String getDescripcion() {
		return descripcion;
	}
	 
	
}
