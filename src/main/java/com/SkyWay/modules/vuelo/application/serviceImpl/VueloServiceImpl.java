package com.SkyWay.modules.vuelo.application.serviceImpl;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.domain.repository.AeropuertoRepository;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaDTO;
import com.SkyWay.modules.asignacionpuerta.domain.model.AsignacionPuerta;
import com.SkyWay.modules.asignacionpuerta.domain.repository.AsignacionPuertaRepository;
import com.SkyWay.modules.estadovuelo.domain.model.EstadoVuelo;
import com.SkyWay.modules.estadovuelo.domain.repository.EstadoVueloRepository;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.itinerariovuelo.domain.repository.ItinerarioVueloRepository;
import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.puertaembarque.domain.model.PuertaEmbarque;
import com.SkyWay.modules.puertaembarque.domain.repository.PuertaEmbarqueRepository;
import com.SkyWay.modules.reservaasiento.domain.repository.ReservaAsientoRepository;
import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import com.SkyWay.modules.segmentovuelo.domain.repository.SegmentoVueloRepository;
import com.SkyWay.modules.segmentovuelo.presentation.dto.SegmentoMapaProjection;
import com.SkyWay.modules.turno.domain.model.Turno;
import com.SkyWay.modules.turno.domain.repository.TurnoRepository;
import com.SkyWay.modules.turnotripulacion.domain.model.TurnoTripulacion;
import com.SkyWay.modules.turnotripulacion.domain.repository.TurnoTripulacionRepository;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.repository.VueloRepository;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.modules.vuelo.presentation.dto.ItinerarioImpactoDTO;
import com.SkyWay.modules.vuelo.presentation.dto.ResultadoReprogramacionDTO;
import com.SkyWay.modules.vuelo.presentation.dto.VueloEstadoProjection;
import com.SkyWay.modules.vuelo.presentation.dto.VueloMapaDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.dto.InfoVueloDTO;


import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;


@Service

public class VueloServiceImpl implements VueloService {

	private final Logger LOGGER = LoggerFactory.getLogger(VueloServiceImpl.class);
	
    @Autowired
    private VueloRepository vueloRepository;
    
    @PersistenceContext
    private EntityManager em;
	@Autowired
	private AeropuertoRepository aeropuertoRepository;
	@Autowired
	private SegmentoVueloRepository segmentoVueloRepository;
	@Autowired
	private ReservaAsientoRepository reservaAsientoRepository;
	@Autowired
	private AsignacionPuertaRepository asignacionPuertaRepository;
	@Autowired
	private PuertaEmbarqueRepository puertaEmbarqueRepository;
	@Autowired
	private TurnoRepository turnoRepository;
	@Autowired
	private TurnoTripulacionRepository turnoTripulacionRepository;
	@Autowired
	private ItinerarioVueloRepository itinerarioVueloRepository;
	@Autowired
	private EstadoVueloRepository estadoVueloRepository;

	private static final long MIN_ESCALA_MINUTOS = 45L;
	private static final long VENTANA_PUERTA_MINUTOS = 60L;
	private static final long BUFFER_TURNAROUND_MINUTOS = 60L;
	private static final long HORAS_VENTANA_CHECKIN = 48L;
	private static final DateTimeFormatter FMT_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private final ObjectMapper auditObjectMapper = new ObjectMapper();

    @Override
    public List<Vuelo> findAll() {
        return vueloRepository.findAll();
    }
    @Override
    public Optional<Vuelo> findById(Integer id) {
        return vueloRepository.findById(id);
    }
    
    @Override
    public Vuelo save(Vuelo vuelo) {
		//vueloRepository.saveAndFlush(vuelo);
		return vueloRepository.saveAndFlush(vuelo);
    }

    @Override
    public void deleteById(Integer id) {
        vueloRepository.deleteById(id);
    }



	@Override
	@Transactional
	public List<Vuelo> buscarVuelo(String departureCity, String arrivalCity, String departureDate) {
		String sql;
		Query query;

		if (departureDate != null && !departureDate.isEmpty()) {
			sql = "SELECT * FROM public.FnbuscarVuelos(:ciudadSalida, :ciudadLlegada, :fechaIn)";
			query = em.createNativeQuery(sql, Vuelo.class);
			query.setParameter("fechaIn", java.sql.Date.valueOf(LocalDate.parse(departureDate)));
		} else {
			sql = "SELECT * FROM public.FnbuscarVuelos(:ciudadSalida, :ciudadLlegada)";
			query = em.createNativeQuery(sql, Vuelo.class);
		}

		query.setParameter("ciudadSalida", departureCity);
		query.setParameter("ciudadLlegada", arrivalCity);

		List<Vuelo> vuelos = query.getResultList();


		return vuelos;
	}

	@Override
	@Transactional
	public List<InfoVueloDTO> vuelosProximos() {

        Query query = em.createNativeQuery(
        		"select * from fn_VuelosProximos()",
          		    InfoVueloDTO.class);
        			
            		//LOGGER.info("Resultado {}",query.getResultList());
            		List<InfoVueloDTO> vuelos= query.getResultList();
            		
   		return vuelos;
	}

	@Override
	public InfoVueloDTO getInfoVuelo(int idVuelo) {
		 Query query = em.createNativeQuery(
	        		"select * from fn_getVueloInfo(:idVuelo);",
	          		    InfoVueloDTO.class);
		
		 query.setParameter("idVuelo", idVuelo); 
		 
		InfoVueloDTO vuelo=(InfoVueloDTO) query.getSingleResult();
		
		return vuelo;
	}

	@Override
	public List<Vuelo> findByPiloto(Piloto piloto) {
		// TODO Auto-generated method stub
		List<Vuelo> vuelos=vueloRepository.findByPiloto(piloto);
		//System.out.println(vuelos);
		return vuelos;
	}

	@Override
	public Vuelo updateVuelo(Vuelo vuelo) {
		return vueloRepository.save(vuelo);
	}

