package com.SkyWay.dto;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class InfoVueloDTO {
		private String destino;
		private int idAvion;
	    private int idVuelo;
	    private String numeroVuelo;
	    private String ciudadSalida;
	    private String ciudadLlegada;
	    private Timestamp fechaHoraSalida;
	    private Timestamp fechaHoraLlegada;
	    private String precio;
	    private String modeloAvion;
	    private String duracion;
	    
	    public InfoVueloDTO() {
			// TODO Auto-generated constructor stub
		}

			public InfoVueloDTO(String destino, int idAvion, int idVuelo, String numeroVuelo, String ciudadSalida,
				String ciudadLlegada, Timestamp fechaHoraSalida, Timestamp fechaHoraLlegada, String precio,
				String modeloAvion, String duracion) {
			super();
			this.destino = destino;
			this.idAvion = idAvion;
			this.idVuelo = idVuelo;
			this.numeroVuelo = numeroVuelo;
			this.ciudadSalida = ciudadSalida;
			this.ciudadLlegada = ciudadLlegada;
			this.fechaHoraSalida = fechaHoraSalida;
			this.fechaHoraLlegada = fechaHoraLlegada;
			this.precio = precio;
			this.modeloAvion = modeloAvion;
			this.duracion = duracion;
		}


			public String getDestino() {
				return destino;
			}


			public void setDestino(String destino) {
				this.destino = destino;
			}


			public int getIdAvion() {
				return idAvion;
			}




			public void setIdAvion(int idAvion) {
				this.idAvion = idAvion;
			}

			public int getIdVuelo() {
				return idVuelo;
			}


			public void setIdVuelo(int idVuelo) {
				this.idVuelo = idVuelo;
			}


			public String getNumeroVuelo() {
				return numeroVuelo;
			}


			public void setNumeroVuelo(String numeroVuelo) {
				this.numeroVuelo = numeroVuelo;
			}




			public String getCiudadSalida() {
				return ciudadSalida;
			}




			public void setCiudadSalida(String ciudadSalida) {
				this.ciudadSalida = ciudadSalida;
			}


			public String getCiudadLlegada() {
				return ciudadLlegada;
			}




			public void setCiudadLlegada(String ciudadLlegada) {
				this.ciudadLlegada = ciudadLlegada;
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



			public String getPrecio() {
				return precio;
			}




			public void setPrecio(String precio) {
				this.precio = precio;
			}




			public String getModeloAvion() {
				return modeloAvion;
			}




			public void setModeloAvion(String modeloAvion) {
				this.modeloAvion = modeloAvion;
			}




			public String getDuracion() {
				return duracion;
			}




			public void setDuracion(String duracion) {
				this.duracion = duracion;
			}


		

		  

		// Método para formatear la fecha
	    public String getFormattedFechaHoraSalida() {
	        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	        return fechaHoraSalida != null ? sdf.format(fechaHoraSalida) : "";
	    }

	    public String getFormattedFechaHoraLlegada() {
	        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	        return fechaHoraLlegada != null ? sdf.format(fechaHoraLlegada) : "";
	    }


	    @Override
	    public String toString() {
	        return "InfoVueloDTO [numeroVuelo=" + numeroVuelo + ", ciudadSalida=" + ciudadSalida + ", ciudadLlegada="
	                + ciudadLlegada + ", fechaHoraSalida=" + getFormattedFechaHoraSalida() + ", fechaHoraLlegada="
	                + getFormattedFechaHoraLlegada() + ", duracion=" + duracion + "]";
	    }


 
	    

}
