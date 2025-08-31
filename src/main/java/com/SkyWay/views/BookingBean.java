package com.SkyWay.views;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;


import com.SkyWay.util.Logger;
import org.hibernate.exception.GenericJDBCException;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.FilterMeta;

import org.springframework.beans.factory.annotation.Autowired;

import com.SkyWay.dto.InfoAsientoDTO;
import com.SkyWay.dto.InfoVueloDTO;
import com.SkyWay.model.EstadoReserva;
import com.SkyWay.model.Pasajero;
import com.SkyWay.model.Reserva;
import com.SkyWay.model.ReservaAsiento;
import com.SkyWay.model.Usuario;
import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.pasajero.domain.service.PasajeroService;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.vuelo.domain.service.VueloService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;


@Named("bookingBean")
@ViewScoped
public class BookingBean implements Serializable{



    private InfoVueloDTO vueloSeleccionado;
    private boolean reservaConfirmada;
    private List<InfoAsientoDTO>asientos;

    private List<InfoAsientoDTO>asientosFiltrados;
    private ArrayList<InfoAsientoDTO>asientosSeleccionados;
    private HashMap<String,InfoAsientoDTO>asientosSeles;

    private List<FilterMeta> filterBy;

    private boolean globalFilterOnly;


    @Autowired
    private AsientoService asientoService;

    @Autowired
    private VueloService vueloService;

    @Autowired
    private AvionService avionService;

    @Autowired
    private PasajeroService pasajeroService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private HttpSession session;

    private int totalReserva=0;

    private boolean value2;

    private List<InfoAsientoDTO> asientoSeleccionados;
    private HashMap<Integer, InfoAsientoDTO> asientosCambio;
    private InfoVueloDTO vuelo;
    private InfoAsientoDTO asientoActual;



    public BookingBean() {

    }


    @PostConstruct
    public void init() {

        asientosSeleccionados=new ArrayList<InfoAsientoDTO>();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        // Obtener el vuelo seleccionado desde la sesión
        vueloSeleccionado = (InfoVueloDTO) externalContext.getSessionMap().get("vueloSeleccionado");

        if (vueloSeleccionado != null) {
            // Si se encontró el vuelo, obtener los asientos
            System.out.println("Vuelo seleccionado: " + vueloSeleccionado);
            asientos = asientoService.getAsientosDisponibles(vueloSeleccionado.getIdVuelo());
            asientosSeles=new HashMap<>();
            // Verificar si los asientos se cargaron correctamente
            System.out.println("Número de asientos cargados: " + (asientos != null ? asientos.size() : 0));
        } else {
            // Si no se encuentra el vuelo, redirigir o manejar el error
            System.out.println("No se encontró el vuelo seleccionado en la sesión.");
        }
    }



    // Método para confirmar la reserva
    public String confirmarReserva() {
        // Lógica para procesar la reserva (por ejemplo, guardar en la base de datos)
        reservaConfirmada = true; // Se confirma la reserva}

        //LOGGER.info("Vuelo {} asientos {} ",vueloSeleccionado,asientos);


        ArrayList<ReservaAsiento>asientos=new ArrayList<ReservaAsiento>();
        Reserva reserva=new Reserva();
        ReservaAsiento reservaAsiento;

        for (int i = 0; i < asientosSeleccionados.size(); i++) {
            var	asiento= asientoService.findById(asientosSeleccionados.get(i).getIdAsiento());
            reservaAsiento=new ReservaAsiento();
            Logger.logInfo("corregir");
            //reservaAsiento.setAsiento(asiento.get());
            reservaAsiento.setReserva(reserva);
            asientos.add(reservaAsiento);
        }


		  		/*if (asientosSeleccionados==null||asientosSeleccionados.length==0 ) {
			model.addAttribute("mensaje", "Por favor, selecciona al menos un asiento.");
			return "home/booking"; // Regresar a la vista de reserva
		}*/



        var pasajero= (Usuario) session.getAttribute("usuario");

        if (pasajero instanceof Pasajero) {
            //LOGGER.info("Informacion del pasajero {} ", pasajero);
            reserva.setPasajero((Pasajero) pasajero);
            //reserva.setVuelo(vueloService.findById(vueloSeleccionado.getIdVuelo()).get());

            //LOGGER.info("Informacion del pasajero {} ",informacionPasjero);



//			LOGGER.info("Informacion del vuelo {} Avion {}",vueloService.findById(idVuelo).get());

            //asientos.forEach(System.out::println);

            EstadoReserva esadoReserva=new EstadoReserva();
            esadoReserva.setIdEstadoReserva(1);
            reserva.setEstadoReservaBean(esadoReserva);
            reserva.setReservaAsientos(asientos);
            reserva.setFechaReserva(new Timestamp(new Date().getTime()));
            //reservaService.save(reserva);


            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Message", "Reserva lista");

            PrimeFaces.current().dialog().showMessageDynamic(message);
        }


        return "reservaConfirmada.xhtml?faces-redirect=true"; // Redirige a la página de confirmación
    }