	public List<AeropuertoMapaDTO> findAllParaMapa() {
		return aeropuertoRepository.findAllConCoordenadas().stream()
				.map(p -> new AeropuertoMapaDTO(
						p.getCodigoIata(), p.getNombreAeropuerto(), p.getCiudad(),
						p.getLatitud(), p.getLongitud()))
				.toList();
	}

	public VueloMapaDTO construirGeometriaVuelo(Vuelo vuelo) {
		List<SegmentoMapaProjection> filas = segmentoVueloRepository.findGeometriaPorVuelo(vuelo.getIdVuelo());
		if (filas.isEmpty()) return null;

		List<VueloMapaDTO.PuntoSegmentoDTO> puntos = new ArrayList<>();

		for (int i = 0; i < filas.size(); i++) {
			SegmentoMapaProjection seg = filas.get(i);

			// Origen del segmento: solo lo agregamos si es el primero,
			// o si no coincide con el destino del segmento anterior (evita duplicados en escalas)
			if (i == 0) {
				puntos.add(new VueloMapaDTO.PuntoSegmentoDTO(
						seg.getIataOrigen(), seg.getNombreOrigen(),
						seg.getLatOrigen(), seg.getLngOrigen(), "ORIGEN"));
			}

			boolean esUltimo = (i == filas.size() - 1);
			puntos.add(new VueloMapaDTO.PuntoSegmentoDTO(
					seg.getIataDestino(), seg.getNombreDestino(),
					seg.getLatDestino(), seg.getLngDestino(),
					esUltimo ? "DESTINO" : "ESCALA"));
		}

		return new VueloMapaDTO(vuelo.getIdVuelo(), vuelo.getNumeroVuelo(), puntos);
	}


	public List<VueloMapaDTO> construirGeometriaVuelos(List<Vuelo> vuelos) {
		List<VueloMapaDTO> resultado = new ArrayList<>();
		for (Vuelo v : vuelos) {
			VueloMapaDTO dto = construirGeometriaVuelo(v);
			if (dto != null) resultado.add(dto);
		}
		return resultado;
	}


	@Transactional(readOnly = true)
	public List<VueloEstadoProjection> obtenerEstadoVuelosEnVivo(String numeroVuelo, String ruta) {
		String numClean = (numeroVuelo != null && !numeroVuelo.isBlank()) ? numeroVuelo.trim() : null;
		String rutaClean = (ruta != null && !ruta.isBlank()) ? ruta.trim() : null;
		return vueloRepository.buscarEstadoVuelosVivo(numClean, rutaClean);
	}

	@Override
	public boolean existeConflictoPiloto(String rutPiloto, Timestamp salida, Timestamp llegada, Integer idVueloActual) {
		return vueloRepository.existeConflictoPiloto(rutPiloto, salida, llegada, idVueloActual);
	}

	@Override
	public boolean existeConflictoAvion(Integer idAvion, Timestamp salida, Timestamp llegada, Integer idVueloActual) {
		return vueloRepository.existeConflictoAvion(idAvion, salida, llegada, idVueloActual);
	}

	@Override
	public boolean esVueloCancelado(Vuelo vuelo) {
		if (vuelo == null || vuelo.getEstadoVuelo() == null) {
			return false;
		}
		EstadoVuelo ev = vuelo.getEstadoVuelo();
		String desc = ev.getDescripcion() != null ? ev.getDescripcion().trim().toUpperCase() : "";
		String est = ev.getEstado() != null ? ev.getEstado().trim().toUpperCase() : "";
		return desc.contains("CANCEL") || est.contains("CANCEL") || "INACTIVO".equals(est);
	}

	@Override
	public boolean esVueloInmutablePorEstado(Vuelo vuelo) {
		if (vuelo == null || vuelo.getEstadoVuelo() == null) {
			return false;
		}
		if (esVueloCancelado(vuelo)) {
			return true;
		}
		EstadoVuelo ev = vuelo.getEstadoVuelo();
		String desc = ev.getDescripcion() != null ? ev.getDescripcion().trim().toUpperCase() : "";
		String est = ev.getEstado() != null ? ev.getEstado().trim().toUpperCase() : "";
		return desc.contains("EN VUELO")
				|| desc.contains("ATERRIZ")
				|| desc.contains("FINALIZ")
				|| "FINALIZADO".equals(est);
	}

	@Override
	public long contarAsientosVendidosVuelo(Integer idVuelo) {
		if (idVuelo == null || idVuelo <= 0) {
			return 0L;
		}
		return reservaAsientoRepository.countByVuelo_IdVuelo(idVuelo);
	}

