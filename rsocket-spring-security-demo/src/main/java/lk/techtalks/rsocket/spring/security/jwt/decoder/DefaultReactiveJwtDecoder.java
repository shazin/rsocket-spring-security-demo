package lk.techtalks.rsocket.spring.security.jwt.decoder;

import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;

public class DefaultReactiveJwtDecoder implements ReactiveJwtDecoder {

    private SignatureVerifier signatureVerifier;
    private JsonParser objectMapper = new JacksonJsonParser();

    public DefaultReactiveJwtDecoder(String key) {
        this.signatureVerifier = new MacSigner(key);
    }

    @Override
    public Mono<Jwt> decode(String token) throws JwtException {
        try {
            org.springframework.security.jwt.Jwt jwt = JwtHelper.decodeAndVerify(token, this.signatureVerifier);
            String content = jwt.getClaims();
            Map<String, Object> map = objectMapper.parseMap(content);
            if (map.containsKey("exp") && map.get("exp") instanceof Integer) {
                Integer intValue = (Integer)map.get("exp");
                map.put("exp", new Long((long)intValue));
            }

            Map<String, Object> headers = Collections.singletonMap("Content-Type", "application/json");

            return Mono.just(new org.springframework.security.oauth2.jwt.Jwt(token, Instant.now(), Instant.now().plus(10, ChronoUnit.DAYS), headers, map));
        } catch (Exception var6) {
            throw new JwtException("Cannot convert access token to JSON", var6);
        }
    }

    public void setSignatureVerifier(SignatureVerifier signatureVerifier) {
        this.signatureVerifier = signatureVerifier;
    }
}
