package uk.gov.cshr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties implements Serializable {
    private static final long serialVersionUID = -2029585388984458403L;

    private String serviceUrl;
    private String clientId;
    private String clientSecret;
    private String tokenUrl;
    private String checkTokenUrl;
}
