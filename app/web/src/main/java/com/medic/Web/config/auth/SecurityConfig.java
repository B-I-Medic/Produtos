package com.medic.Web.config.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authorization.AllAuthoritiesReactiveAuthorizationManager;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig implements WebFluxConfigurer {

    private final ServerAuthenticationEntryPoint authenticationEntryPoint;
    private final ServerAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(ServerAuthenticationEntryPoint authenticationEntryPoint,
                          ServerAccessDeniedHandler accessDeniedHandler) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
            ROLE_ADMIN > ROLE_MASTER
            ROLE_MASTER > ROLE_USER
        """);
    }

    @Bean
    public SecurityWebFilterChain filterChain(
            JwtAuthenticationConfig jwtConfig,
            ServerHttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            RoleHierarchy roleHierarchy) {

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(auth -> auth
                                .pathMatchers("/swagger-ui/**",
                                        "/api-docs",
                                        "/api-docs/**",
                                        "/v3/api-docs/**",
                                        "/v3/api-docs",
                                        "/actuator/health/readiness",
                                        "/auth/login",
                                        "/auth/forgot-password",
                                        "/auth/reset-password"
                                ).permitAll()
                                .pathMatchers("/auth/first-acess",
                                        "/centro-distribuicao/get",
                                        "/centro-distribuicao/*/empresa-municipio/get",
                                        "/empresa/get",
                                        "/empresa/municipio/get",
                                        "/municipio/get",
                                        "/forecast/get",
                                        "/forecast/agrupamentos-disponiveis/get",
                                        "/estoque/interno/get",
                                        "/estoque/segregado/get",
                                        "/vale-permanente/get",
                                        "/periodo/get",
                                        "/taxa/get",
                                        "/schedule/get"
                                ).access(hasRole("USER", roleHierarchy))
                                .pathMatchers("/centro-distribuicao/save",
                                        "/centro-distribuicao/update",
                                        "/centro-distribuicao/delete",
                                        "/empresa/save",
                                        "/empresa/update",
                                        "/empresa/delete",
                                        "/empresa/municipio/save",
                                        "/empresa/municipio/delete",
                                        "/estoque/interno/save",
                                        "/estoque/interno/update",
                                        "/estoque/interno/delete",
                                        "/estoque/segregado/save",
                                        "/estoque/segregado/update",
                                        "/estoque/segregado/delete",
                                        "/vale-permanente/save",
                                        "/vale-permanente/update",
                                        "/vale-permanente/delete",
                                        "/periodo/definir",
                                        "/taxa/definir",
                                        "/relatorio/forecast"
                                ).access(hasRole("MASTER", roleHierarchy))
                                .pathMatchers("/usuario/save",
                                        "/usuario/update",
                                        "/usuario/enable",
                                        "/usuario/disable",
                                        "/usuario/get/paginado",
                                        "/schedule/update",
                                        "/schedule/enable",
                                        "/schedule/disable"
                                ).access(hasRole("ADMIN", roleHierarchy))
                                .anyExchange().authenticated()
                )
                .addFilterAt(jwtConfig, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private ReactiveAuthorizationManager<AuthorizationContext> hasRole(String role, RoleHierarchy roleHierarchy) {

        var manager = AllAuthoritiesReactiveAuthorizationManager.<AuthorizationContext>hasAllRoles(role);

        manager.setRoleHierarchy(roleHierarchy);

        return manager;
    }

}
