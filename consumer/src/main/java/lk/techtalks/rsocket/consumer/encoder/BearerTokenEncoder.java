package lk.techtalks.rsocket.consumer.encoder;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.security.rsocket.metadata.BearerTokenMetadata;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BearerTokenEncoder extends AbstractEncoder<BearerTokenMetadata> {

    public BearerTokenEncoder() {
        super(BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE);
    }

    @Override
    public Flux<DataBuffer> encode(Publisher<? extends BearerTokenMetadata> publisher, DataBufferFactory dataBufferFactory, ResolvableType resolvableType, MimeType mimeType, Map<String, Object> hints) {
        return Flux.from(publisher).map((credentials) -> {
            return this.encodeValue(credentials, dataBufferFactory, resolvableType, mimeType, hints);
        });
    }

    @Override
    public DataBuffer encodeValue(BearerTokenMetadata credentials, DataBufferFactory bufferFactory, ResolvableType valueType, MimeType mimeType, Map<String, Object> hints) {
        String token = credentials.getToken();
        byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
        DataBuffer metadata = bufferFactory.allocateBuffer();
        boolean release = true;

        DataBuffer dataBuffer;
        try {
            metadata.write(tokenBytes);
            release = false;
            dataBuffer = metadata;
        } finally {
            if (release) {
                DataBufferUtils.release(metadata);
            }
        }

        return dataBuffer;
    }
}