    public void comfirmarAsientos() {
        reservaConfirmada = true;
        var pasajero = (Usuario) session.getAttribute("usuario");

        int[] asientosArray = asientosSeleccionados.stream()
                .mapToInt(InfoAsientoDTO::getIdAsiento)
                .toArray();

        Integer[] asientos = Arrays.stream(asientosArray)
                .boxed()
                .toArray(Integer[]::new);

        try {
            String mensaje = reservaService.confirmarReserva(vueloSeleccionado.getIdVuelo(), asientos, pasajero.getRutUsuario(),1);

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Reserva confirmada", mensaje);
            PrimeFaces.current().dialog().showMessageDynamic(message);

        } catch (SQLException e) {
            Logger.logInfo("Error SQL en la reserva: " + e.getMessage());

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error en la reserva", e.getMessage());
            PrimeFaces.current().dialog().showMessageDynamic(message);
        } catch (Exception ex) {
            Logger.logInfo("Error inesperado: " + ex.getMessage());
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error inesperado", ex.getMessage());
            PrimeFaces.current().dialog().showMessageDynamic(message);
        }
    }


    /**
     * Función que loguea todos los detalles posibles de una excepción JDBC.
     */
    private String logJDBCExceptionDetails(GenericJDBCException jdbcException) {
        String error = "";

        // Capturamos los detalles de la SQLException subyacente, si existe
        SQLException sqlException = jdbcException.getSQLException();
        if (sqlException != null) {
            // Procesamos la causa específica de la SQLException
            Throwable cause = sqlException.getCause();
            if (cause != null) {
                error = extractErrorMessage(cause);
            }
        } else {
            //LOGGER.error("No se encontró SQLException subyacente.");
        }

        // Capturamos los detalles de la causa de la excepción JDBC, si existe
        Throwable cause = jdbcException.getCause();
        if (cause != null) {
            error = extractErrorMessage(cause);
        }

        return error;
    }

    /**
     * Función que extrae el mensaje de error específico de la excepción.
     */
    private String extractErrorMessage(Throwable cause) {
        // Extraemos el mensaje de error después del primer ":"
        String message = cause.getMessage();
        if (message != null && message.contains(":")) {
            return message.substring(message.indexOf(":") + 1,
                    message.lastIndexOf(".")).trim();
        }
        return "Error desconocido";
    }



    public void setearAsiento(InfoAsientoDTO asiento) {
        if (asiento.getEstado().equalsIgnoreCase("ocupado")) {
            return; // No se puede seleccionar un asiento ocupado
        }

        boolean yaSeleccionado = asientosSeleccionados.contains(asiento);

        if (yaSeleccionado) {
            // Deseleccionar asiento (quitarlo)
            asientosSeleccionados.remove(asiento);
            totalReserva -= asiento.getPrecio();
            for (int i = 0; i < asientos.size(); i++) {
                if (asientos.get(i).getIdAsiento() == asiento.getIdAsiento()) {
                    asientos.get(i).setEstado("disponible");
                }
            }
        } else {
            // Seleccionar asiento
            asientosSeleccionados.add(asiento);
            totalReserva += asiento.getPrecio();
            for (int i = 0; i < asientos.size(); i++) {
                if (asientos.get(i).getIdAsiento() == asiento.getIdAsiento()) {
                    asientos.get(i).setEstado("seleccionado");
                }
            }
        }
    }


