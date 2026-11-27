package es.safareyes.smartchef.seguridad;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final UsuarioDetallesService usuarioDetallesService;
    private final RespuestasSeguridad respuestasSeguridad;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(respuestasSeguridad)
                        .accessDeniedHandler(respuestasSeguridad))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/v1/auth/**", "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/fotos/*", "/api/v1/recetas", "/api/v1/recetas/*",
                                "/api/v1/ingredientes", "/api/catalogos/*", "/api/recetas", "/api/recetas/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/recetas", "/api/v1/recetas/*/fotos")
                        .hasAnyRole(Roles.CHEF, Roles.ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/recetas/*").hasAnyRole(Roles.CHEF, Roles.ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/recetas/*", "/api/v1/recetas/*/fotos/*")
                        .hasAnyRole(Roles.CHEF, Roles.ADMIN)
                        .requestMatchers("/api/v1/informes/**").hasAnyRole(Roles.ADMIN, Roles.CHEF)
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtFiltro(jwtService, usuarioDetallesService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
