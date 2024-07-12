package uk.gov.cshr.service.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;

@Data
public class OAuthToken implements Serializable {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    private Instant expiryDateTime;

    @JsonIgnore
    public boolean isExpired() {
        return Instant.now().isAfter(expiryDateTime);
    }

    @JsonIgnore
    public void setExpiryDateTimeFromExpiresIn() {
        expiryDateTime = Instant.now().plus(expiresIn, ChronoUnit.SECONDS);
    }
}
