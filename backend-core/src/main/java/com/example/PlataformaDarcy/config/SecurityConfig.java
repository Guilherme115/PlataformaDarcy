package com.example.PlataformaDarcy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                // 1. RECURSOS ESTÁTICOS (Essencial para o CSS, JS e Imagens carregarem)
                                                // Adicionei "/favicon.ico" para evitar o erro 302 no navegador
                                                .requestMatchers(
                                                                "/css/**",
                                                                "/js/**",
                                                                "/img/**",
                                                                "/images/**",
                                                                "/media/**",
                                                                "/favicon.ico")
                                                .permitAll()

                                                // 2. PÁGINAS PÚBLICAS (Acesso sem login)
                                                // Adicionei "/error" para que falhas não redirecionem para o login
                                                .requestMatchers(
                                                                "/",
                                                                "/index",
                                                                "/login",
                                                                "/register",
                                                                "/forgot-password",
                                                                "/reset-password",
                                                                "/error")
                                                .permitAll()

                                                // 3. ÁREA RESTRITA (Admin)
                                                .requestMatchers("/cms/**", "/admin/**").hasRole("ADMIN")

                                                // 4. RESTANTE (Exige Login)
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login") // Aponta para o nosso controller customizado
                                                .defaultSuccessUrl("/", true) // Força ir para a home após logar
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .successHandler(googleOAuth2SuccessHandler))
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/") // Volta para a landing page ao sair
                                                .permitAll())
                                .csrf(csrf -> csrf.disable()); // Desabilitado para facilitar desenvolvimento de forms

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}