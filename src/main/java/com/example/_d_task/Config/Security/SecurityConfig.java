package com.example._d_task.Config.Security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http){
        http.authorizeHttpRequests(auth->auth
                .requestMatchers("/auth/register").permitAll().anyRequest()
                .authenticated()
        )
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
