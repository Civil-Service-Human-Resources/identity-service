package uk.gov.cshr.service.security;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class OAuthToken {
    @JsonProperty("access_token")
    private String accessToken;
}
