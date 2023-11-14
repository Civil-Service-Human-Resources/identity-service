package uk.gov.cshr.service.security;

import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.ARG_OUT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
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

    @CacheEvict("csrs-token")
    public void clearTokenCache() {}

    @Cacheable("csrs-token")
    public OAuthToken getClientToken() {
        log.info("Making request to get client token");
        String url = String.format("%s/oauth/token?grant_type=client_credentials", baseUrl);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .basicAuthorization(clientId, clientSecret).build();
        ResponseEntity<OAuthToken> resp = restTemplate.exchange(url, HttpMethod.POST, null, OAuthToken.class);
        if (resp.getStatusCode().isError()) {
            throw new RuntimeException(String.format("Request failed with status %s, body: %s", resp.getStatusCode(), resp.getBody()));
        } else if (resp.getBody() == null) {
            throw new RuntimeException("Client token is null");
        }
        OAuthToken token = resp.getBody();
        token.setExpiryDateTimeFromExpiresIn();
        log.info(String.format("Successfully fetched client token. Expiry is %s", token.getExpiryDateTime()));
        return token;
    }
}