	@Override
	@Transactional(readOnly = true)
	public ResultadoReprogramacionDTO validarReprogramacionVuelo(Integer idVuelo,
																Timestamp nuevaSalida,
																Timestamp nuevaLlegada,
																Integer idPuertaSeleccionada) {
		ResultadoReprogramacionDTO res = new ResultadoReprogramacionDTO();
		if (idVuelo == null || idVuelo <= 0) {
			res.getErrores().add("El vuelo debe estar guardado en base de datos para poder reprogramarlo.");
			res.setValido(false);
			return res;
		}

		Vuelo vuelo = vueloRepository.findById(idVuelo).orElse(null);
		if (vuelo == null) {
			res.getErrores().add("No se encontró el vuelo solicitado (ID #" + idVuelo + ").");
			res.setValido(false);
			return res;
		}

		if (esVueloInmutablePorEstado(vuelo)) {
			String est = vuelo.getEstadoVuelo() != null ? vuelo.getEstadoVuelo().getDescripcion() : "Inmutable";
			res.getErrores().add("El vuelo se encuentra en estado '" + est + "' y no admite reprogramación.");
			res.setValido(false);
			return res;
		}

		if (nuevaSalida == null || nuevaLlegada == null) {
			res.getErrores().add("Debe ingresar las nuevas fechas y horas de salida y llegada.");
			res.setValido(false);
			return res;
		}

		LocalDateTime salidaLdt = nuevaSalida.toLocalDateTime();
		LocalDateTime llegadaLdt = nuevaLlegada.toLocalDateTime();

		long vendidos = contarAsientosVendidosVuelo(idVuelo);
		long reservasActivas = contarReservasActivasPorVuelo(idVuelo);
		long checkinsCompletados = contarCheckinsCompletadosPorVuelo(idVuelo);
		res.setAsientosVendidos(vendidos);
		res.setReservasActivas(reservasActivas);
		res.setCheckinsCompletados(checkinsCompletados);

		if (!llegadaLdt.isAfter(salidaLdt)) {
			res.getErrores().add("La fecha/hora de llegada debe ser posterior a la de salida.");
		}
		if (salidaLdt.isBefore(LocalDateTime.now().minusMinutes(5))) {
			res.getErrores().add("La nueva fecha/hora de salida no puede ubicarse en el pasado.");
		}

		List<SegmentoVuelo> segmentos = segmentoVueloRepository.buscarPorVuelo(idVuelo);
		if (segmentos == null || segmentos.isEmpty()) {
			res.getErrores().add("El vuelo no tiene segmentos configurados en base de datos.");
			res.setValido(false);
			return res;
		}
		if (segmentos.size() > 1) {
			res.getErrores().add("La reprogramación automática solo aplica a vuelos de tramo directo (1 segmento).");
			res.setValido(false);
			return res;
		}

		SegmentoVuelo seg = segmentos.get(0);
		if (llegadaLdt.isAfter(salidaLdt)) {
			validarDuracionFisica(seg, salidaLdt, llegadaLdt, res);
			validarReglaAdelantoYCheckIn(seg, salidaLdt, vendidos, checkinsCompletados, res);
			validarConflictosAeronaveYPiloto(vuelo, salidaLdt, llegadaLdt, res);
			validarConflictosTripulacionYTurnos(idVuelo, salidaLdt, llegadaLdt, res);
			validarConflictosPuerta(seg, idVuelo, nuevaSalida, idPuertaSeleccionada, res);
			validarEscalasItinerarios(idVuelo, salidaLdt, llegadaLdt, res);
			validarCoherenciaIdaVueltaReservas(idVuelo, salidaLdt, llegadaLdt, res);
		} else {
			validarEscalasItinerarios(idVuelo, salidaLdt, llegadaLdt, res);
		}

		if (vendidos > 0 || reservasActivas > 0) {
			res.getAdvertencias().add(String.format(
					"Impacto en pasajeros: %d asiento(s) vendido(s) en %d reserva(s) activa(s). Se notificará por correo y en bandeja a titulares y acompañantes.",
					vendidos, reservasActivas
			));
		}
		if (checkinsCompletados > 0) {
			res.getAdvertencias().add(
					"Atención Check-in: existen " + checkinsCompletados + " check-in(s) completado(s). Los pasajeros deberán re-descargar su pase de abordar actualizado."
			);
		}
		if (res.getItinerariosEvaluados() > 0 && res.getErrores().isEmpty()) {
			res.getAdvertencias().add(
					"Se recalcularán automáticamente " + res.getItinerariosEvaluados() + " itinerario(s) vinculado(s) manteniendo escalas >= " + MIN_ESCALA_MINUTOS + " min."
			);
		}
		if (res.getTurnosEvaluados() > 0 && res.getErrores().isEmpty()) {
			res.getAdvertencias().add(
					"Se actualizarán automáticamente " + res.getTurnosEvaluados() + " turno(s) de tripulación respetando un margen mínimo de " + BUFFER_TURNAROUND_MINUTOS + " min."
			);
		}

		res.setValido(res.getErrores().isEmpty());
		res.setResumenMensaje(res.isValido()
				? "Horario verificado: sin choques en aeronave, comandante, turnos, puertas, escalas ni reservas ida/vuelta."
				: "Se detectaron " + res.getErrores().size() + " conflicto(s) bloqueante(s) para el horario propuesto.");
		return res;
	}

	@Override
	@Transactional
	public ResultadoReprogramacionDTO reprogramarHorarioVuelo(Integer idVuelo,
															  Timestamp nuevaSalida,
															  Timestamp nuevaLlegada,
															  Integer idPuertaSeleccionada,
															  boolean marcarRetrasado,
															  String motivo,
															  String rutUsuarioAdmin) {
		ResultadoReprogramacionDTO res = validarReprogramacionVuelo(idVuelo, nuevaSalida, nuevaLlegada, idPuertaSeleccionada);
		if (!res.isValido()) {
			return res;
		}
		if (motivo == null || motivo.isBlank()) {
			res.setValido(false);
			res.getErrores().add("Debe ingresar un motivo operacional para la reprogramación.");
			return res;
		}

		Vuelo vuelo = vueloRepository.findById(idVuelo).orElseThrow();
		SegmentoVuelo seg = segmentoVueloRepository.buscarPorVuelo(idVuelo).get(0);

		String salidaAnterior = seg.getHoraSalida() != null ? seg.getHoraSalida().toLocalDateTime().format(FMT_FECHA_HORA) : null;
		String llegadaAnterior = seg.getHoraLlegada() != null ? seg.getHoraLlegada().toLocalDateTime().format(FMT_FECHA_HORA) : null;
		String estadoAnterior = vuelo.getEstadoVuelo() != null ? vuelo.getEstadoVuelo().getDescripcion() : "Programado";

		List<AsignacionPuerta> apsActuales = asignacionPuertaRepository.findBySegmentoVuelo(seg);
		Integer idPuertaAnterior = (!apsActuales.isEmpty() && apsActuales.get(0).getPuertaEmbarque() != null)
				? apsActuales.get(0).getPuertaEmbarque().getIdPuerta()
				: null;

		if (idPuertaSeleccionada != null && !idPuertaSeleccionada.equals(idPuertaAnterior)) {
			PuertaEmbarque puertaNueva = puertaEmbarqueRepository.findById(idPuertaSeleccionada)
					.orElseThrow(() -> new IllegalArgumentException("Puerta seleccionada no encontrada."));
			AsignacionPuerta ap = apsActuales.isEmpty() ? new AsignacionPuerta() : apsActuales.get(0);
			ap.setSegmentoVuelo(seg);
			ap.setPuertaEmbarque(puertaNueva);
			ap.setHoraAsignacion(new Timestamp(System.currentTimeMillis()));
			asignacionPuertaRepository.saveAndFlush(ap);
		}

		// Actualizar segmento dispara en BD trg_set_fecha_vuelo, trg_vuelo_hacia_itinerario,
		// trg_actualizar_fechas_turno y tr_notificacionVueloEstado.
		seg.setHoraSalida(nuevaSalida);
		seg.setHoraLlegada(nuevaLlegada);
		segmentoVueloRepository.saveAndFlush(seg);

		em.refresh(vuelo);

		String estadoNuevo = estadoAnterior;
		if (marcarRetrasado) {
			EstadoVuelo estadoRetrasado = buscarEstadoRetrasado();
			if (estadoRetrasado != null
					&& (vuelo.getEstadoVuelo() == null || !estadoRetrasado.getIdEstadoVuelo().equals(vuelo.getEstadoVuelo().getIdEstadoVuelo()))) {
				vuelo.setEstadoVuelo(estadoRetrasado);
				vueloRepository.saveAndFlush(vuelo);
				estadoNuevo = estadoRetrasado.getDescripcion();
			}
		}

		registrarAuditoriaReprogramacion(
				vuelo,
				rutUsuarioAdmin,
				motivo.trim(),
				salidaAnterior,
				llegadaAnterior,
				nuevaSalida.toLocalDateTime().format(FMT_FECHA_HORA),
				nuevaLlegada.toLocalDateTime().format(FMT_FECHA_HORA),
				idPuertaAnterior,
				idPuertaSeleccionada != null ? idPuertaSeleccionada : idPuertaAnterior,
				estadoAnterior,
				estadoNuevo,
				res
		);

		res.setEjecutado(true);
		res.setResumenMensaje("Vuelo " + (vuelo.getNumeroVuelo() != null ? vuelo.getNumeroVuelo() : "#" + idVuelo)
				+ " reprogramado correctamente. Itinerarios, turnos y notificaciones sincronizados.");
		return res;
	}

