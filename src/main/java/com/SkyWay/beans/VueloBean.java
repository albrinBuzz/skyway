package com.SkyWay.beans;


import java.io.Serial;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.estadovuelo.domain.model.EstadoVuelo;
import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import org.hibernate.exception.GenericJDBCException;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;



import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.claseasiento.domain.service.ClaseAsientoService;
import com.SkyWay.modules.estadovuelo.domain.service.EstadoVueloService;
import com.SkyWay.modules.piloto.domain.service.PiloService;
import com.SkyWay.modules.precioasiento.domain.service.PrecioAsientoService;
import com.SkyWay.modules.vuelo.domain.service.VueloService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.validation.Validator;
@Named("vueloView")
@ViewScoped
public class VueloBean implements Serializable {
	   private static final Logger logger = LoggerFactory.getLogger(VueloBean.class);

	
    @Serial
    private static final long serialVersionUID = 1L;

    @Autowired
    private VueloService vueloService; // Servicio para acceder a la lógica de negocio y persistencia

    @Autowired
    private AeropuertoService aeropuertoService;
    @Autowired
    private PiloService pilotoService;
    @Autowired
    private AvionService avionService;
    @Autowired
    private EstadoVueloService estadoVueloService;
	@Autowired
	private ClaseAsientoService claseAsientoService;

	@Autowired
	private PrecioAsientoService precioAsientoService;

    private List<Vuelo> vuelos;
    private Vuelo selectedVuelo;
    private List<Vuelo> selectedVuelos;
    private Vuelo nuevoVuelo;

    private List<Aeropuerto> listaAeropuertos;
    private List<Avion> listaAviones;
    private List<Piloto> listaPilotos;
    private List<EstadoVuelo> listaEstadosVuelo;

    private String aeropuerto2;
    private String aeropuerto1;
    private String estadoVuelo;
    private HashMap<String,Aeropuerto>aeropuertos;
    private HashMap<String,Piloto >pilotos;
    private HashMap<String, Avion>aviones;
    private Avion avionSeleccionado;
    
    private String avion;
    private String piloto;
    private Integer precioPrimera;
    private Integer precioEjecutiva;
    private Integer precioEconomica;

    private int cantidadAsientos;
    private int asientosPrimeraClase;
    private int asientosEjecutiva;
    private int asientosEconomica;
    private Validator validator;

