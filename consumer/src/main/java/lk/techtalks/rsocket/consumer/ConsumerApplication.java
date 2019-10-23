package lk.techtalks.rsocket.consumer;

import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import lk.techtalks.rsocket.consumer.encoder.BearerTokenEncoder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.rsocket.metadata.BasicAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.BearerTokenMetadata;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.netty.tcp.TcpClient;

@SpringBootApplication
public class ConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
	}

	@Bean
	public RSocketRequester rSocketRequester(RSocketStrategies rSocketStrategies) {
		//UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("setup", "sha123");
        //BearerTokenMetadata credentials = new BearerTokenMetadata("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6InNldHVwIiwiaWF0IjoxNTE2MjM5MDIyLCJzY29wZSI6WyJTRVRVUCJdfQ.1RpzzbM6FskucjB71oXXG-t8FMBmaLYWv7ZJXojMyQA");
		BearerTokenMetadata credentials = new BearerTokenMetadata("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6InNldHVwIiwiaWF0IjoxNTE2MjM5MDIyLCJzY29wZSI6WyJTRVRVUCJdfQ.l2N5TT7hsN6KJLHwzYXxjS48-fqIqjNWHcJ13ll3ExU");
		return RSocketRequester.builder()
				.dataMimeType(MimeTypeUtils.APPLICATION_JSON)
				.rsocketStrategies(rSocketStrategies)
				.rsocketFactory(clientRSocketFactory -> {
					clientRSocketFactory.frameDecoder(PayloadDecoder.ZERO_COPY);
				})
				.setupMetadata(credentials, BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE)
				.connect(TcpClientTransport.create(TcpClient.create().port(7000)))
				.block();
	}

	@Bean
	public RSocketStrategies rsocketStrategies() {
		return RSocketStrategies.builder()
				.encoder(new BasicAuthenticationEncoder(), new Jackson2JsonEncoder(), new BearerTokenEncoder())
				.decoder(new Jackson2JsonDecoder())
				.build();
	}

}

@RestController
class TaxisRestController {

	private final RSocketRequester rSocketRequester;

	TaxisRestController(RSocketRequester rSocketRequester) {
		this.rSocketRequester = rSocketRequester;
	}

	@GetMapping("/taxis/{type}/{from}/{to}")
	public Publisher<TaxisResponse> taxis(@PathVariable String type, @PathVariable String from, @PathVariable String to) {
		//UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("shazin", "sha123");
        //BearerTokenMetadata credentials = new BearerTokenMetadata("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6InNoYXppbiIsImlhdCI6MTUxNjIzOTAyMiwic2NvcGUiOlsiQURNSU4iXX0.fiozzAM38Pz9tc-0PdojgYpco49W6SSh9mtYUipiFpM");
		BearerTokenMetadata credentials = new BearerTokenMetadata("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6InNoYXppbiIsImlhdCI6MTUxNjIzOTAyMiwic2NvcGUiOlsiQURNSU4iXX0.lieYZKrPVtEoH2prh_H2ae4z8iBCMc9wz82CWRHtRUI");
		return rSocketRequester
				.route("taxis")
				.metadata(credentials, BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE)
				.data(new TaxisRequest(type, from, to))
				.retrieveMono(TaxisResponse.class);
	}

	@GetMapping(value = "/taxis/sse/{type}/{from}/{to}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Publisher<TaxisResponse> taxisStream(@PathVariable String type, @PathVariable String from, @PathVariable String to) {
		//UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("shazin", "sha123");
        //BearerTokenMetadata credentials = new BearerTokenMetadata("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6InNoYXppbiIsImlhdCI6MTUxNjIzOTAyMiwic2NvcGUiOlsiQURNSU4iXX0.fiozzAM38Pz9tc-0PdojgYpco49W6SSh9mtYUipiFpM");
		BearerTokenMetadata credentials = new BearerTokenMetadata("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6InNoYXppbiIsImlhdCI6MTUxNjIzOTAyMiwic2NvcGUiOlsiQURNSU4iXX0.lieYZKrPVtEoH2prh_H2ae4z8iBCMc9wz82CWRHtRUI");
		return rSocketRequester
				.route("taxis-stream")
				.metadata(credentials, BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE)
				.data(new TaxisRequest(type, from, to))
				.retrieveFlux(TaxisResponse.class);
	}


}

@Data
@AllArgsConstructor
@NoArgsConstructor
class TaxisRequest {
	private String type;
	private String from;
	private String to;


}

@Data
@NoArgsConstructor
@AllArgsConstructor
class TaxisResponse {

	private Double latitude;
	private Double longitude;
	private String driverName;
	private String message;

	public TaxisResponse withMessage(String msg) {
		this.message = msg;
		return this;
	}

}
