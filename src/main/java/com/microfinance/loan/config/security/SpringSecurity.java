package com.microfinance.loan.config.security;

import com.microfinance.loan.common.service.impl.UserDetailsServiceImpl;
import com.microfinance.loan.config.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
//@Profile("prod")
public class SpringSecurity {

    private final UserDetailsServiceImpl userService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SpringSecurity(UserDetailsServiceImpl userService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userService = userService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        return http.authorizeHttpRequests(request -> request
//                        .requestMatchers(HttpMethod.POST, "/user", "/officer/", "/manager/").permitAll()
                        .requestMatchers("/api/public/**", "/api/health-check").permitAll()
                        .requestMatchers("/api/officer/**", "/api/user/**", "/api/agent/**").authenticated()
                        .requestMatchers("/api/manager/**").hasRole("MANAGER")
                        .anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passEncoder(){
        return new BCryptPasswordEncoder();
    }

}
