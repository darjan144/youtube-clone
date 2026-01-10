package isa.vezbe1.spring_boot_example.config;

import isa.vezbe1.spring_boot_example.auth.RestAuthenticationEntryPoint;
import isa.vezbe1.spring_boot_example.auth.TokenAuthenticationFilter;
import isa.vezbe1.spring_boot_example.service.CustomUserDetailsService;
import isa.vezbe1.spring_boot_example.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    private TokenUtils tokenUtils;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint(restAuthenticationEntryPoint)
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()              // Login, registration, activation
                .requestMatchers("/api/videos/**").permitAll()            // 3.1 - View videos (unauthenticated)
                .requestMatchers("/api/comments/**").permitAll()          // 3.1 - View comments (unauthenticated)
                .requestMatchers("/api/users/{id}").permitAll()          // 3.1 - View user profile (unauthenticated)
                .requestMatchers("/h2-console/**").permitAll()           // H2 console if using H2 database

                .anyRequest().authenticated()
        );

        http.cors(cors -> cors.configure(http));

        http.addFilterBefore(
                new TokenAuthenticationFilter(tokenUtils, userDetailsService),
                BasicAuthenticationFilter.class
        );

        http.csrf(csrf -> csrf.disable());

        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(HttpMethod.POST, "/api/auth/login")          // 3.2 - Login endpoint
                .requestMatchers(HttpMethod.POST, "/api/auth/register")       // 3.2 - Registration endpoint
                .requestMatchers(HttpMethod.GET, "/api/auth/activate")        // 3.2 - Email activation endpoint
                .requestMatchers(HttpMethod.GET, "/api/videos")               // 3.1 - Get all videos
                .requestMatchers(HttpMethod.GET, "/api/videos/{id}")          // 3.1 - Get single video
                .requestMatchers(HttpMethod.GET, "/api/videos/{id}/comments") // 3.1 - Get comments for video
                .requestMatchers(HttpMethod.GET, "/api/users/{id}")           // 3.1 - Get user profile

                .requestMatchers(HttpMethod.GET, "/", "/webjars/**", "/*.html", "/favicon.ico",
                        "/**/*.html", "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg");
    }
}