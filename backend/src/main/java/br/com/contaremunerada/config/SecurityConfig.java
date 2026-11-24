
package br.com.contaremunerada.config;

import br.com.contaremunerada.auth.security.JwtAuthenticationEntryPoint;
import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
  ) throws Exception {

    http
        .csrf(AbstractHttpConfigurer::disable)

        .sessionManagement(session ->
            session.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
            )
        )

        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)

        .authorizeHttpRequests(auth -> auth
            .dispatcherTypeMatchers(
                DispatcherType.ERROR
            ).permitAll()

            .requestMatchers(
                HttpMethod.POST, "/contas"
            ).permitAll()

            .requestMatchers(
                HttpMethod.POST, "/auth/login"
            ).permitAll()

            .requestMatchers(
                HttpMethod.GET, "/actuator/health"
            ).permitAll()

            .requestMatchers(
                "/taxa", "/fechamentos", "/fechamentos/**"
            ).permitAll()

            .anyRequest().authenticated()
        )

        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        )

        .oauth2ResourceServer(oauth2 -> oauth2
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .jwt(Customizer.withDefaults())
        );

    return http.build();
  }
}