    @PostConstruct
    public void init() {
        vuelos = vueloService.findAll();
        listaAeropuertos = aeropuertoService.findAll();
        listaAviones = avionService.findAll();
        listaPilotos = pilotoService.findAll();
        listaEstadosVuelo =estadoVueloService.findAll();
        nuevoVuelo = new Vuelo();
        aeropuertos=new HashMap<>();
        aviones=new HashMap<String, Avion>();
        pilotos=new HashMap<String, Piloto>();
        listaAeropuertos.forEach(aeropuerto -> aeropuertos.put(aeropuerto.getNombreAeropuerto(),aeropuerto));
        listaAviones.forEach(t -> aviones.put(t.getIdAvion().toString(),t));
        listaPilotos.forEach(t -> pilotos.put(t.getRut(), t));
        //this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public void saveVuelo() {
    	LocalDateTime fechaActual = LocalDateTime.now();
        if (aeropuerto2==null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El Aeropuerto de Salida es obligatorio", ""));
            return;
        }

        if (aeropuerto1==null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El Aeropuerto de Llegada es obligatorio", ""));
            return;
        }

        if (estadoVuelo == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El Estado del Vuelo es obligatorio", ""));
            return;
        }
  
        // Verificar que la fecha de salida y la fecha de llegada sean iguales o mayores que la fecha actual
        /*if (selectedVuelo.getFechaHoraSalida().isBefore(fechaActual)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("La fecha de salida no puede ser anterior a la fecha actual"));
            return; // Detener el proceso de guardado
        }

        if (selectedVuelo.getFechaHoraLlegada().isBefore(fechaActual)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("La fecha de llegada no puede ser anterior a la fecha actual"));
            return; // Detener el proceso de guardado
        }

        // Verificar que la fecha de llegada sea posterior o igual a la fecha de salida
        if (selectedVuelo.getFechaHoraLlegada().isBefore(selectedVuelo.getFechaHoraSalida())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("La fecha de llegada no puede ser anterior a la fecha de salida"));
            return; // Detener el proceso de guardado
        }
        
        // Verificar que la fecha de llegada sea posterior o igual a la fecha de salida
        if (selectedVuelo.getFechaHoraSalida().isAfter(selectedVuelo.getFechaHoraLlegada())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("La fecha de salida no puede ser despues a la fecha de llegada"));
            return; // Detener el proceso de guardado
        }*/
        
        

        //selectedVuelo.setAeropuerto1(aeropuertos.get(aeropuerto2));
        //selectedVuelo.setAeropuerto2(aeropuertos.get(aeropuerto1));
        
        var estadto=listaEstadosVuelo.stream().filter(estadoVuelo1 -> estadoVuelo1.getEstado().equals(estadoVuelo)
                ).findFirst().get();	

        //selectedVuelo.setEstadoVuelo(estadto);

        selectedVuelo.setPiloto(pilotos.get(piloto));

        selectedVuelo.setAvion(avionSeleccionado);
        
        if (selectedVuelo.getIdVuelo() == null) {
        	try {
        	    // Guardar el vuelo
        	    var vueloGuardado = vueloService.save(selectedVuelo);

        	    // Configurar precios de asiento
        	    /*PrecioAsiento precioEco = new PrecioAsiento();
        	    PrecioAsiento precioPrim = new PrecioAsiento();
        	    PrecioAsiento precioEje = new PrecioAsiento();

        	    // Asignar clase de asiento y precios
        	    precioEco.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(1).get());
        	    precioEje.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(2).get());
        	    precioPrim.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(3).get());

        	    precioEco.setPrecio(precioEconomica);
        	    precioEje.setPrecio(precioEjecutiva);
        	    precioPrim.setPrecio(precioPrimera);
        	    
        	    List<PrecioAsiento> precios = Arrays.asList(precioEco, precioEje, precioPrim);
        	    for (PrecioAsiento precio : precios) {
        	        //precio.setVuelo(vueloGuardado);  // Asociar el vuelo
        	        precioAsientoService.guardarPrecioAsiento(precio);
        	    }*/

        	    
        	    // Mostrar mensaje de éxito
        	    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Vuelo Agregado"));
        	    PrimeFaces.current().ajax().update("form:messages", "form:dt-vuelos");
        	} catch (DataAccessException e) {
        		selectedVuelo.setIdVuelo(null);
        	    // Capturamos excepciones relacionadas con la base de datos (por ejemplo, el trigger)
        	    Throwable cause = e.getCause();
    
        	    
        	    if (cause instanceof GenericJDBCException jdbcException) {

        	    	  String errorMessage = logJDBCExceptionDetails(jdbcException);

        	          // Si es un error relacionado con la base de datos, mostramos un mensaje específico
        	          FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(errorMessage));
        	      } else {
        	          // Si la excepción no es una SQLException, mostramos el mensaje general de error
        	          FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error al agregar el vuelo: " + e.getMessage()));
        	      }

        	      // Actualizamos los componentes del formulario
        	      PrimeFaces.current().ajax().update("form:messages", "form:dt-vuelos");
        	}catch (Exception e) {
        		 System.out.println("Error al agregar el vuelo: " + e.getMessage());
			}


        } else {
            //var vuelo=vueloService.findById(selectedVuelo.getIdVuelo()).get();
            //vuelo.setPrecio(selectedVuelo.getPrecio());
            //vuelo.setFechaHoraLlegada(selectedVuelo.getFechaHoraLlegada());
            vueloService.updateVuelo(selectedVuelo);  // Editar vuelo existente
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Vuelo Editado"));
            PrimeFaces.current().ajax().update("form:messages", "form:dt-vuelos");
        }
        
      
        
        vuelos = vueloService.findAll();  // Refrescar lista
        nuevoVuelo = new Vuelo();  // Limpiar el formulario

        
        

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
            logger.error("No se encontró SQLException subyacente.");
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
    

    public void deleteVuelo() {
        if (selectedVuelo != null) {
            vueloService.deleteById(selectedVuelo.getIdVuelo());
            vuelos = vueloService.findAll();  // Refrescar lista después de eliminar
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Vuelo Eliminado"));
            PrimeFaces.current().ajax().update("form:messages", "form:dt-vuelos");
        }
    }

    public void deleteSelectedVuelos() {
        if (selectedVuelos != null && !selectedVuelos.isEmpty()) {
            //vueloService.(selectedVuelos);
            vuelos = vueloService.findAll();  // Refrescar lista
        }
    }
    
