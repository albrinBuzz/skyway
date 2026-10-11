package com.SkyWay.modules.reserva.domain.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.SkyWay.dto.BoletoDTO;
import com.SkyWay.dto.ReservaVueloDTO;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.domain.model.Reserva;

import com.SkyWay.modules.reserva.presentation.dto.TicketInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservaService {

	  public List<Reserva> findAll();
	  public Optional<Reserva> findById(Integer id);	  
	  public Reserva save(Reserva reserva);
	  public void deleteById(Integer id);
	  public List<ReservaVueloDTO>getReservasUsuario(String rut);
	  public void cancelarReserva(Integer id);
	  public BoletoDTO getBoleto(Integer id);
	  public List<TicketInfo>getTicket(String rut,Integer idReserva,int idItinerario);
	  List<Reserva> obtenerReservasPorPasajero(Pasajero pasajero);
	  List<Reserva> obtenerReservasPorRut(String rut);
		Page<Reserva> obtenerReservasPorRut(String rut, Pageable pageable);
	void cancelarReserva(Integer idReserva, String rutPasajero, String motivo);
	void procesarCheckInWeb(Integer idReserva, Integer idItinerario);

	/**
	 * Obtiene la fecha y hora de salida programada del primer vuelo/segmento asignado a un itinerario.
	 * @param idItinerario Identificador del itinerario de vuelo
	 * @return Date con la fecha y hora de salida, o null si no se encuentra.
	 */
	Date obtenerFechaSalidaVuelo(Integer idItinerario);

	public Integer buscarIdPorPnrYPasajero(String pnr, String apellidoRut);
	Reserva guardarOGenerarPnr(Integer idReserva);
	/**
	 * Busca el ID numérico de la reserva a partir de su código único (ej: "SKW-1234" o id en string).
	 */
	Integer buscarIdPorCodigo(String codigoReserva);

	/**
	 * Obtiene el ID del itinerario asociado a una reserva dada.
	 */
	Integer obtenerIdItinerarioPorReserva(Integer idReserva);
	String confirmarReserva(int idVuelo, Integer[] asientosArray, String rutUsuario,Integer idReserva) throws SQLException;
}
