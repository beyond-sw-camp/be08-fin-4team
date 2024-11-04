package com.beyond.easycheck.common.security.config;

import com.beyond.easycheck.common.security.filter.JwtAuthenticationFilter;
import com.beyond.easycheck.common.security.provider.JwtAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class EasyCheckSecurityConfig {

    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    @Bean
    public SecurityFilterChain defaultConfig(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // jwt authentication filter
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // api endpoint
        http
                .authorizeHttpRequests(registry -> {

                    // swagger
                    registry.requestMatchers(
                                    // Swagger UI v3 (OpenAPI)
                                    "/v3/api-docs/**",
                                    "/swagger-ui/**"
                            )
                            .permitAll();

                    // 회원가입, 로그인
                    registry.requestMatchers(HttpMethod.POST,
                                    "/api/v1/users",
                                    "/api/v1/corp-users",
                                    "/api/v1/users/login",
                                    "/api/v1/admin/login"
                            )
                            .permitAll();

                    // 휴대폰 인증
                    registry.requestMatchers(HttpMethod.POST,
                            "/api/v1/sms/code",
                            "/api/v1/sms/verify"
                    ).permitAll();

                    // 이메일 찾기
                    registry.requestMatchers(HttpMethod.POST,
                            "/api/v1/users/find-email",
                            "/api/v1/users/find-password"
                    ).permitAll();

                    // 중복확인 비밀번호 변경, email
                    registry.requestMatchers(HttpMethod.PATCH,
                            "/api/v1/users/check-duplicate",
                            "/api/v1/verification-code",
                            "/api/v1/verify-code"
                    ).permitAll();



                    registry.requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll();

                    registry.requestMatchers("/api/v1/users/{id}/status")
                            .hasRole("ADMIN");

                    // 권한 생성은 SUPER_ADMIN만 가능
                    registry.requestMatchers("/api/v1/permissions/**")
                            .hasRole("SUPER_ADMIN");

                    // 관리자 관련 설정
                    registry.requestMatchers("/api/v1/admin/**")
                            .hasAnyAuthority("ROLE_[0-9]+_ADMIN");

                    registry.requestMatchers(HttpMethod.GET, "/api/v1/users/info")
                            .authenticated();

                    registry.anyRequest().authenticated();
                });


        return http.build();
    }

    // 비밀번호 암호화용 인코더 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtAuthenticationProvider);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080","http://localhost:8081", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}