    public void ajaxListener(AjaxBehaviorEvent event) {
    
        //avionSeleccionado=avionService.findById(Integer.parseInt( avion)).get();
        avionSeleccionado=aviones.get(this.avion);
        seteo();


        
        
    }
    public void editarVuelo(Vuelo vuelo){
        System.out.println("Dentro del editar Vuelo");
        avionSeleccionado=vuelo.getAvion();
        selectedVuelo=vuelo;
        //piloto=selectedVuelo.getPiloto().getRutUsuario();
        seteo();
        /*var precios=precioAsientoService.findByVuelo(selectedVuelo);
        
        //claseAsientoService.obtenerClaseAsientoPorId(1).get();
       precioEconomica=precios.stream()
       		.filter(t -> t.getClaseAsiento().getIdClase()==1)
       		.findFirst()
       		.get()
       		.getPrecio();
       
       precioEjecutiva=precios.stream()
       		.filter(t -> t.getClaseAsiento().getIdClase()==2)
       		.findFirst()
       		.get()
       		.getPrecio();
       
       precioPrimera=precios.stream()
       		.filter(t -> t.getClaseAsiento().getIdClase()==3)
       		.findFirst()
       		.get()
       		.getPrecio();*/
    }
    public void seteo(){
        if (avionSeleccionado != null) {
        	System.out.println("elementos seteados");
            cantidadAsientos = avionSeleccionado.getCapacidadDePasajeros();
            /*asientosPrimeraClase = avionSeleccionado.getCap_primera();
            asientosEjecutiva = avionSeleccionado.getCap_ejecutiva();
            asientosEconomica = avionSeleccionado.getCap_economica();*/
            
            
            /*precioEco.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(1).get());
    		precioEje.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(2).get());
    		precioPrim.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(3).get());*/
            
            
        }
    }
    
    public void reseteo() {
    	precioPrimera=0;
    	precioEconomica=0;
    	precioEjecutiva=0;
    	
        cantidadAsientos = 0;
        asientosPrimeraClase = 0;
        asientosEjecutiva = 0;
        asientosEconomica =0;
    }
    public void onValueChange(ValueChangeEvent event) {
        String newValue = (String) event.getNewValue();
        System.out.println("Nuevo valor seleccionado: " + newValue);
    }

    public void openNew() {
        this.selectedVuelo = new Vuelo();
    }

    // Getters y Setters
    public List<Vuelo> getVuelos() {
        return vuelos;
    }

    public void setVuelos(List<Vuelo> vuelos) {
        this.vuelos = vuelos;
    }

    public Vuelo getSelectedVuelo() {
        return selectedVuelo;
    }

    public void setSelectedVuelo(Vuelo selectedVuelo) {
        this.selectedVuelo = selectedVuelo;
    }

    public List<Vuelo> getSelectedVuelos() {
        return selectedVuelos;
    }

    public void setSelectedVuelos(List<Vuelo> selectedVuelos) {
        this.selectedVuelos = selectedVuelos;
    }

    public Vuelo getNuevoVuelo() {
        return nuevoVuelo;
    }

    public void setNuevoVuelo(Vuelo nuevoVuelo) {
        this.nuevoVuelo = nuevoVuelo;
    }

    public List<Aeropuerto> getListaAeropuertos() {
        return listaAeropuertos;
    }

    public void setListaAeropuertos(List<Aeropuerto> listaAeropuertos) {
        this.listaAeropuertos = listaAeropuertos;
    }

    public List<Avion> getListaAviones() {
        return listaAviones;
    }

    public void setListaAviones(List<Avion> listaAviones) {
        this.listaAviones = listaAviones;
    }

    public List<Piloto> getListaPilotos() {
        return listaPilotos;
    }

    public void setListaPilotos(List<Piloto> listaPilotos) {
        this.listaPilotos = listaPilotos;
    }

    public List<EstadoVuelo> getListaEstadosVuelo() {
        return listaEstadosVuelo;
    }

    public void setListaEstadosVuelo(List<EstadoVuelo> listaEstadosVuelo) {
        this.listaEstadosVuelo = listaEstadosVuelo;
    }

    // Método auxiliar para verificar si hay vuelos seleccionados
    public boolean hasSelectedVuelos() {
        return selectedVuelos != null && !selectedVuelos.isEmpty();
    }

    public void setAeropuerto1(String aeropuerto1) {
        this.aeropuerto1 = aeropuerto1;
    }