	private void validarDuracionFisica(SegmentoVuelo seg,
									   LocalDateTime salidaLdt,
									   LocalDateTime llegadaLdt,
									   ResultadoReprogramacionDTO res) {
		long minutosReales = Duration.between(salidaLdt, llegadaLdt).toMinutes();
		if (Duration.between(salidaLdt, llegadaLdt).toHours() > 18) {
			res.getErrores().add("Un segmento directo no puede exceder las 18 horas continuas de vuelo.");
			return;
		}
		Aeropuerto origen = seg.getAeropuertoOrigen();
		Aeropuerto destino = seg.getAeropuertoDestino();
		if (origen != null && destino != null && origen.getPosicion() != null && destino.getPosicion() != null) {
			double distanciaKm = origen.getPosicion().distance(destino.getPosicion()) * 111.0;
			long minutosMinimos = (long) (((distanciaKm / 800.0) + 0.75) * 60.0);
			if (minutosReales < minutosMinimos) {
				LocalDateTime sugerida = salidaLdt.plusMinutes(minutosMinimos);
				res.getErrores().add(String.format(
						"Duración física insuficiente para %s -> %s (~%.0f km). Mínimo requerido: %d min (llegada sugerida >= %s).",
						origen.getCodigoIata(), destino.getCodigoIata(), distanciaKm, minutosMinimos, sugerida.format(FMT_FECHA_HORA)
				));
			}
		}
	}

	private void validarReglaAdelantoYCheckIn(SegmentoVuelo seg,
											  LocalDateTime nuevaSalida,
											  long vendidos,
											  long checkinsCompletados,
											  ResultadoReprogramacionDTO res) {
		if (seg.getHoraSalida() == null) {
			return;
		}
		LocalDateTime salidaActual = seg.getHoraSalida().toLocalDateTime();
		if (!nuevaSalida.isBefore(salidaActual)) {
			return;
		}
		LocalDateTime ahora = LocalDateTime.now();
		long horasHastaActual = Duration.between(ahora, salidaActual).toHours();
		long horasHastaNueva = Duration.between(ahora, nuevaSalida).toHours();
		if (checkinsCompletados > 0 && (horasHastaActual < HORAS_VENTANA_CHECKIN || horasHastaNueva < HORAS_VENTANA_CHECKIN)) {
			res.getErrores().add(String.format(
					"Adelanto bloqueado: existen %d check-in(s) completado(s) dentro de las próximas %d horas; adelantar el despegue dejaría pasajeros fuera de ventana o con pase expirado.",
					checkinsCompletados, HORAS_VENTANA_CHECKIN
			));
		} else if (vendidos > 0 && (horasHastaActual < HORAS_VENTANA_CHECKIN || horasHastaNueva < HORAS_VENTANA_CHECKIN)) {
			res.getErrores().add(String.format(
					"Adelanto bloqueado: el vuelo posee %d asiento(s) vendido(s) dentro de la ventana de Check-in (< %d h). No se permite adelantar la salida con poco preaviso.",
					vendidos, HORAS_VENTANA_CHECKIN
			));
		}
	}

