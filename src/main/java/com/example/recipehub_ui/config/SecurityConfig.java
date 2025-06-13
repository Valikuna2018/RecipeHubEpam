package com.example.recipehub_ui.config;

import com.example.recipehub_ui.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository users) {
        return username -> users.findByUsername(username)
                .map(u -> User.builder()
                        .username(u.getUsername())
                        .password(u.getPassword())
                        .roles("USER")
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException("No user " + username));
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            UserDetailsService uds,
            PasswordEncoder encoder
    ) throws Exception {
        AuthenticationManagerBuilder auth =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.userDetailsService(uds)
                .passwordEncoder(encoder);
        return auth.build();
    }


    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new LogoutSuccessHandler() {
            @Override
            public void onLogoutSuccess(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    Authentication authentication
            ) throws java.io.IOException {
                if (authentication != null) {
                    String user = authentication.getName();
                    log.info("User '{}' logged out successfully", user);
                }
                response.sendRedirect(request.getContextPath() + "/");
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/search",
                                "/css/**","/js/**","/images/**",
                                "/register","/login","/error"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/recipes/**").permitAll()
                        .requestMatchers("/recipes/upload").authenticated()
                        .requestMatchers(HttpMethod.POST, "/recipes/upload").authenticated()
                        .requestMatchers(HttpMethod.POST, "/recipes/*/rate").authenticated()
                        .requestMatchers("/my-recipes").authenticated()
                        .requestMatchers(HttpMethod.POST, "/recipes/*/delete").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            String username = authentication.getName();
                            log.info("User '{}' logged in successfully", username);
                            response.sendRedirect(request.getContextPath() + "/");
                        })
                        .failureHandler((request, response, exception) -> {
                            String username = request.getParameter("username");
                            log.warn("Login failed for '{}': {}", username, exception.getMessage());
                            response.sendRedirect(request.getContextPath() + "/login?error");
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
