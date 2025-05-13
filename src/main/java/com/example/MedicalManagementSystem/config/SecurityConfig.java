package com.example.MedicalManagementSystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;                       // ← import this
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RecaptchaAuthenticationFilter recaptchaAuthenticationFilter
    ) throws Exception {
        http
                // 1) disable CSRF once (so your curl/ajax works without tokens)
                .csrf(csrf -> csrf.disable())

                // 2) configure URL authorization
                .authorizeHttpRequests(auth -> auth
                        // permit static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // permit your login & error pages
                        .requestMatchers("/login", "/process-login", "/error", "/change-language").permitAll()

                        // permit the WebSocket handshake
                        .requestMatchers("/ws/**").permitAll()

                        // *** KEY: allow anonymous POST to your OCR extract API ***
                        .requestMatchers(HttpMethod.POST, "/user/ocr/extract").permitAll()

                        // admin pages
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // everything else under /user/** still requires USER
                        .requestMatchers("/user/**").hasRole("USER")

                        // chat pages for both roles
                        .requestMatchers("/chat", "/chat/**").hasAnyRole("ADMIN", "USER")

                        // anything else needs login
                        .anyRequest().authenticated()
                )

                // 3) form login for your UI
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                )

                // 4) logout config
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // 5) custom filter & context repository
                .addFilterAt(recaptchaAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .securityContext(ctx -> ctx.securityContextRepository(securityContextRepository()))

                // 6) redirect to login page on auth failures
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                )
        ;

        return http.build();
    }
}
