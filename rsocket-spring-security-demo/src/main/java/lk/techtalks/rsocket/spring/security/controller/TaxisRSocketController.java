package lk.techtalks.rsocket.spring.security.controller;

import lk.techtalks.rsocket.spring.security.dto.TaxisRequest;
import lk.techtalks.rsocket.spring.security.dto.TaxisResponse;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Controller
class TaxisRSocketController {

    @MessageMapping("taxis")
    public Mono<TaxisResponse> taxis(TaxisRequest taxisRequest) {
        if (!"nano".equalsIgnoreCase(taxisRequest.getType()) && !"mini".equalsIgnoreCase(taxisRequest.getType())) {
            return Mono.error(new IllegalArgumentException("Only Taxi types Nano and Mini supported"));
        }
        return Mono.just(new TaxisResponse(61.39493, 23.30044, "Driver 1", "A "+taxisRequest.getType()+" is on the way to go from "+taxisRequest.getFrom() + " to "+ taxisRequest.getTo() + " @ "+ Instant.now()));
    }

    @MessageMapping("taxis-stream")
    public Flux<TaxisResponse> taxisStream(TaxisRequest taxisRequest) {
        if (!"nano".equalsIgnoreCase(taxisRequest.getType()) && !"mini".equalsIgnoreCase(taxisRequest.getType())) {
            return Flux.error(new IllegalArgumentException("Only Taxi types Nano and Mini supported"));
        }
        return Flux
                .interval(Duration.ofSeconds(1))
                .map(i -> new TaxisResponse(Math.random() * 10 + 1, Math.random() * 20 + 1, "Driver "+ i, "A "+taxisRequest.getType()+" is on the way to go from "+taxisRequest.getFrom() + " to "+ taxisRequest.getTo() + " @ "+ Instant.now()));
    }

    @MessageExceptionHandler
    public Flux<TaxisResponse> error(IllegalArgumentException iae) {
        return Flux.just(new TaxisResponse().withMessage(iae.getMessage()));
    }
}