	private void validarConflictosAeronaveYPiloto(Vuelo vuelo,
												  LocalDateTime salidaLdt,
												  LocalDateTime llegadaLdt,
												  ResultadoReprogramacionDTO res) {
		Timestamp salidaConBuffer = Timestamp.valueOf(salidaLdt.minusMinutes(BUFFER_TURNAROUND_MINUTOS));
		Timestamp llegadaConBuffer = Timestamp.valueOf(llegadaLdt.plusMinutes(BUFFER_TURNAROUND_MINUTOS));

		if (vuelo.getAvion() != null && vuelo.getAvion().getIdAvion() != null) {
			boolean choqueAvion = vueloRepository.existeConflictoAvion(
					vuelo.getAvion().getIdAvion(), salidaConBuffer, llegadaConBuffer, vuelo.getIdVuelo());
			if (choqueAvion) {
				res.getErrores().add("Choque de aeronave: el avión " + vuelo.getAvion().getNumeroDeRegistro()
						+ " tiene otro vuelo activo que se solapa o incumple el margen mínimo de rotación ("
						+ BUFFER_TURNAROUND_MINUTOS + " min).");
			}
		}
		if (vuelo.getPiloto() != null && vuelo.getPiloto().getRut() != null) {
			boolean choquePiloto = vueloRepository.existeConflictoPiloto(
					vuelo.getPiloto().getRut(), salidaConBuffer, llegadaConBuffer, vuelo.getIdVuelo());
			if (choquePiloto) {
				String nombrePiloto = vuelo.getPiloto().getUsuario() != null
						? vuelo.getPiloto().getUsuario().getNombre() + " " + vuelo.getPiloto().getUsuario().getApellido()
						: vuelo.getPiloto().getRut();
				res.getErrores().add("Choque de comandante: " + nombrePiloto + " (" + vuelo.getPiloto().getRut()
						+ ") tiene otro vuelo activo dentro de la ventana incluyendo margen de descanso ("
						+ BUFFER_TURNAROUND_MINUTOS + " min).");
			}
		}
	}

	private void validarConflictosTripulacionYTurnos(Integer idVuelo,
													 LocalDateTime salidaLdt,
													 LocalDateTime llegadaLdt,
													 ResultadoReprogramacionDTO res) {
		List<Turno> turnosVuelo = turnoRepository.findByVueloIdVuelo(idVuelo);
		res.setTurnosEvaluados(turnosVuelo != null ? turnosVuelo.size() : 0);

		Timestamp salidaConBuffer = Timestamp.valueOf(salidaLdt.minusMinutes(BUFFER_TURNAROUND_MINUTOS));
		Timestamp llegadaConBuffer = Timestamp.valueOf(llegadaLdt.plusMinutes(BUFFER_TURNAROUND_MINUTOS));

		List<TurnoTripulacion> choques = turnoTripulacionRepository.findConflictosTripulacionEnRango(
				idVuelo, salidaConBuffer, llegadaConBuffer);
		if (choques == null || choques.isEmpty()) {
			return;
		}
		Set<String> reportados = new HashSet<>();
		for (TurnoTripulacion tt : choques) {
			String rut = tt.getTripulacion1() != null ? tt.getTripulacion1().getRut() : "?";
			Vuelo otroVuelo = (tt.getTurno1() != null) ? tt.getTurno1().getVuelo() : null;
			String numOtro = (otroVuelo != null && otroVuelo.getNumeroVuelo() != null) ? otroVuelo.getNumeroVuelo() : "otro vuelo";
			String clave = rut + "|" + numOtro;
			if (!reportados.add(clave)) {
				continue;
			}
			String nombre = (tt.getTripulacion1() != null && tt.getTripulacion1().getUsuario() != null)
					? tt.getTripulacion1().getUsuario().getNombre() + " " + tt.getTripulacion1().getUsuario().getApellido()
					: rut;
			String rangoOtro = (tt.getTurno1() != null && tt.getTurno1().getHoraInicio() != null && tt.getTurno1().getHoraFin() != null)
					? tt.getTurno1().getHoraInicio().toLocalDateTime().format(FMT_FECHA_HORA) + " - "
					  + tt.getTurno1().getHoraFin().toLocalDateTime().format(FMT_FECHA_HORA)
					: "";
			res.getErrores().add(String.format(
					"Choque de turno de tripulación: %s (%s) está asignado al vuelo %s (%s; margen mínimo %d min).",
					nombre, rut, numOtro, rangoOtro, BUFFER_TURNAROUND_MINUTOS
			));
		}
	}

	private void validarConflictosPuerta(SegmentoVuelo seg,
										 Integer idVuelo,
										 Timestamp nuevaSalida,
										 Integer idPuertaSeleccionada,
										 ResultadoReprogramacionDTO res) {
		Integer idPuertaEvaluar = idPuertaSeleccionada;
		if (idPuertaEvaluar == null) {
			List<AsignacionPuerta> aps = asignacionPuertaRepository.findBySegmentoVuelo(seg);
			if (aps != null && !aps.isEmpty() && aps.get(0).getPuertaEmbarque() != null) {
				idPuertaEvaluar = aps.get(0).getPuertaEmbarque().getIdPuerta();
			}
		}
		if (idPuertaEvaluar == null) {
			return;
		}

		PuertaEmbarque puerta = puertaEmbarqueRepository.findById(idPuertaEvaluar).orElse(null);
		if (puerta == null) {
			res.getErrores().add("La puerta de embarque seleccionada no existe o fue desactivada.");
			return;
		}
		if (seg.getAeropuertoOrigen() != null && puerta.getAeropuerto() != null
				&& !seg.getAeropuertoOrigen().getIdAeropuerto().equals(puerta.getAeropuerto().getIdAeropuerto())) {
			res.getErrores().add("La puerta " + puerta.getCodigoPuerta() + " no pertenece al aeropuerto de origen "
					+ seg.getAeropuertoOrigen().getCodigoIata() + ".");
			return;
		}

		LocalDateTime salLdt = nuevaSalida.toLocalDateTime();
		Timestamp ventanaInicio = Timestamp.valueOf(salLdt.minusMinutes(VENTANA_PUERTA_MINUTOS));
		Timestamp ventanaFin = Timestamp.valueOf(salLdt.plusMinutes(VENTANA_PUERTA_MINUTOS));

		List<AsignacionPuerta> choques = asignacionPuertaRepository.findConflictosPuertaEnVentana(
				idPuertaEvaluar, idVuelo, ventanaInicio, ventanaFin);
		if (choques != null && !choques.isEmpty()) {
			for (AsignacionPuerta ap : choques) {
				SegmentoVuelo otroSeg = ap.getSegmentoVuelo();
				Vuelo otroVuelo = otroSeg != null ? otroSeg.getVuelo() : null;
				String numOtro = (otroVuelo != null && otroVuelo.getNumeroVuelo() != null) ? otroVuelo.getNumeroVuelo() : "#" + (otroVuelo != null ? otroVuelo.getIdVuelo() : "?");
				String horaOtro = (otroSeg != null && otroSeg.getHoraSalida() != null)
						? otroSeg.getHoraSalida().toLocalDateTime().format(FMT_FECHA_HORA) : "";
				res.getErrores().add(String.format(
						"Choque de puerta %s: ocupada por vuelo %s (salida %s) dentro de la ventana de ±%d min. Elija otra puerta disponible.",
						puerta.getCodigoPuerta(), numOtro, horaOtro, VENTANA_PUERTA_MINUTOS
				));
			}
		}
	}

