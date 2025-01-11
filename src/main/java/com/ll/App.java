package com.ll;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ll.security.ApiKeyAuthFilter;
import com.ll.security.JwtService;
import com.ll.service.PersonalAccountService;

@SpringBootApplication
public class App {

    private final JwtService jwtService;
    private final PersonalAccountService personalAccountService;

    public App(JwtService jwtService, PersonalAccountService personalAccountService) {
        this.jwtService = jwtService;
        this.personalAccountService = personalAccountService;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public ApiKeyAuthFilter apiKeyAuthFilter() {
        return new ApiKeyAuthFilter(jwtService, personalAccountService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiKeyAuthFilter apiKeyAuthFilter)
            throws Exception {
        List<ApiKeyAuthFilter.ExcludedPath> excludedPaths = new ArrayList<>();

        // Only exact match for account creation
        excludedPaths.add(new ApiKeyAuthFilter.ExcludedPath("/api/accounts", "POST"));

        // Swagger UI & OpenAPI paths
        excludedPaths.add(new ApiKeyAuthFilter.ExcludedPath("/v3/api-docs/**", null));
        excludedPaths.add(new ApiKeyAuthFilter.ExcludedPath("/swagger-ui/**", null));
        excludedPaths.add(new ApiKeyAuthFilter.ExcludedPath("/swagger-ui.html", null));
        excludedPaths.add(new ApiKeyAuthFilter.ExcludedPath("/webjars/**", null));

        apiKeyAuthFilter.setExcludedPaths(excludedPaths);

        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/accounts").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
