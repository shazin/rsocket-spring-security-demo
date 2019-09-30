package lk.techtalks.rsocket.spring.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaxisRequest {
    private String type;
    private String from;
    private String to;


}