	private void validarEscalasItinerarios(Integer idVuelo,
										   LocalDateTime nuevaSalida,
										   LocalDateTime nuevaLlegada,
										   ResultadoReprogramacionDTO res) {
		List<Integer> idsItinerarios = itinerarioVueloRepository.findIdsItinerariosPorVuelo(idVuelo);
		if (idsItinerarios == null || idsItinerarios.isEmpty()) {
			res.setItinerariosEvaluados(0);
			res.setItinerariosImpactados(new ArrayList<>());
			return;
		}
		res.setItinerariosEvaluados(idsItinerarios.size());
		List<ItinerarioImpactoDTO> impactoList = new ArrayList<>();

		for (Integer idItin : idsItinerarios) {
			List<ItinerarioVuelo> tramos = itinerarioVueloRepository.findByItinerarioIdItinerarioOrderByOrdenAsc(idItin);
			if (tramos == null || tramos.isEmpty()) {
				continue;
			}
			Itinerario itin = tramos.get(0).getItinerario();
			ItinerarioImpactoDTO dto = new ItinerarioImpactoDTO();
			dto.setIdItinerario(idItin);
			String origCom = (itin != null && itin.getAeropuertoOrigen() != null) ? itin.getAeropuertoOrigen().getCodigoIata() : "?";
			String destCom = (itin != null && itin.getAeropuertoDestino() != null) ? itin.getAeropuertoDestino().getCodigoIata() : "?";
			dto.setRutaComercial(origCom + " → " + destCom);
			dto.setNumeroEscalas(Math.max(0, tramos.size() - 1));
			dto.setReservasActivas(contarReservasActivasPorItinerario(idItin));

			StringBuilder sec = new StringBuilder();
			int idx = -1;
			for (int i = 0; i < tramos.size(); i++) {
				Vuelo vTramo = tramos.get(i).getVuelo();
				if (i > 0) {
					sec.append(" ➔ ");
				}
				if (vTramo != null && idVuelo.equals(vTramo.getIdVuelo())) {
					idx = i;
					String cod = vTramo.getNumeroVuelo() != null ? vTramo.getNumeroVuelo() : "#" + vTramo.getIdVuelo();
					sec.append("[").append(cod).append(" ").append(nuevaSalida.format(FMT_FECHA_HORA))
					   .append("-").append(nuevaLlegada.format(DateTimeFormatter.ofPattern("HH:mm"))).append("]");
				} else if (vTramo != null) {
					String cod = vTramo.getNumeroVuelo() != null ? vTramo.getNumeroVuelo() : "#" + vTramo.getIdVuelo();
					String hor = vTramo.getFechaHoraSalida() != null
							? vTramo.getFechaHoraSalida().toLocalDateTime().format(FMT_FECHA_HORA) : "sin hora";
					sec.append(cod).append(" (").append(hor).append(")");
				}
			}
			dto.setSecuenciaTramos(sec.toString());

			if (idx < 0) {
				dto.setEstadoConexion("DIRECTO");
				dto.setDetalleConexion("Tramo único.");
				impactoList.add(dto);
				continue;
			}

			boolean rota = false;
			List<String> detallesConexion = new ArrayList<>();

			// Validar escala respecto al vuelo previo en el itinerario
			for (int j = idx - 1; j >= 0; j--) {
				Vuelo prev = tramos.get(j).getVuelo();
				if (prev == null || esVueloCancelado(prev)) {
					continue;
				}
				if (prev.getFechaHoraLlegada() != null) {
					LocalDateTime llegadaPrev = prev.getFechaHoraLlegada().toLocalDateTime();
					long minEscala = Duration.between(llegadaPrev, nuevaSalida).toMinutes();
					String numPrev = prev.getNumeroVuelo() != null ? prev.getNumeroVuelo() : "#" + prev.getIdVuelo();
					if (minEscala < MIN_ESCALA_MINUTOS) {
						rota = true;
						LocalDateTime salidaMinima = llegadaPrev.plusMinutes(MIN_ESCALA_MINUTOS);
						detallesConexion.add(String.format("ROTA con previo %s (%d min < %d min; min salida %s)",
								numPrev, minEscala, MIN_ESCALA_MINUTOS, salidaMinima.format(FMT_FECHA_HORA)));
						res.getErrores().add(String.format(
								"Escala rota en Itinerario #%d (%s): el tramo previo %s llega %s y solo deja %d min de conexión (mínimo %d min; salida requerida >= %s).",
								idItin, dto.getRutaComercial(), numPrev, llegadaPrev.format(FMT_FECHA_HORA),
								minEscala, MIN_ESCALA_MINUTOS, salidaMinima.format(FMT_FECHA_HORA)
						));
					} else {
						detallesConexion.add(String.format("Escala desde %s: %d min OK", numPrev, minEscala));
					}
				}
				break;
			}

			// Validar escala respecto al vuelo siguiente en el itinerario
			for (int j = idx + 1; j < tramos.size(); j++) {
				Vuelo next = tramos.get(j).getVuelo();
				if (next == null || esVueloCancelado(next)) {
					continue;
				}
				if (next.getFechaHoraSalida() != null) {
					LocalDateTime salidaNext = next.getFechaHoraSalida().toLocalDateTime();
					long minEscala = Duration.between(nuevaLlegada, salidaNext).toMinutes();
					String numNext = next.getNumeroVuelo() != null ? next.getNumeroVuelo() : "#" + next.getIdVuelo();
					if (minEscala < MIN_ESCALA_MINUTOS) {
						rota = true;
						LocalDateTime llegadaMaxima = salidaNext.minusMinutes(MIN_ESCALA_MINUTOS);
						detallesConexion.add(String.format("ROTA con siguiente %s (%d min < %d min; max llegada %s)",
								numNext, minEscala, MIN_ESCALA_MINUTOS, llegadaMaxima.format(FMT_FECHA_HORA)));
						res.getErrores().add(String.format(
								"Escala rota en Itinerario #%d (%s): el tramo siguiente %s despega %s y solo quedan %d min de conexión (mínimo %d min; llegada máxima <= %s).",
								idItin, dto.getRutaComercial(), numNext, salidaNext.format(FMT_FECHA_HORA),
								minEscala, MIN_ESCALA_MINUTOS, llegadaMaxima.format(FMT_FECHA_HORA)
						));
					} else {
						detallesConexion.add(String.format("Escala hacia %s: %d min OK", numNext, minEscala));
					}
				}
				break;
			}

			if (detallesConexion.isEmpty()) {
				dto.setEstadoConexion("DIRECTO");
				dto.setDetalleConexion("Itinerario directo sin escalas intermedias.");
			} else {
				dto.setEstadoConexion(rota ? "ESCALA ROTA" : "OK");
				dto.setDetalleConexion(String.join(" · ", detallesConexion));
			}
			impactoList.add(dto);
		}
		res.setItinerariosImpactados(impactoList);
	}

