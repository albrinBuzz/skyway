package com.SkyWay.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.SkyWay.dto.InfoAsientoDTO;
import com.SkyWay.dto.InfoVueloDTO;

import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.pasajero.domain.service.PasajeroService;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.vuelo.domain.service.VueloService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
public class HomeController {
	
	private final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);
	
	@Autowired
    private VueloService vueloService;
	
	@Autowired
	private AsientoService asientoService;

	@Autowired
	private AvionService avionService;
	
	@Autowired
	private PasajeroService pasajeroService;
	
	@Autowired 
	private ReservaService reservaService;
    
    private static final int BUTTONS_TO_SHOW = 5;
    private static final int INITIAL_PAGE = 0;
    private static final int INITIAL_PAGE_SIZE = 5;
    private static final int[] PAGE_SIZES = {5, 10, 20};
	
    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView("home/index.xhtml");
    }
    
	/*@GetMapping("")
	public String home(Model model,Principal principal,
			@RequestParam("page") Optional<Integer> page,
	        @RequestParam("size") Optional<Integer> size) {
		
		  int currentPage = page.orElse(1);
		  int pageSize = size.orElse(3);
	    
	    // Obtén la lista de vuelos (puedes optimizar para no cargarla toda si la base de datos es grande)
	    List<InfoVueloDTO> vuelosLista = vueloService.vuelosProximos();
	    
	    // Paginación
	    Page<InfoVueloDTO> vuelos = paginarVuelos(vuelosLista, PageRequest.of(currentPage - 1, pageSize));
	    
	    // Número total de páginas
	    int totalPages = vuelos.getTotalPages();
	    
	    if (totalPages > 0) {
	        List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
	                .boxed()
	                .collect(Collectors.toList());
	        model.addAttribute("pageNumbers", pageNumbers);  // Aquí pasamos los números de página
	    }
	    
	    // Agrega al modelo los datos necesarios para la vista
	    model.addAttribute("vuelosProx", vuelos);
	    model.addAttribute("pagina", currentPage);
		
		return "home/index";
		//return "protegido/index";
	}*/
	
	
	 @GetMapping("/login")
	    public String showLoginPage(Model model) {
	        return "home/login"; // the name of the Thymeleaf template (login.html)
	    }
	
	

	private Page<InfoVueloDTO> paginarVuelos(List<InfoVueloDTO> vuelosLista, Pageable pageable) {
		
		
	    int pageSize = pageable.getPageSize();
	    int currentPage = pageable.getPageNumber();
	    int startItem = currentPage * pageSize;
	    
	    // Sublista de los vuelos que corresponden a la página actual
	    List<InfoVueloDTO> vuelos;
	    int idx = Math.min(startItem + pageSize, vuelosLista.size());
	    vuelos = vuelosLista.subList(startItem, idx);
	    
	    // Devuelve una página con los vuelos correspondientes
	    return new PageImpl<>(vuelos, pageable, vuelosLista.size());
	}

	
	
	@GetMapping("/buscar_vuelos")
	public String buscarVuelos( @RequestParam String departureCity,
            @RequestParam String arrivalCity,
            @RequestParam String departureDate,
            @RequestParam(required = false) String returnDate,
            @RequestParam String classType,
            Model model) {

	
  		List<InfoVueloDTO> vuelos= vueloService.buscarVuelo(departureCity, arrivalCity, departureDate, returnDate);
  		model.addAttribute("vuelos", vuelos);
		
  		 int currentPage = 1;
		  int pageSize = 3;
	    
	    // Obtén la lista de vuelos (puedes optimizar para no cargarla toda si la base de datos es grande)
	    List<InfoVueloDTO> vuelosLista = vueloService.vuelosProximos();
	    
	    // Paginación
	    Page<InfoVueloDTO> vuelosPagiandos = paginarVuelos(vuelosLista, PageRequest.of(currentPage - 1, pageSize));
	    
	    // Número total de páginas
	    int totalPages = vuelosPagiandos.getTotalPages();
	    
	    if (totalPages > 0) {
	        List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
	                .boxed()
	                .collect(Collectors.toList());
	        model.addAttribute("pageNumbers", pageNumbers);  // Aquí pasamos los números de página
	    }
	    
	    // Agrega al modelo los datos necesarios para la vista
	    model.addAttribute("vuelosProx", vuelosPagiandos);
	    model.addAttribute("pagina", currentPage);
  		
  		//List<InfoVueloDTO>vuelosProx=vueloService.vuelosProximos();
		//model.addAttribute("vuelosProx", vuelosProx);
  		
		return "home/index";
	}
	@GetMapping("/booking")
	public String  reserva(@RequestParam("vueloId")Integer vueloId,Model model) {
		
		
		InfoVueloDTO vuelo=vueloService.getInfoVuelo(vueloId);
		
		
		
		//pasar el objet del InfoVueloDTO al modelo
		
		
		//List<Asiento> asientos=asientoService.findByAvion(avionService.findById(vuelo.getIdAvion()).get());
		List<InfoAsientoDTO>asientos=asientoService.getAsientosDisponibles(vuelo.getIdVuelo());
		model.addAttribute("asientos", asientos);
		model.addAttribute("vuelo", vuelo);
		
		List<List<InfoAsientoDTO>> asientosParticionados = new ArrayList<>();
		for (int i = 0; i < asientos.size(); i += 6) {
		    asientosParticionados.add(asientos.subList(i, Math.min(i + 6, asientos.size())));
		}
		model.addAttribute("asientosParticionados", asientosParticionados);

		return "home/booking";
	}
	
	
	
	
	@PostMapping("/reservar")
	public String reservar(@RequestParam("asientosReservados") String asientosReservados,
			@RequestParam ("idVuelo") Integer idVuelo,
			Model model,HttpSession session) {
		
		//recibir el InfoVueloDTO del modelo como @ModelAttribute 
		
		
		/*
		
		 <form action="#" th:action="@{/producto}" method="post" th:object="${producto}">
	    <input type="hidden" th:field="*{nombre}" />
	    <input type="hidden" th:field="*{precio}" />
	    <!-- Mostrar otros atributos que deseas que sean visibles -->
	    <p>Nombre: <span th:text="${producto.nombre}"></span></p>
	    <p>Precio: <span th:text="${producto.precio}"></span></p>
	    <button type="submit">Enviar</button>
		</form>

		
		 * */
		
		
		// Procesar la reserva de asientos
		  String[] asientosSeleccionados = asientosReservados.split(",");

		  
		  ArrayList<ReservaAsiento>asientos=new ArrayList<ReservaAsiento>();
		  Reserva reserva=new Reserva();
		  ReservaAsiento reservaAsiento;
		  
		  for (int i = 0; i < asientosSeleccionados.length; i++) {
			  var	asiento= asientoService.findById(Integer.parseInt( asientosSeleccionados[i]));
			  reservaAsiento=new ReservaAsiento();
			  reservaAsiento.setAsiento(asiento.get());
			  reservaAsiento.setReserva(reserva);
			  asientos.add(reservaAsiento);
		  }
		 
		  
		  		/*if (asientosSeleccionados==null||asientosSeleccionados.length==0 ) {
			model.addAttribute("mensaje", "Por favor, selecciona al menos un asiento.");
			return "home/booking"; // Regresar a la vista de reserva
		}*/
		  
		  

		var pasajero= (Usuario) session.getAttribute("usuario");
		
		/*if (pasajero instanceof Pasajero) {
			LOGGER.info("Informacion del pasajero {} ", pasajero);
			reserva.setPasajero((Pasajero) pasajero);
			//reserva.setVuelo(vueloService.findById(idVuelo).get());
			
			//LOGGER.info("Informacion del pasajero {} ",informacionPasjero);
			
			LOGGER.info("Informacion del pasajero {} ", pasajero);
			
//			LOGGER.info("Informacion del vuelo {} Avion {}",vueloService.findById(idVuelo).get());
			
			//asientos.forEach(System.out::println);
			
			EstadoReserva esadoReserva=new EstadoReserva();
			esadoReserva.setIdEstadoReserva(1);
			reserva.setEstadoReservaBean(esadoReserva);
			reserva.setReservaAsientos(asientos);
			reserva.setFechaReserva(new Timestamp(new Date().getTime()));
			reservaService.save(reserva);
		}*/

		
		
		
		
		

		model.addAttribute("mensaje", "Reserva confirmada para los asientos: " + String.join(", ", asientosSeleccionados));
		return "redirect:/"; // Redirigir a una página de confirmación
		
	}
	
	
	
}
