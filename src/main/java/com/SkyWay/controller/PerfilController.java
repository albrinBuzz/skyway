package com.SkyWay.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.dto.InfoVueloDTO;
import com.SkyWay.dto.ReservaVueloDTO;
import com.SkyWay.model.Pasajero;
import com.SkyWay.model.Piloto;
import com.SkyWay.model.RolEnum;
import com.SkyWay.model.Usuario;
import com.SkyWay.model.Vuelo;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.claseasiento.domain.service.ClaseAsientoService;
import com.SkyWay.modules.estadovuelo.domain.service.EstadoVueloService;
import com.SkyWay.modules.piloto.domain.service.PiloService;
import com.SkyWay.modules.precioasiento.domain.service.PrecioAsientoService;
import com.SkyWay.modules.reservaasiento.domain.service.ReservaAsientoService;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.vuelo.domain.service.VueloService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

	
	private final Logger LOGGER = LoggerFactory.getLogger(PerfilController.class);
	
	@Autowired
	private VueloService vueloService;
	
	
	@Autowired
	private AsientoService asientoService;
	

	@Autowired
	private ReservaService reservaService;
	
	@Autowired 
	private ReservaAsientoService reservaAsientoService;
	
	@Autowired
	private PiloService piloService;
	
	@Autowired
	private AvionService avionService;
	
	@Autowired
	 private  AeropuertoService aeropuertoService;
	

	@Autowired
	private EstadoVueloService estadoVueloService;
	
	@Autowired
	private ClaseAsientoService claseAsientoService;
	
	@Autowired
	private PrecioAsientoService precioAsientoService;
		
	@GetMapping("")
	public RedirectView home(  Model model,Principal principal,HttpSession session) {

		Usuario usuario=(Usuario) session.getAttribute("usuario");
	
		if(usuario.getRol().getNombre().equals(RolEnum.PASAJERO.getDescripcion())) {

			 return new RedirectView("perfil/pasajero/index.xhtml");
		}else if (usuario.getRol().getNombre().equals(RolEnum.PILOTO.getDescripcion())) {
			
			 return new RedirectView("perfil/piloto/index.xhtml");
		}
		else {
			 return new RedirectView("perfil/pasajero/index.xhtml");
		}
        
		//return "protegido/index";
	}
	
	
	
	@GetMapping("/agregarVuelo")
	public String agregarVuelo(Model model) {
		
		model.addAttribute("pilotos", piloService.findAll());
		model.addAttribute("vuelo", new Vuelo());
		model.addAttribute("aviones", avionService.findAll());
		model.addAttribute("estadoVuelos", estadoVueloService.findAll());
		model.addAttribute("aropuertosSalida", aeropuertoService.findAll());
		model.addAttribute("aropuertosLlegada", aeropuertoService.findAll());
		return "perfil/piloto/agregarVuelo";
	}
	
	
	
	@PostMapping("/agregarVuelo")
	public String agregarVuelo(Model model,Vuelo vuelo,@RequestParam("precioPrimera") Integer precioPrimera,
			@RequestParam("precioEjecutiva") Integer precioEjecutiva,
			@RequestParam("precioEconomica") Integer precioEconomica) {
		
		model.addAttribute("pilotos", piloService.findAll());
		model.addAttribute("vuelo", new Vuelo());
		model.addAttribute("aviones", avionService.findAll());
		model.addAttribute("estadoVuelos", estadoVueloService.findAll());
		model.addAttribute("aropuertosSalida", aeropuertoService.findAll());
		model.addAttribute("aropuertosLlegada", aeropuertoService.findAll());
		
		System.out.println(vuelo);
		
		System.out.println(precioPrimera+" "+precioEjecutiva+" "+precioEconomica);

		/*var vueloGuardado=vueloService.save(vuelo);
		
		
		PrecioAsiento precioEco=new PrecioAsiento();
		PrecioAsiento precioPrim=new PrecioAsiento();;
		PrecioAsiento precioEje=new PrecioAsiento();;
		
		precioEco.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(1).get());
		precioEje.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(2).get());
		precioPrim.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(3).get());
		
		precioEco.setPrecio(precioEconomica);
		precioEje.setPrecio(precioEjecutiva);
		precioPrim.setPrecio(precioPrimera);
		
		precioEco.setVuelo(vueloGuardado);
		precioEje.setVuelo(vueloGuardado);
		precioPrim.setVuelo(vueloGuardado);
		
		precioAsientoService.guardarPrecioAsiento(precioEje);
		precioAsientoService.guardarPrecioAsiento(precioEco);
		precioAsientoService.guardarPrecioAsiento(precioPrim);*/
		
		return "perfil/piloto/agregarVuelo";
	}
	
	
	

	@GetMapping("/detalleReserva/{id}")
	public String detalleReserva(@PathVariable Integer id,Model model,Principal principal,HttpSession session) {
		
		LOGGER.info("Id de reserva: {}",id);

		//model.addAttribute("usuario", session.getAttribute("usuario"));
	
		/*Reserva reserva=reservaService.findById(id).get();
		BoletoDTO boleto=reservaService.getBoleto(id);
		
		model.addAttribute("reserva", reserva);
		model.addAttribute("boleto", boleto);*/
		
		
		
		return "perfil/detalleVuelo";
		//return "protegido/index";
	}
	
	@GetMapping("/boleto/{id}")
	public String boleto(@PathVariable Integer id,Model model,Principal principal,HttpSession session) {
		
		LOGGER.info("Id de reserva: {}",id);

		//model.addAttribute("usuario", session.getAttribute("usuario"));
	
		/*Reserva reserva=reservaService.findById(id).get();
		BoletoDTO boleto=reservaService.getBoleto(id);
		model.addAttribute("reserva", reserva);
		model.addAttribute("boleto", boleto);*/
		
		return "perfil/boleto";
		//return "protegido/index";
	}
	
	
	
	public String detalleVuelo() {
		
		return "perfil/detalleVueloPiloto";
	}
	
	@GetMapping("/cambiarAsiento/{idVuelo}/{idReserva}")
	public String cambiarAsiento(@PathVariable Integer idVuelo ,@PathVariable Integer idReserva,Model model ) {
		
		InfoVueloDTO vuelo=vueloService.getInfoVuelo(idVuelo);
		
		//pasar el objet del InfoVueloDTO al modelo
		
		
		//List<Asiento> asientos=asientoService.findByAvion(avionService.findById(vuelo.getIdAvion()).get());
		List<InfoAsientoDTO>asientos=asientoService.getAsientosVuelo(idReserva, idVuelo);
		List<InfoAsientoDTO>asientoSeleccionados=asientos.stream()
				.filter((as)->as.getEstado().equals("seleccionado"))
				.toList();
		
		model.addAttribute("asientos", asientos);
		model.addAttribute("asientosSeleccionados", asientoSeleccionados);
		model.addAttribute("vuelo", vuelo);
		model.addAttribute("totalAsientos", asientoSeleccionados.size());
		model.addAttribute("idReserva", idReserva);
		
		List<List<InfoAsientoDTO>> asientosParticionados = new ArrayList<>();
		for (int i = 0; i < asientos.size(); i += 6) {
		    asientosParticionados.add(asientos.subList(i, Math.min(i + 6, asientos.size())));
		}
		model.addAttribute("asientosParticionados", asientosParticionados);

		
		
		return "perfil/cambiarAsiento";
	}
	
	
	@GetMapping("/cancelarReserva/{id}")
	public String cancelarReserva(@PathVariable Integer id,  Model model,Principal principal,HttpSession session) {
	
		
		
		reservaService.cancelarReserva(id);
		
		Usuario usuario=(Usuario) session.getAttribute("usuario");
		model.addAttribute("usuario", session.getAttribute("usuario"));
		
		if(usuario instanceof Pasajero) {
			List<ReservaVueloDTO>vuelos=reservaService.getReservasUsuario(((Usuario) session.getAttribute("usuario")).getRutUsuario());
			model.addAttribute("vuelos", vuelos);
			return "perfil/perfil";
		}else {
			Piloto piloto=(Piloto) session.getAttribute("usuario");
			model.addAttribute("piloto", piloto);
			
			return "perfil/perfilPiloto";
		}

	}
	
	
	@PostMapping("/cambiar")
    public ModelAndView cambiarAsiento(@RequestParam("asientoOriginal") String asientoOriginal,
                                  @RequestParam("asientoReemplazo") String asientoReemplazo,
                                  @RequestParam("idVuelo") Integer idVuelo,
                                  @RequestParam("idReserva") Integer idReserva) {
        // Mostrar los valores recibidos en la consola (para depuración)
        System.out.println("Asiento Original: " + asientoOriginal);
        System.out.println("Asiento Reemplazo: " + asientoReemplazo);
        System.out.println("ID Vuelo: " + idVuelo);
        System.out.println("ID Reserva: " + idReserva);
		//LOGGER.info("Asiento Reemplazo: " + asientoReemplazo);
		
        
        System.out.println("Salida : " + reservaAsientoService.cambiarAsiento(Integer.parseInt( asientoReemplazo), idReserva,Integer.parseInt( asientoOriginal)));

		return new ModelAndView("redirect:/perfil/cambiarAsiento/"+idVuelo+"/"+idReserva);

        // Aquí puedes realizar la lógica para cambiar el asiento, por ejemplo, actualizar la base de datos

    }
	
	@GetMapping("/getData")
    @ResponseBody
    public String getData() {
        // Aquí puedes poner cualquier lógica para obtener datos, por ejemplo:
        String data = "Hola, este es un mensaje desde el servidor!";
        LOGGER.info(data);
        return data;
    }
	
}
