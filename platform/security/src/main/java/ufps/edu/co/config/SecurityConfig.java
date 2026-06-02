package ufps.edu.co.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import ufps.edu.co.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

        // Rutas totalmente públicas: autenticación, documentación y catálogos de consulta.
        private static final String[] PUBLIC_PATHS = {
                        "/auth/login",
                        "/auth/refresh",
                        "/api/application/case/login/recoveryPassword",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**",
                        "/error",
                        "/api/application/case/inscripciones/**",
                        "/api/application/case/wompi/**",
                        "/api/application/case/aspirantes/confirmar-correo"
        };

        // Rutas del director de programa: bloque propio y bloque compartido.
        private static final String[] DIRECTOR_PROGRAMA_CORE_PATHS = {
                        "/api/application/case/director-programa/**",
                        "/api/application/case/director-posgrados/**"
        };

        private static final String[] DIRECTOR_PROGRAMA_SHARED_PATHS = {
                        "/api/application/case/cohortes/**",
                        "/api/application/case/documentos/**",
                        "/api/dev/endpoint/tipoentrevista/listall",
                        "/api/dev/endpoint/estado/listall"
        };

        private static final String[] DIRECTOR_PROGRAMA_PATHS = concatPaths(
                        DIRECTOR_PROGRAMA_CORE_PATHS,
                        DIRECTOR_PROGRAMA_SHARED_PATHS);

        // Rutas de administración académica para posgrados.
        private static final String[] POSGRADOS_PATHS = {
                        "/api/dev/endpoint/otrosvalores/listall",
                        "/api/dev/endpoint/sedes/listall",
                        "/api/dev/endpoint/administrativo/listall",
                        "/api/dev/endpoint/facultad/listall",
                        "/api/dev/endpoint/programa/listbyfacultad",
                        "/api/dev/endpoint/programa/listall",
                        "/api/dev/endpoint/estado/listall",
                        "/api/dev/endpoint/cohortes/listall"
        };

        // Rutas del aspirante: consulta y operación sobre inscripciones.
        private static final String[] ASPIRANTE_PATHS = {
                        "/api/application/case/aspirantes/**",
                        "/api/application/case/inscripciones/**"
        };

        // Rutas compartidas entre director de programa y posgrados.
        private static final String[] DIRECTOR_PROGRAMA_POSGRADOS_PATHS = {
                        "/api/dev/endpoint/semestre/listall"
        };

        // Catch-all: solo super admin.
        private static final String[] SUPER_ADMIN_PATHS = {
                        "/**"
        };

        private static String[] concatPaths(String[]... groups) {
                return Arrays.stream(groups)
                                .flatMap(Arrays::stream)
                                .toArray(String[]::new);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource,
                        JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth

                                                // Permite todas las solicitudes OPTIONS para CORS preflight.
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                                // Rutas públicas sin autenticación.
                                                .requestMatchers(PUBLIC_PATHS)
                                                .permitAll()

                                                // Posgrados y super admin.
                                                .requestMatchers(POSGRADOS_PATHS)
                                                .hasAnyRole("SUPER_ADMINISTRADOR", "POSGRADOS")

                                                .requestMatchers(DIRECTOR_PROGRAMA_PATHS)
                                                .hasAnyRole("SUPER_ADMINISTRADOR", "DIRECTOR_DE_PROGRAMA", "POSGRADOS")

                                                .requestMatchers(DIRECTOR_PROGRAMA_POSGRADOS_PATHS)
                                                .hasAnyRole("SUPER_ADMINISTRADOR", "DIRECTOR_DE_PROGRAMA", "POSGRADOS")

                                                // Aspirante, director de programa y super admin.
                                                .requestMatchers(ASPIRANTE_PATHS)
                                                .hasAnyRole("SUPER_ADMINISTRADOR", "DIRECTOR_DE_PROGRAMA", "ASPIRANTE")

                                                // Catch-all para cualquier otra ruta: solo super admin.
                                                .requestMatchers(SUPER_ADMIN_PATHS)
                                                .hasRole("SUPER_ADMINISTRADOR")

                                                // Cualquier otra solicitud no contemplada se deniega.
                                                .anyRequest().denyAll())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
