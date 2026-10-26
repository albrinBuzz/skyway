package com.SkyWay.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.SkyWay.modules.usuario.domain.service.UsuarioService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {


	@Autowired
	private UsuarioService usService;


	@Autowired
	AuthenticationSuccessHandler authenticationSuccessHandler;

	   @Bean
	    public static PasswordEncoder passwordEncoder(){
	        return new BCryptPasswordEncoder();
	    }

	   @Bean
	    public MessageSource messageSource() {
	        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
	        messageSource.setBasename("classpath:messages");
	        messageSource.setDefaultEncoding("UTF-8");
	        return messageSource;
	    }

	    /*@Autowired
	    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
	        auth.userDetailsService(usService) .passwordEncoder(passwordEncoder());
	    }*/


	    @Bean
	    DaoAuthenticationProvider authenticationProvider() {
	        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
	        authProvider.setUserDetailsService(usService);
	        authProvider.setPasswordEncoder(passwordEncoder());

	        return authProvider;
	    }

	    @Bean
	    public SecurityContextRepository securityContextRepository() {
	        return new HttpSessionSecurityContextRepository();
	    }

	    @Bean
	    public AuthenticationManager authenticationManager(
	            UserDetailsService userDetailsService,
	            PasswordEncoder passwordEncoder) {
	        var provider = new DaoAuthenticationProvider();

	        provider.setUserDetailsService(userDetailsService);
	        provider.setPasswordEncoder(passwordEncoder);
	        return new ProviderManager(authenticationProvider());
	    }


	    /** CSP con 'unsafe-inline' porque los .xhtml aun tienen scripts/estilos inline. TODO: migrar a nonce. */
	    private static final String CSP = "default-src 'self'; "
	            + "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://unpkg.com https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://use.fontawesome.com; "
	            + "style-src 'self' 'unsafe-inline' https://unpkg.com https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://use.fontawesome.com https://fonts.googleapis.com; "
	            + "font-src 'self' data: https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://use.fontawesome.com https://fonts.gstatic.com; "
	            + "img-src 'self' data: blob: https:; "
	            + "connect-src 'self' ws: wss: https://nominatim.openstreetmap.org https://unpkg.com https://cdn.jsdelivr.net; "
	            + "object-src 'none'; base-uri 'self'; frame-ancestors 'self'";

	    @Bean
	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	        // Postbacks JSF: el ViewState (ligado a la sesion) hace de token anti-CSRF.
	        RequestMatcher jsfPostback = req -> req.getParameter("jakarta.faces.ViewState") != null;
	        RequestMatcher ws = antMatcher("/ws-asientos/**");
	        RequestMatcher wsMetrics = antMatcher("/ws-metrics/**");

	        http
	            .csrf(csrf -> csrf
	                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
	                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
	                .ignoringRequestMatchers(jsfPostback, ws, wsMetrics, antMatcher("/api/auth/**")))
	            .sessionManagement(sm -> sm.sessionFixation(sf -> sf.changeSessionId()))
	            .headers(h -> h
	                .contentSecurityPolicy(c -> c.policyDirectives(CSP))
	                .frameOptions(f -> f.sameOrigin())
	                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
	                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
	            .authorizeHttpRequests(a -> a
	                // --- Administracion ---
	                .requestMatchers(antMatcher("/admin/**")).hasRole("ADMIN")
	                .requestMatchers(antMatcher("/ws-metrics/**")).hasRole("ADMIN")
	                .requestMatchers(antMatcher("/api/health"), antMatcher("/api/health/")).hasRole("ADMIN")
	                .requestMatchers(antMatcher("/api/health/liveness")).permitAll()
	                .requestMatchers(antMatcher("/perfil/agregarVuelo")).hasRole("ADMIN")
	                .requestMatchers(antMatcher("/perfil/**")).authenticated()
	                .requestMatchers(antMatcher("/blog/agregarPost.xhtml")).authenticated()
	                // --- Publico: login, recursos, retornos de pago ---
	                .requestMatchers(antMatcher("/home/login.xhtml"), antMatcher("/login")).permitAll()
	                .requestMatchers(antMatcher("/jakarta.faces.resource/**"), antMatcher("/javax.faces.resource/**"),
	                        antMatcher("/resources/**"), antMatcher("/favicon.ico"), antMatcher("/error/**"),
	                        antMatcher("/errors/**"), antMatcher("/error")).permitAll()
	                .requestMatchers(antMatcher("/webpay/**"), antMatcher("/paypal/**"), antMatcher("/mercadopago/**")).permitAll()
	                .requestMatchers(antMatcher("/ws-asientos/**")).permitAll() // el canal STOMP exige principal (WebSocketConfig)
	                .requestMatchers(antMatcher("/api/auth/**")).permitAll()
	                // --- API: lectura publica solo de catalogo de vuelos/itinerarios ---
	                .requestMatchers(antMatcher(HttpMethod.GET, "/api/itinerarios/**"),
	                        antMatcher(HttpMethod.GET, "/api/vuelos/**"),
	                        antMatcher(HttpMethod.GET, "/api/segmentos/**"),
	                        antMatcher(HttpMethod.GET, "/api/capacidadclase/**"),
	                        antMatcher(HttpMethod.GET, "/api/puertas/**")).permitAll()
	                .requestMatchers(antMatcher(HttpMethod.GET, "/api2/vuelos"), antMatcher(HttpMethod.GET, "/api2/vuelos/")).permitAll()
	                .requestMatchers(antMatcher(HttpMethod.GET, "/api2/vuelos/{id:[0-9]+}")).permitAll()
	                // --- Resto de API: escritura solo ADMIN; lectura autenticada ---
	                .requestMatchers(antMatcher("/api/**"), antMatcher("/api2/**"), antMatcher("/avion/**"))
	                    .access((auth, ctx) -> new AuthorizationDecision(
	                        auth.get().isAuthenticated() && !(auth.get() instanceof AnonymousAuthenticationToken)
	                        && ("GET".equals(ctx.getRequest().getMethod())
	                            || auth.get().getAuthorities().stream().anyMatch(g -> "ROLE_ADMIN".equals(g.getAuthority())))))
	                // --- Paginas JSF/estaticas publicas (home, booking, blog, etc.) ---
	                .requestMatchers(antMatcher("/"), antMatcher("/home/**"), antMatcher("/blog/**"),
	                        antMatcher("/template/**"), antMatcher("/*.xhtml")).permitAll()
	                .anyRequest().authenticated()
	            )
	            .exceptionHandling(e -> e.accessDeniedPage("/errors/403"))
	            .formLogin(f -> f
	                .loginPage("/home/login.xhtml")
	                .loginProcessingUrl("/login")
	                .defaultSuccessUrl("/")
	                .permitAll().successHandler(authenticationSuccessHandler)
	            )
	            .logout(l -> l
	                .invalidateHttpSession(true)
	                .clearAuthentication(true)
	                .deleteCookies("JSESSIONID")
	                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
	                .logoutSuccessUrl("/")
	                .permitAll()
	            );

	        return http.build();
	    }
}
