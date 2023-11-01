package uk.gov.cshr.service.security;

import org.omg.CORBA.ARG_OUT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IdentityClientTokenService {
    private final String baseUrl;
    private final String clientId;
    private final String clientSecret;

    public IdentityClientTokenService(@Value("${identity.identityBaseUrl}") String baseUrl,
                                      @Value("${identity.clientId}") String clientId,
                                      @Value("${identity.clientSecret}") String clientSecret) {
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public OAuthToken getClientToken() {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .basicAuthorization(clientId, clientSecret).build();
        return restTemplate.postForObject(String.format("%s/oauth/token?grant_type=client_credentials", baseUrl), null, OAuthToken.class);
    }
}
