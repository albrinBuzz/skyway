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

	@Bean
	DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(usService);
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
		return new ProviderManager(authenticationProvider());
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable()) // Deshabilitar CSRF
				.authorizeHttpRequests(authorizeRequests ->
						authorizeRequests
								.requestMatchers("/perfil/**").authenticated()
								.requestMatchers("/home/login.xhtml").permitAll()
								.requestMatchers("/perfil/agregarVuelo").hasRole("admin")
								.requestMatchers("/protegido/admin/**").hasAuthority("admin")
								.requestMatchers("/protegido/**").hasAnyAuthority("usuario", "admin")
								.requestMatchers("/**").permitAll()
								.anyRequest().authenticated()
				)
				.exceptionHandling(exceptionHandling ->
						exceptionHandling
								.accessDeniedPage("/errors/403")
				)
				.formLogin(formLogin ->
						formLogin
								.loginPage("/home/login.xhtml")
								.loginProcessingUrl("/login")
								.defaultSuccessUrl("/", true)
								.permitAll()
								.successHandler(authenticationSuccessHandler)
				)
				.logout(logout ->
						logout
								.invalidateHttpSession(true)
								.clearAuthentication(true)
								.logoutUrl("/logout")
								.logoutSuccessUrl("/")
								.permitAll()
				);

		return http.build();
	}
}