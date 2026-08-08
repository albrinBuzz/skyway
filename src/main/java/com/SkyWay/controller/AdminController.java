package com.SkyWay.controller;


import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.estadovuelo.domain.service.EstadoVueloService;
import com.SkyWay.modules.piloto.domain.service.PiloService;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
	@Autowired
	private PiloService piloService;

	@Autowired
	private AvionService avionService;

	@Autowired
	private AeropuertoService aeropuertoService;


	@Autowired
	private EstadoVueloService estadoVueloService;
	
	@GetMapping("")
	public String agregarVuelo(Model model) {

		model.addAttribute("pilotos", piloService.findAll());
		model.addAttribute("vuelo", new Vuelo());
		model.addAttribute("aviones", avionService.findAll());
		model.addAttribute("estadoVuelos", estadoVueloService.findAll());
		model.addAttribute("aropuertosSalida", aeropuertoService.findAll());
		model.addAttribute("aropuertosLlegada", aeropuertoService.findAll());
		return "perfil/piloto/agregarVuelo";
	}
	
	
}
