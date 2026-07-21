package com.cts.logichain360.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//Extracting from SecurityConfig to break a circular bean dependency
 //The cycle was:
 //SecurityConfig (@Bean PasswordEncoder) -> used by UserServiceImpl
 //SecurityConfig (constructor) -> UserInfoConfigManager
 //UserInfoConfigManager (constructor)-> UserRepository
 //By moving the PasswordEncoder bean here, SecurityConfig no longer participates
 // in the creation graph of the services that need encoding, and Spring can wire
 // everything without 'spring.main.allow-circular-references=true'

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}