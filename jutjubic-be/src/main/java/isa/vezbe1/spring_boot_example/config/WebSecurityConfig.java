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

        // FIXED: Method-specific rules for videos
        http.authorizeHttpRequests(auth -> auth
                // Auth endpoints
                .requestMatchers("/api/auth/**").permitAll()

                // Videos - ONLY allow GET requests without authentication (3.1 requirement)
                .requestMatchers(HttpMethod.GET, "/api/videos").permitAll()           // Get all videos
                .requestMatchers(HttpMethod.GET, "/api/videos/{id}").permitAll()      // Get single video
                .requestMatchers(HttpMethod.POST, "/api/videos/{id}/view").permitAll() // Increment view count
                .requestMatchers(HttpMethod.GET, "/api/videos/{id}/comments").permitAll() // Get comments

                // All other /api/videos/** requests REQUIRE authentication (including POST /api/videos/upload)
                .requestMatchers("/api/videos/**").authenticated()

                // Comments - allow unauthenticated viewing, but commenting requires auth
                .requestMatchers(HttpMethod.GET, "/api/comments/**").permitAll()
                .requestMatchers("/api/comments/**").authenticated()

                // Users - allow viewing profiles
                .requestMatchers(HttpMethod.GET, "/api/users/{id}").permitAll()

                // H2 console (if using)
                .requestMatchers("/h2-console/**").permitAll()

                // All other requests require authentication
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
                // Authentication endpoints
                .requestMatchers(HttpMethod.POST, "/api/auth/login")          // 3.2 - Login endpoint
                .requestMatchers(HttpMethod.POST, "/api/auth/register")       // 3.2 - Registration endpoint
                .requestMatchers(HttpMethod.GET, "/api/auth/activate")        // 3.2 - Email activation endpoint

                // Public video viewing endpoints (3.1)
                .requestMatchers(HttpMethod.GET, "/api/videos")               // 3.1 - Get all videos
                .requestMatchers(HttpMethod.GET, "/api/videos/{id}")          // 3.1 - Get single video
                .requestMatchers(HttpMethod.GET, "/api/videos/{id}/comments") // 3.1 - Get comments for video
                .requestMatchers(HttpMethod.GET, "/api/users/{id}")           // 3.1 - Get user profile

                // Static resources (patterns cannot have content after **)
                .requestMatchers(HttpMethod.GET, "/", "/webjars/**", "/*.html", "/favicon.ico",
                        "/static/**", "/resources/**", "/public/**");
    }
}