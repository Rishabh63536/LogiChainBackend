package com.cts.logichain360.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.cts.logichain360.service.impl.UserInfoConfigManager;
import com.cts.util.AppConstants;
import lombok.AllArgsConstructor;
@EnableMethodSecurity
@AllArgsConstructor
@Configuration
public class SecurityConfig {
    private final JWTFilter jwtFilter;
    private final UserInfoConfigManager userInfoConfigManager;
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(request -> request
                        // Explicitly permit ALL incoming HTTP OPTIONS preflight requests
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(AppConstants.PUBLIC_URLS).permitAll()
                        .anyRequest().authenticated())
                //.cors(cors->cors.disable())
                .cors(cors -> {})
                .csrf(csrf->csrf.disable())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

