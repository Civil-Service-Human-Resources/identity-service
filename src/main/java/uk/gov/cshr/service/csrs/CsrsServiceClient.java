package uk.gov.cshr.service.csrs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.service.csrs.model.GetOrganisationsResponse;
import uk.gov.cshr.service.security.IdentityClientTokenService;
import uk.gov.cshr.service.security.OAuthToken;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class CsrsServiceClient {
    private final RestTemplate restTemplate;
    private final IdentityClientTokenService identityClientTokenService;
    private final String civilServantUrl;
    private final Integer getOrganisationsMaxPageSize;
    private final String organsationsUrl;

    public CsrsServiceClient(@Autowired RestTemplate restTemplate,
                             IdentityClientTokenService identityClientTokenService,
                             @Value("${registry.civilServantUrl}") String civilServantUrl,
                             @Value("${registry.getOrganisationsMaxPageSize}") Integer getOrganisationsMaxPageSize,
                             @Value("${registry.organisationalUnitsFlatUrl}") String organsationsUrl) {
        this.restTemplate = restTemplate;
        this.identityClientTokenService = identityClientTokenService;
        this.civilServantUrl = civilServantUrl;
        this.getOrganisationsMaxPageSize = getOrganisationsMaxPageSize;
        this.organsationsUrl = organsationsUrl;
    }

    private HttpEntity<HttpHeaders> buildCsrsClientTokenRequest() {
        OAuthToken token = fetchClientToken();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token.getAccessToken());
        return new HttpEntity<>(headers);
    }

    private OAuthToken fetchClientToken() {
        OAuthToken token = identityClientTokenService.getClientToken();
        if (token.isExpired()) {
            identityClientTokenService.clearTokenCache();
            token = identityClientTokenService.getClientToken();
        }
        return token;
    }

    private GetOrganisationsResponse getOrganisations(Integer size, Integer page) {
        HttpEntity<HttpHeaders> request = buildCsrsClientTokenRequest();
        String url = organsationsUrl + String.format("?size=%s&page=%s&formatName=true", size, page);
        ResponseEntity<GetOrganisationsResponse> response = restTemplate.exchange(url, HttpMethod.GET, request, GetOrganisationsResponse.class);
        return response.getBody();
    }

    public List<OrganisationalUnitDto> getAllOrganisations() {
        log.info("Fetching all organisations from CSRS API");
        List<OrganisationalUnitDto> organisationalUnits = new ArrayList<>();
        GetOrganisationsResponse initialResponse = getOrganisations(1, 0);
        if (initialResponse.getTotalElements() >= 1) {
            organisationalUnits = IntStream.range(0, (int) Math.ceil((double) initialResponse.getTotalElements() / getOrganisationsMaxPageSize))
                    .boxed()
                    .flatMap(i -> getOrganisations(getOrganisationsMaxPageSize, i).getContent().stream()).collect(Collectors.toList());
        }
        return organisationalUnits;
    }

    public void removeOrganisationalUnitFromCivilServant(String uid) {
        log.info(String.format("Removing organisation from user %s", uid));
        HttpEntity<HttpHeaders> request = buildCsrsClientTokenRequest();
        String url = String.format("%s/resource/%s/remove_organisation", civilServantUrl, uid);
        restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
    }
}
