package com.hackathon.securestarter.config;

import com.hackathon.securestarter.security.JwtAuthenticationEntryPoint;
import com.hackathon.securestarter.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Disable CSRF (using JWT, stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless session management
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Exception handling
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - No authentication required
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // Swagger/API docs (optional, for development)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ===== VEHICLE RBAC =====
                        // Write: FLEET_MANAGER only
                        .requestMatchers(HttpMethod.POST, "/api/vehicles/**").hasRole("FLEET_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/vehicles/**").hasRole("FLEET_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/vehicles/**").hasRole("FLEET_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/vehicles/**").hasRole("FLEET_MANAGER")
                        // Read: FLEET_MANAGER, DISPATCHER, SAFETY_OFFICER, FINANCIAL_ANALYST
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/**").hasAnyRole("FLEET_MANAGER", "DISPATCHER", "SAFETY_OFFICER", "FINANCIAL_ANALYST")

                        // ===== TRIP RBAC =====
                        // Write: DISPATCHER only
                        .requestMatchers(HttpMethod.POST, "/api/trips/**").hasRole("DISPATCHER")
                        .requestMatchers(HttpMethod.PUT, "/api/trips/**").hasRole("DISPATCHER")
                        .requestMatchers(HttpMethod.PATCH, "/api/trips/**").hasRole("DISPATCHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/trips/**").hasRole("DISPATCHER")
                        // Read: DISPATCHER, FLEET_MANAGER, FINANCIAL_ANALYST
                        .requestMatchers(HttpMethod.GET, "/api/trips/**").hasAnyRole("DISPATCHER", "FLEET_MANAGER", "FINANCIAL_ANALYST")

                        // ===== DRIVER RBAC =====
                        // Write: SAFETY_OFFICER only
                        .requestMatchers(HttpMethod.POST, "/api/drivers/**").hasRole("SAFETY_OFFICER")
                        .requestMatchers(HttpMethod.PUT, "/api/drivers/**").hasRole("SAFETY_OFFICER")
                        .requestMatchers(HttpMethod.PATCH, "/api/drivers/**").hasRole("SAFETY_OFFICER")
                        .requestMatchers(HttpMethod.DELETE, "/api/drivers/**").hasRole("SAFETY_OFFICER")
                        // Read: SAFETY_OFFICER, FLEET_MANAGER, DISPATCHER
                        .requestMatchers(HttpMethod.GET, "/api/drivers/**").hasAnyRole("SAFETY_OFFICER", "FLEET_MANAGER", "DISPATCHER")

                        // ===== MAINTENANCE RBAC =====
                        // Write: FLEET_MANAGER only
                        .requestMatchers(HttpMethod.POST, "/api/maintenance/**").hasRole("FLEET_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/maintenance/**").hasRole("FLEET_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/maintenance/**").hasRole("FLEET_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/maintenance/**").hasRole("FLEET_MANAGER")
                        // Read: FLEET_MANAGER, SAFETY_OFFICER, FINANCIAL_ANALYST
                        .requestMatchers(HttpMethod.GET, "/api/maintenance/**").hasAnyRole("FLEET_MANAGER", "SAFETY_OFFICER", "FINANCIAL_ANALYST")

                        // ===== EXPENSE & FUEL RBAC =====
                        // Write: FINANCIAL_ANALYST only
                        .requestMatchers(HttpMethod.POST, "/api/expenses/**").hasRole("FINANCIAL_ANALYST")
                        .requestMatchers(HttpMethod.PUT, "/api/expenses/**").hasRole("FINANCIAL_ANALYST")
                        .requestMatchers(HttpMethod.PATCH, "/api/expenses/**").hasRole("FINANCIAL_ANALYST")
                        .requestMatchers(HttpMethod.DELETE, "/api/expenses/**").hasRole("FINANCIAL_ANALYST")
                        .requestMatchers(HttpMethod.POST, "/api/fuel-logs/**").hasRole("FINANCIAL_ANALYST")
                        .requestMatchers(HttpMethod.DELETE, "/api/fuel-logs/**").hasRole("FINANCIAL_ANALYST")
                        // Read: FINANCIAL_ANALYST, FLEET_MANAGER
                        .requestMatchers(HttpMethod.GET, "/api/expenses/**").hasAnyRole("FINANCIAL_ANALYST", "FLEET_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/fuel-logs/**").hasAnyRole("FINANCIAL_ANALYST", "FLEET_MANAGER")

                        // ===== ANALYTICS RBAC =====
                        // Write (generate summaries): FINANCIAL_ANALYST only
                        .requestMatchers(HttpMethod.POST, "/api/analytics/**").hasRole("FINANCIAL_ANALYST")
                        // Read: FINANCIAL_ANALYST, FLEET_MANAGER
                        .requestMatchers(HttpMethod.GET, "/api/analytics/**").hasAnyRole("FINANCIAL_ANALYST", "FLEET_MANAGER")

                        // ===== DASHBOARD (all authenticated roles) =====
                        .requestMatchers("/api/dashboard/**").authenticated()

                        // ===== USER PROFILE (all authenticated) =====
                        .requestMatchers("/api/users/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                );

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}