package lk.techtalks.rsocket.spring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.rsocket.PayloadSocketAcceptorInterceptor;
import org.springframework.security.rsocket.metadata.BasicAuthenticationDecoder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

@Configuration
@EnableRSocketSecurity
public class SecurityConfig {

    @Bean
    public RSocketMessageHandler messageHandler() {
        RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.setRSocketStrategies(rsocketStrategies());
        return handler;
    }

    @Bean
    public RSocketStrategies rsocketStrategies() {
        return RSocketStrategies.builder()
                .decoder(new BasicAuthenticationDecoder(), new Jackson2JsonDecoder())
                .encoder(new Jackson2JsonEncoder())
                .build();
    }

    @Bean
    public PayloadSocketAcceptorInterceptor rsocketInterceptor(RSocketSecurity rsocket) {
        rsocket.authorizePayload(authorize -> {
            authorize
                    // must have ROLE_SETUP to make connection
                    .setup().hasRole("SETUP")
                    // must have ROLE_ADMIN for routes starting with "taxis."
                    .route("taxis*").hasRole("ADMIN")
                    // any other request must be authenticated for
                    .anyRequest().authenticated();
        }).jwt(jwtSpec -> {
            jwtSpec.authenticationManager(jwtReactiveAuthenticationManager());
        });
        //.basicAuthentication(Customizer.withDefaults());

        return rsocket.build();
    }

    @Bean
    public JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager() {
        JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager = new JwtReactiveAuthenticationManager(new ReactiveJwtDecoder() {
            @Override
            public Mono<Jwt> decode(String token) throws JwtException {
                ObjectMapper mapper = new ObjectMapper();
                String[] parts = token.split("\\.");
                String base64Decoded = new String(Base64.getDecoder().decode(parts[1]));
                Map<String, Object> headers = Collections.singletonMap("Content-Type", "text/plain");
                Map<String, Object> claims = null;//Collections.singletonMap("role", "ROLE_SETUP");
                try {
                    claims = mapper.readValue(base64Decoded, Map.class);
                } catch (IOException e) {
                    throw new JwtException(e.getMessage());
                }
                return Mono.just(new Jwt(token, Instant.now(), Instant.now().plus(10, ChronoUnit.DAYS), headers, claims));
            }
        });

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        jwtReactiveAuthenticationManager.setJwtAuthenticationConverter( new ReactiveJwtAuthenticationConverterAdapter(authenticationConverter));
        return jwtReactiveAuthenticationManager;
    }

//    @Bean
//    public MapReactiveUserDetailsService userDetailsService() {
//        UserDetails adminUser = User.withDefaultPasswordEncoder()
//                .username("shazin")
//                .password("sha123")
//                .roles("ADMIN").build();
//
//        UserDetails setupUser = User.withDefaultPasswordEncoder()
//                .username("setup")
//                .password("sha123")
//                .roles("SETUP").build();
//
//        return new MapReactiveUserDetailsService(adminUser, setupUser);
//    }

}
