package com.uade.tpo.foodmarketplace.security;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Define autorización por endpoint; la autenticación identifica quién llama y los Services validan ownership. */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationProvider authenticationProvider,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/platos/**", "/chef-profiles/**", "/resenas/**",
                                "/categories/**", "/ingredientes/**").permitAll()
                        .requestMatchers("/users/me").authenticated()
                        .requestMatchers("/users/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/platos/**").hasAuthority("CHEF")
                        .requestMatchers(HttpMethod.PUT, "/platos/**").hasAnyAuthority("CHEF", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/platos/**").hasAnyAuthority("CHEF", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/chef-profiles/**").hasAuthority("CHEF")
                        .requestMatchers(HttpMethod.PUT, "/chef-profiles/**").hasAnyAuthority("CHEF", "ADMIN")
                        .requestMatchers("/categories/**", "/ingredientes/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/resenas/**").hasAuthority("CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/resenas/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/resenas/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/domicilios/**", "/orders/**", "/pagos/**")
                                .hasAuthority("CLIENTE")
                        .requestMatchers("/domicilios/**", "/orders/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/pagos/**").hasAuthority("ADMIN")
                        .requestMatchers("/pagos/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers("/subpedidos/**").hasAnyAuthority("CHEF", "ADMIN")
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