    public void onRowSelect(SelectEvent<InfoAsientoDTO> event) {
        InfoAsientoDTO asiento = event.getObject();
        if (!asiento.getEstado().equals("ocupado") && !asientosSeleccionados.contains(asiento)) {
            asientosSeleccionados.add(asiento);
            totalReserva += asiento.getPrecio();
        }
    }

    public void onRowUnselect(UnselectEvent<InfoAsientoDTO> event) {
        InfoAsientoDTO asiento = event.getObject();
        if (asientosSeleccionados.contains(asiento)) {
            asientosSeleccionados.remove(asiento);
            totalReserva -= asiento.getPrecio();
        }
    }

    public void abrirDistribucion() {
        PrimeFaces.current().executeScript("PF('modalAsientos').show();");
    }


    public InfoVueloDTO getVueloSeleccionado() {
        return vueloSeleccionado;
    }

    public void setVueloSeleccionado(InfoVueloDTO vueloSeleccionado) {
        this.vueloSeleccionado = vueloSeleccionado;
    }

    public boolean isReservaConfirmada() {
        return reservaConfirmada;
    }

    public void setReservaConfirmada(boolean reservaConfirmada) {
        this.reservaConfirmada = reservaConfirmada;
    }

    public List<InfoAsientoDTO> getAsientos() {
        return asientos;
    }

    public void setAsientos(List<InfoAsientoDTO> asientos) {
        this.asientos = asientos;
    }

    public List<FilterMeta> getFilterBy() {
        return filterBy;
    }


    public boolean isGlobalFilterOnly() {
        return globalFilterOnly;
    }

    public void setGlobalFilterOnly(boolean globalFilterOnly) {
        this.globalFilterOnly = globalFilterOnly;
    }

    public String[] getCustomerStatus() {
        List<String> statuses = List.of("Económica", "Ejecutiva", "Primera Clase");
        return statuses.toArray(new String[statuses.size()]);
    }

    public List<InfoAsientoDTO> getAsientosFiltrados() {
        return asientosFiltrados;
    }

    public void setAsientosFiltrados(List<InfoAsientoDTO> asientosFiltrados) {
        this.asientosFiltrados = asientosFiltrados;
    }

    public List<InfoAsientoDTO> getAsientosSeleccionados() {
        return asientosSeleccionados;
    }
    public void setAsientoService(AsientoService asientoService) {
        this.asientoService = asientoService;
    }

    public void setAsientosSeleccionados(ArrayList<InfoAsientoDTO> asientosSeleccionados) {
        this.asientosSeleccionados = asientosSeleccionados;
    }

    public int getTotalReserva() {
        return totalReserva;
    }

    public void setTotalReserva(int totalReserva) {
        this.totalReserva = totalReserva;
    }

    public void reservarAsiento(InfoAsientoDTO asiento) {
        if (!asiento.getEstado().equals("ocupado")) {
            if (asientosSeleccionados.contains(asiento)) {
                // Si el asiento ya está seleccionado, lo deseleccionamos
                asientosSeleccionados.remove(asiento);
                totalReserva -= asiento.getPrecio(); // Restar el precio al total

                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Asientos Eliminados"));
                PrimeFaces.current().ajax().update("form:messages", "form:dt-asientos");
                PrimeFaces.current().executeScript("PF('dtAsientos').clearFilters()");

            } else {
                // Si no está seleccionado, lo agregamos a la lista
                asientosSeleccionados.add(asiento);
                totalReserva += asiento.getPrecio(); // Sumar el precio al total

                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Asiento Agregado"));
                PrimeFaces.current().executeScript("PF('manageAsientoDialog').hide()");
                PrimeFaces.current().ajax().update("form:messages", "form:dt-asientos");

            }


        }

    }

    public AsientoService getAsientoService() {
        return asientoService;
    }

    public InfoAsientoDTO getAsientoActual() {
        return asientoActual;
    }

    public void setAsientoActual(InfoAsientoDTO asientoActual) {
        this.asientoActual = asientoActual;
    }

}


