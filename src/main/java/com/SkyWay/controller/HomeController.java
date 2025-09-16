package com.SkyWay.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.util.Logger;
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

import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
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
	

    
    private static final int BUTTONS_TO_SHOW = 5;
    private static final int INITIAL_PAGE = 0;
    private static final int INITIAL_PAGE_SIZE = 5;
    private static final int[] PAGE_SIZES = {5, 10, 20};
	
    @GetMapping("")
    public RedirectView home() {
		Logger.logInfo("dentro del controaldor");
        return new RedirectView("home/index.xhtml");
    }


	/*@GetMapping("")
	public String home() {
		Logger.logInfo("dentro del controaldor");
		return "/home/index.xhtml";
	}*/



    


	
}