	private void validarCoherenciaIdaVueltaReservas(Integer idVuelo,
													LocalDateTime nuevaSalida,
													LocalDateTime nuevaLlegada,
													ResultadoReprogramacionDTO res) {
		List<?> reservasRtIds = em.createNativeQuery(
				"SELECT DISTINCT r.id_reserva " +
				"FROM reserva r " +
				"LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva " +
				"JOIN reserva_itinerario ri_has ON ri_has.id_reserva = r.id_reserva " +
				"JOIN itinerario_vuelo iv ON iv.id_itinerario = ri_has.id_itinerario " +
				"WHERE iv.id_vuelo = :idVuelo " +
				"AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%CANCEL%' " +
				"AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%ANUL%' " +
				"AND (SELECT COUNT(*) FROM reserva_itinerario ri2 WHERE ri2.id_reserva = r.id_reserva) > 1"
		).setParameter("idVuelo", idVuelo).getResultList();

		if (reservasRtIds == null || reservasRtIds.isEmpty()) {
			return;
		}

		for (Object idResObj : reservasRtIds) {
			Integer idReserva = ((Number) idResObj).intValue();
			@SuppressWarnings("unchecked")
			List<Object[]> filas = em.createNativeQuery(
					"SELECT r.codigo_reserva, i.id_itinerario, i.hora_salida, i.hora_llegada, " +
					"EXISTS(SELECT 1 FROM itinerario_vuelo iv2 WHERE iv2.id_itinerario = i.id_itinerario AND iv2.id_vuelo = :idVuelo) AS contiene_vuelo " +
					"FROM reserva r " +
					"JOIN reserva_itinerario ri ON ri.id_reserva = r.id_reserva " +
					"JOIN itinerario i ON i.id_itinerario = ri.id_itinerario " +
					"WHERE r.id_reserva = :idReserva " +
					"ORDER BY ri.id_reserva_itinerario ASC"
			)
					.setParameter("idVuelo", idVuelo)
					.setParameter("idReserva", idReserva)
					.getResultList();

			if (filas == null || filas.size() < 2) {
				continue;
			}
			String codigoReserva = filas.get(0)[0] != null ? String.valueOf(filas.get(0)[0]) : "RES-" + idReserva;
			LocalDateTime[] salidas = new LocalDateTime[filas.size()];
			LocalDateTime[] llegadas = new LocalDateTime[filas.size()];

			for (int i = 0; i < filas.size(); i++) {
				Object[] row = filas.get(i);
				Integer idItin = ((Number) row[1]).intValue();
				Timestamp tsSal = (Timestamp) row[2];
				Timestamp tsLleg = (Timestamp) row[3];
				boolean contieneVuelo = Boolean.TRUE.equals(row[4]);

				if (!contieneVuelo) {
					salidas[i] = tsSal != null ? tsSal.toLocalDateTime() : null;
					llegadas[i] = tsLleg != null ? tsLleg.toLocalDateTime() : null;
				} else {
					List<ItinerarioVuelo> tramos = itinerarioVueloRepository.findByItinerarioIdItinerarioOrderByOrdenAsc(idItin);
					LocalDateTime minSal = null;
					LocalDateTime maxLleg = null;
					for (ItinerarioVuelo iv : tramos) {
						Vuelo v = iv.getVuelo();
						if (v == null) continue;
						LocalDateTime s = idVuelo.equals(v.getIdVuelo())
								? nuevaSalida
								: (v.getFechaHoraSalida() != null ? v.getFechaHoraSalida().toLocalDateTime() : null);
						LocalDateTime l = idVuelo.equals(v.getIdVuelo())
								? nuevaLlegada
								: (v.getFechaHoraLlegada() != null ? v.getFechaHoraLlegada().toLocalDateTime() : null);
						if (s != null && (minSal == null || s.isBefore(minSal))) minSal = s;
						if (l != null && (maxLleg == null || l.isAfter(maxLleg))) maxLleg = l;
					}
					salidas[i] = minSal;
					llegadas[i] = maxLleg;
				}
			}

			for (int i = 0; i < filas.size() - 1; i++) {
				if (llegadas[i] != null && salidas[i + 1] != null) {
					long minEntre = Duration.between(llegadas[i], salidas[i + 1]).toMinutes();
					if (minEntre < MIN_ESCALA_MINUTOS) {
						res.getErrores().add(String.format(
								"Inconsistencia Ida/Vuelta en Reserva %s: el itinerario de ida llegaría %s y chocaría con el regreso (%s).",
								codigoReserva, llegadas[i].format(FMT_FECHA_HORA), salidas[i + 1].format(FMT_FECHA_HORA)
						));
					}
				}
			}
		}
	}

