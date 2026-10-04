package com.SkyWay.mobile.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.*;
import javax.crypto.spec.SecretKeySpec;
import java.time.*;
import java.security.SecureRandom;
import java.util.*;

@Configuration
public class MobileSecurityConfig {
    public static final String ISSUER = "skyway-mobile";
    public static final String AUDIENCE = "skyway-passengers";

    @Bean
    Clock mobileClock(@Value("${skyway.mobile.zone:America/Santiago}") String zone) {
        return Clock.system(ZoneId.of(zone));
    }

    @Bean
    SecretKeySpec mobileJwtKey(@Value("${skyway.mobile.jwt-secret:}") String encoded) {
        byte[] bytes;
        if (encoded == null || encoded.isBlank()) {
            bytes = new byte[48];
            new SecureRandom().nextBytes(bytes);
            org.slf4j.LoggerFactory.getLogger(MobileSecurityConfig.class).warn(
                    "MOBILE_JWT_SECRET no configurado: se usa una clave efímera solo para esta ejecución. Configúrala en ambientes compartidos/producción.");
        } else {
            try { bytes = Base64.getDecoder().decode(encoded); }
            catch (IllegalArgumentException ex) { throw new IllegalStateException("MOBILE_JWT_SECRET debe ser Base64 válido."); }
            if (bytes.length < 32) throw new IllegalStateException("MOBILE_JWT_SECRET necesita al menos 32 bytes aleatorios.");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder mobileJwtEncoder(SecretKeySpec key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    JwtDecoder mobileJwtDecoder(SecretKeySpec key) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> audience = jwt -> jwt.getAudience().contains(AUDIENCE)
                && jwt.getSubject() != null && jwt.getExpiresAt() != null
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(ISSUER), audience));
        return decoder;
    }

    @Bean
    @Order(1)
    SecurityFilterChain mobileFilterChain(HttpSecurity http, JwtDecoder decoder,
            ObjectMapper mapper, @Value("${skyway.mobile.allowed-origins:http://localhost:8100,http://localhost,https://localhost,capacitor://localhost}") String origins) throws Exception {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setAllowCredentials(false);
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/mobile/**", cors);
        var entry = (org.springframework.security.web.AuthenticationEntryPoint) (req, res, ex) -> {
            res.setStatus(401); res.setContentType("application/json");
            mapper.writeValue(res.getOutputStream(), Map.of("codigo", "NO_AUTENTICADO", "mensaje", "Inicia sesión nuevamente."));
        };
        var denied = (org.springframework.security.web.access.AccessDeniedHandler) (req, res, ex) -> {
            res.setStatus(403); res.setContentType("application/json");
            mapper.writeValue(res.getOutputStream(), Map.of("codigo", "ACCESO_DENEGADO", "mensaje", "Acceso no permitido."));
        };
        // AntPathRequestMatcher evita ambigüedad entre FacesServlet y DispatcherServlet.
        http.securityMatcher(new AntPathRequestMatcher("/api/v1/mobile/**"))
            .cors(c -> c.configurationSource(source))
            .csrf(c -> c.disable()) // API exclusivamente Bearer; no admite cookies de sesión.
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache(c -> c.disable())
            .formLogin(c -> c.disable()).httpBasic(c -> c.disable()).logout(c -> c.disable())
            .authorizeHttpRequests(a -> a
                .requestMatchers(new AntPathRequestMatcher("/api/v1/mobile/**", HttpMethod.OPTIONS.name())).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/mobile/auth/login", "POST")).permitAll()
                .anyRequest().hasAuthority("SCOPE_mobile"))
            .exceptionHandling(e -> e.authenticationEntryPoint(entry).accessDeniedHandler(denied))
            .oauth2ResourceServer(o -> o.jwt(j -> j.decoder(decoder)).authenticationEntryPoint(entry).accessDeniedHandler(denied));
        return http.build();
    }
}
