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
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

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
		MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector){
			   return new MvcRequestMatcher.Builder(introspector);
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


	    @Bean
	    public SecurityFilterChain filterChain(HttpSecurity http,MvcRequestMatcher.Builder mvc) throws Exception {
	        http
	            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF (Considerar habilitar en producción)
	            .authorizeHttpRequests(authorizeRequests ->
	                authorizeRequests
	                	.requestMatchers("/perfil/**").authenticated()
						//.requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
						.requestMatchers(mvc.pattern("/home/login.xhtml")).permitAll()
	                  	//.requestMatchers("/booking").authenticated()
	                    //.requestMatchers(HttpMethod.DELETE).hasRole("admin")
	                    .requestMatchers("/perfil/agregarVuelo").hasRole("admin")
	                    .requestMatchers("/protegido/admin/**").hasAuthority("admin")
	                    .requestMatchers("/protegido/**").hasAnyAuthority("usuario", "admin")
	                    .requestMatchers("/**").permitAll()
	                    .anyRequest().authenticated()
	            )

	            .exceptionHandling(exceptionHandling ->
	                exceptionHandling

	                    .accessDeniedPage("/errors/403") // Ruta correcta para la página 403
	            )
	            .formLogin(formLogin ->
	                formLogin
	                	.loginPage("/home/login.xhtml")
	                	.loginProcessingUrl("/login")

	                    .defaultSuccessUrl("/")
	                    .permitAll().successHandler(authenticationSuccessHandler)
	            )
	            .logout(logout ->
	                logout
	                    .invalidateHttpSession(true)
	                    .clearAuthentication(true)
	                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
	                    .logoutSuccessUrl("/")
	                    .permitAll()
	            );

	        return http.build();
	    }
}
