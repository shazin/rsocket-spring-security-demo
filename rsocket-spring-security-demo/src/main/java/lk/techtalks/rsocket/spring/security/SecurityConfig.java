package lk.techtalks.rsocket.spring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.rsocket.PayloadSocketAcceptorInterceptor;
import org.springframework.security.rsocket.metadata.BasicAuthenticationDecoder;

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
        })
        .basicAuthentication(Customizer.withDefaults());

        return rsocket.build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails adminUser = User.withDefaultPasswordEncoder().username("shazin").password("sha123").roles("ADMIN").build();

        UserDetails setupUser = User.withDefaultPasswordEncoder().username("setup").password("sha123").roles("SETUP").build();

        return new MapReactiveUserDetailsService(adminUser, setupUser);
    }

}
