package com.studyIn.infra.config;

import com.studyIn.domain.account.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;


/**
 * @EnableWebSecurity => "Spring Boot"를 사용하는 경우
 * "SecurityAutoConfiguration"에서 "import"되는 "WebSecurityEnablerConfiguration"에 의해 자동 세팅된다.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginService loginService;
    private final DataSource dataSource;

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
                .rememberMe(remember -> remember
                        .userDetailsService(loginService)
                        .tokenRepository(tokenRepository()))
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false))
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(loginService);
        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * "rememberMe" cookies 토큰 생성
     */
    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    /**
     * .../node_modules/** => security filter 무시
     */
    @Bean
    public SecurityFilterChain node_modules(HttpSecurity http) throws Exception {
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
