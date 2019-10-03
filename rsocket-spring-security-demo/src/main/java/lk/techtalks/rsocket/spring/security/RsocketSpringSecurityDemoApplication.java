package lk.techtalks.rsocket.spring.security;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.rsocket.frame.decoder.PayloadDecoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.rsocket.netty.NettyRSocketServerFactory;
import org.springframework.boot.rsocket.server.RSocketServer;
import org.springframework.boot.rsocket.server.RSocketServerBootstrap;
import org.springframework.boot.rsocket.server.RSocketServerFactory;
import org.springframework.boot.rsocket.server.ServerRSocketFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.rsocket.PayloadSocketAcceptorInterceptor;

import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.stream.Collectors;

@SpringBootApplication
public class RsocketSpringSecurityDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(RsocketSpringSecurityDemoApplication.class, args);
	}

	@Bean
	ReactorResourceFactory reactorResourceFactory() {
		return new ReactorResourceFactory();
	}

	@Bean
	RSocketServerFactory rSocketServerFactory(ReactorResourceFactory resourceFactory,
											  ObjectProvider<ServerRSocketFactoryCustomizer> customizers) throws Exception {
//		SslContext sslServer;
//		final SslProvider sslProvider = SslProvider.JDK;
//		SecureRandom random = new SecureRandom();
//		SelfSignedCertificate ssc = new SelfSignedCertificate("techtalks.lk", random, 1024);
//		sslServer =
//				SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
//						.sslProvider(sslProvider)
//						.build();
		NettyRSocketServerFactory factory = new NettyRSocketServerFactory();
		factory.setResourceFactory(resourceFactory);
		factory.setTransport(RSocketServer.TRANSPORT.TCP);
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		map.from(InetAddress.getByName("localhost")).to(factory::setAddress);
		map.from(7000).to(factory::setPort);
		factory.setServerCustomizers(customizers.orderedStream().collect(Collectors.toList()));
		return factory;
	}

	@Bean
	RSocketServerBootstrap rSocketServerBootstrap(RSocketServerFactory rSocketServerFactory,
												  RSocketMessageHandler rSocketMessageHandler) {
		return new RSocketServerBootstrap(rSocketServerFactory, rSocketMessageHandler.responder());
	}

	@Bean
	ServerRSocketFactoryCustomizer frameDecoderServerFactoryCustomizer(
			RSocketMessageHandler rSocketMessageHandler, PayloadSocketAcceptorInterceptor rsocketInterceptor) {
		return (serverRSocketFactory) -> {
			if (rSocketMessageHandler.getRSocketStrategies()
					.dataBufferFactory() instanceof NettyDataBufferFactory) {
				serverRSocketFactory.frameDecoder(PayloadDecoder.ZERO_COPY);
			}
			return serverRSocketFactory.addSocketAcceptorPlugin(rsocketInterceptor);
		};
	}


}