	private long contarReservasActivasPorVuelo(Integer idVuelo) {
		Object res = em.createNativeQuery(
				"SELECT COUNT(DISTINCT r.id_reserva) " +
				"FROM reserva r " +
				"LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva " +
				"WHERE UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%CANCEL%' " +
				"AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%ANUL%' " +
				"AND (" +
				"   r.id_reserva IN (SELECT ra.id_reserva FROM reserva_asiento ra WHERE ra.id_vuelo = :idVuelo) " +
				"   OR r.id_reserva IN (" +
				"       SELECT ri.id_reserva FROM reserva_itinerario ri " +
				"       JOIN itinerario_vuelo iv ON iv.id_itinerario = ri.id_itinerario " +
				"       WHERE iv.id_vuelo = :idVuelo" +
				"   )" +
				")"
		).setParameter("idVuelo", idVuelo).getSingleResult();
		return res != null ? ((Number) res).longValue() : 0L;
	}

	private long contarReservasActivasPorItinerario(Integer idItinerario) {
		Object res = em.createNativeQuery(
				"SELECT COUNT(DISTINCT r.id_reserva) " +
				"FROM reserva_itinerario ri " +
				"JOIN reserva r ON r.id_reserva = ri.id_reserva " +
				"LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva " +
				"WHERE ri.id_itinerario = :idItin " +
				"AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%CANCEL%' " +
				"AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%ANUL%'"
		).setParameter("idItin", idItinerario).getSingleResult();
		return res != null ? ((Number) res).longValue() : 0L;
	}

	private long contarCheckinsCompletadosPorVuelo(Integer idVuelo) {
		Object res = em.createNativeQuery(
				"SELECT COUNT(DISTINCT c.id_checkin) " +
				"FROM checkin c " +
				"JOIN reserva r ON r.id_reserva = c.id_reserva " +
				"LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva " +
				"WHERE UPPER(COALESCE(c.estado, '')) = 'COMPLETADO' " +
				"AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%CANCEL%' " +
				"AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%ANUL%' " +
				"AND (" +
				"   r.id_reserva IN (SELECT ra.id_reserva FROM reserva_asiento ra WHERE ra.id_vuelo = :idVuelo) " +
				"   OR r.id_reserva IN (" +
				"       SELECT ri.id_reserva FROM reserva_itinerario ri " +
				"       JOIN itinerario_vuelo iv ON iv.id_itinerario = ri.id_itinerario " +
				"       WHERE iv.id_vuelo = :idVuelo" +
				"   )" +
				")"
		).setParameter("idVuelo", idVuelo).getSingleResult();
		return res != null ? ((Number) res).longValue() : 0L;
	}

	private EstadoVuelo buscarEstadoRetrasado() {
		List<EstadoVuelo> estados = estadoVueloRepository.findAll();
		if (estados == null) {
			return null;
		}
		for (EstadoVuelo ev : estados) {
			String desc = ev.getDescripcion() != null ? ev.getDescripcion().toUpperCase() : "";
			if (desc.contains("RETRAS") || desc.contains("DEMOR")) {
				return ev;
			}
		}
		return null;
	}

	private void registrarAuditoriaReprogramacion(Vuelo vuelo,
												  String rutAdmin,
												  String motivo,
												  String salidaAnt,
												  String llegadaAnt,
												  String salidaNueva,
												  String llegadaNueva,
												  Integer puertaAnt,
												  Integer puertaNueva,
												  String estadoAnt,
												  String estadoNuevo,
												  ResultadoReprogramacionDTO res) {
		try {
			Map<String, Object> ant = new LinkedHashMap<>();
			ant.put("hora_salida", salidaAnt);
			ant.put("hora_llegada", llegadaAnt);
			ant.put("id_puerta", puertaAnt);
			ant.put("estado", estadoAnt);

			Map<String, Object> nue = new LinkedHashMap<>();
			nue.put("hora_salida", salidaNueva);
			nue.put("hora_llegada", llegadaNueva);
			nue.put("id_puerta", puertaNueva);
			nue.put("estado", estadoNuevo);

			String jsonAnt = auditObjectMapper.writeValueAsString(ant);
			String jsonNue = auditObjectMapper.writeValueAsString(nue);
			String detalle = String.format(
					"REPROGRAMACION VUELO %s | MOTIVO: %s | ASIENTOS_VENDIDOS: %d | ITINERARIOS_EVALUADOS: %d | TURNOS_EVALUADOS: %d",
					vuelo.getNumeroVuelo() != null ? vuelo.getNumeroVuelo() : "#" + vuelo.getIdVuelo(),
					motivo,
					res.getAsientosVendidos(),
					res.getItinerariosEvaluados(),
					res.getTurnosEvaluados()
			);

			em.createNativeQuery(
					"INSERT INTO log_auditoria (rut_usuario, accion, nivel, tabla_afectada, id_registro_afectado, datos_anteriores, datos_nuevos, detalle) " +
					"VALUES (:rut, 'REPROGRAMACION_HORARIO_VUELO', CAST('INFO' AS tipo_nivel_log), 'vuelo', :idVuelo, CAST(:ant AS jsonb), CAST(:nue AS jsonb), :det)"
			)
					.setParameter("rut", (rutAdmin != null && !rutAdmin.isBlank()) ? rutAdmin : null)
					.setParameter("idVuelo", vuelo.getIdVuelo())
					.setParameter("ant", jsonAnt)
					.setParameter("nue", jsonNue)
					.setParameter("det", detalle)
					.executeUpdate();
		} catch (Exception ex) {
			LOGGER.warn("No se pudo registrar log_auditoria para reprogramación del vuelo {}: {}",
					vuelo.getIdVuelo(), ex.getMessage());
		}
	}


}
