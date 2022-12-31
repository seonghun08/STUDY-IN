package com.studyIn.infra.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.web.SecurityFilterChain;


/**
 * @EnableWebSecurity => "Spring Boot"를 사용하는 경우
 * "SecurityAutoConfiguration"에서 "import"되는 "WebSecurityEnablerConfiguration"에 의해 자동으로 세팅된다.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeRequests(authorize -> authorize
                        .mvcMatchers("/", "/login", "/sign-up", "/check-email-token", "/find-password", "/login-by-email").permitAll()
                        .mvcMatchers(HttpMethod.GET, "profile/*").permitAll())
                .formLogin(login -> login
                        .loginPage("/login").permitAll()
                        .defaultSuccessUrl("/"))
                .logout(logout -> logout
                        .logoutSuccessUrl("/"))
                .build();
    }

    @Bean
    public SecurityFilterChain resources(HttpSecurity http) throws Exception {
        return http
                .requestMatchers(matchers -> matchers
                        .mvcMatchers("/node_modules/**"))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll())
                .requestCache().disable()
                .securityContext().disable()
                .sessionManagement().disable()
                .build();
    }
}
