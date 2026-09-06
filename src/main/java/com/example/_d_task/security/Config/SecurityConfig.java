package com.example._d_task.security.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private TokenFilter tokenFilter;

    public SecurityConfig(TokenFilter tokenFilter){
        this.tokenFilter = tokenFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http){
        http.authorizeHttpRequests(auth->auth
                .requestMatchers("/auth/***").permitAll()
                        .requestMatchers("/Users/***").hasRole("USER")
                        .requestMatchers("/project/***").hasRole("USER")
                        .requestMatchers("/task/***").hasRole("USER")
                        .requestMatchers("/files/***").hasRole("USER")
                        .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable).addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