    public void setAeropuerto2(String aeropuerto2) {
        this.aeropuerto2 = aeropuerto2;
    }

    public String getAeropuerto1() {
        return aeropuerto1;
    }

    public String getAeropuerto2() {
        return aeropuerto2;
    }

    public String getEstadoVuelo() {
        return estadoVuelo;
    }

    public void setEstadoVuelo(String estadoVuelo) {
        this.estadoVuelo = estadoVuelo;
    }
    
    public String getAvion() {
		return avion;
	}
    public void setAvion(String avion) {
		this.avion = avion;
	}
    public String getPiloto() {
		return piloto;
	}
    
    public void setPiloto(String piloto) {
		this.piloto = piloto;
	}

	public VueloService getVueloService() {
		return vueloService;
	}

	public void setVueloService(VueloService vueloService) {
		this.vueloService = vueloService;
	}

	public Integer getPrecioPrimera() {
		return precioPrimera;
	}

	public void setPrecioPrimera(Integer precioPrimera) {
		this.precioPrimera = precioPrimera;
	}

	public Integer getPrecioEjecutiva() {
		return precioEjecutiva;
	}

	public void setPrecioEjecutiva(Integer precioEjecutiva) {
		this.precioEjecutiva = precioEjecutiva;
	}
  
	public Integer getPrecioEconomica() {
		return precioEconomica;
	}
	
	public void setPrecioEconomica(Integer precioEconomica) {
		this.precioEconomica = precioEconomica;
	}
	
	public Avion getAvionSeleccionado() {
		return avionSeleccionado;
	}
	
	public void setAvionSeleccionado(Avion avionSeleccionado) {
		this.avionSeleccionado = avionSeleccionado;
	}
	
	
	public int getCantidadAsientos() {
		return cantidadAsientos;
	}
	
	public void setCantidadAsientos(int cantidadAsientos) {
		this.cantidadAsientos = cantidadAsientos;
	}
	
	public int getAsientosEconomica() {
		return asientosEconomica;
	}
	public void setAsientosEconomica(int asientosEconomica) {
		this.asientosEconomica = asientosEconomica;
	}
	public int getAsientosEjecutiva() {
		return asientosEjecutiva;
	}
	public void setAsientosEjecutiva(int asientosEjecutiva) {
		this.asientosEjecutiva = asientosEjecutiva;
	}
	public int getAsientosPrimeraClase() {
		return asientosPrimeraClase;
	}
	public void setAsientosPrimeraClase(int asientosPrimeraClase) {
		this.asientosPrimeraClase = asientosPrimeraClase;
	}

	public AeropuertoService getAeropuertoService() {
		return aeropuertoService;
	}

	public void setAeropuertoService(AeropuertoService aeropuertoService) {
		this.aeropuertoService = aeropuertoService;
	}

	public PiloService getPilotoService() {
		return pilotoService;
	}

	public void setPilotoService(PiloService pilotoService) {
		this.pilotoService = pilotoService;
	}

	public AvionService getAvionService() {
		return avionService;
	}

	public void setAvionService(AvionService avionService) {
		this.avionService = avionService;
	}

	public EstadoVueloService getEstadoVueloService() {
		return estadoVueloService;
	}

	public void setEstadoVueloService(EstadoVueloService estadoVueloService) {
		this.estadoVueloService = estadoVueloService;
	}

	public ClaseAsientoService getClaseAsientoService() {
		return claseAsientoService;
	}

	public void setClaseAsientoService(ClaseAsientoService claseAsientoService) {
		this.claseAsientoService = claseAsientoService;
	}

	public PrecioAsientoService getPrecioAsientoService() {
		return precioAsientoService;
	}

	public void setPrecioAsientoService(PrecioAsientoService precioAsientoService) {
		this.precioAsientoService = precioAsientoService;
	}

	public HashMap<String, Aeropuerto> getAeropuertos() {
		return aeropuertos;
	}

	public void setAeropuertos(HashMap<String, Aeropuerto> aeropuertos) {
		this.aeropuertos = aeropuertos;
	}

	public HashMap<String, Piloto> getPilotos() {
		return pilotos;
	}

	public void setPilotos(HashMap<String, Piloto> pilotos) {
		this.pilotos = pilotos;
	}

	public HashMap<String, Avion> getAviones() {
		return aviones;
	}

	public void setAviones(HashMap<String, Avion> aviones) {
		this.aviones = aviones;
	}
	
	
    
    
}