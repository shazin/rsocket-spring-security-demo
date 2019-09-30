package lk.techtalks.rsocket.spring.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxisResponse {

    private Double latitude;
    private Double longitude;
    private String driverName;
    private String message;

    public TaxisResponse withMessage(String msg) {
        this.message = msg;
        return this;
    }

}
