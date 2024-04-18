package uk.gov.cshr.service.csrs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.DomainsResponse;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.dto.AgencyTokenDTO;
import uk.gov.cshr.service.csrs.model.GetOrganisationsResponse;
import uk.gov.cshr.service.security.IdentityClientTokenService;
import uk.gov.cshr.service.security.OAuthToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class CsrsServiceClient {
    private final RestTemplate restTemplate;
    private final String domainsUrl;
    private final IdentityClientTokenService identityClientTokenService;
    private final String civilServantUrl;
    private final String agencyTokensUrl;
    private final Integer getOrganisationsMaxPageSize;
    private final String organsationsUrl;

    public CsrsServiceClient(@Autowired RestTemplate restTemplate,
                             @Value("${registry.domainsUrl}") String domainsUrl,
                             IdentityClientTokenService identityClientTokenService,
                             @Value("${registry.civilServantUrl}") String civilServantUrl,
                             @Value("${registry.agencyTokensUrl}") String agencyTokensUrl,
                             @Value("${registry.getOrganisationsMaxPageSize}") Integer getOrganisationsMaxPageSize,
                             @Value("${registry.organisationalUnitsFlatUrl}") String organsationsUrl) {
        this.restTemplate = restTemplate;
        this.domainsUrl = domainsUrl;
        this.identityClientTokenService = identityClientTokenService;
        this.civilServantUrl = civilServantUrl;
        this.agencyTokensUrl = agencyTokensUrl;
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

    private <T> T exchangeWithClientToken(String url, HttpMethod method, Class<T> responseClass) {
        HttpEntity<HttpHeaders> request = buildCsrsClientTokenRequest();
        try {
            log.info(String.format("Making request to CSRS: %s", url));
            ResponseEntity<T> response = restTemplate.exchange(url, method, request, responseClass);
            return response.getBody();
        } catch (Exception e) {
            log.error(String.format("Error sending request to CSRS API: %s", e));
            throw e;
        }
    }

    public List<String> getAllowlist() {
        log.info("Fetching allowlist from CSRS API");
        DomainsResponse body = exchangeWithClientToken(domainsUrl, HttpMethod.GET, DomainsResponse.class);
        if (body == null) {
            throw new RuntimeException("Allowlist returned null");
        }
        return body.getDomains().stream().map(d -> d.getDomain().toLowerCase()).collect(Collectors.toList());
    }

    private GetOrganisationsResponse getOrganisations(Integer size, Integer page) {
        String url = organsationsUrl + String.format("?size=%s&page=%s&formatName=true", size, page);
        return exchangeWithClientToken(url, HttpMethod.GET, GetOrganisationsResponse.class);
    }

    public boolean isDomainInAnAgencyToken(String domain) {
        try {
            String url = agencyTokensUrl + String.format("?domain=%s", domain);
            return exchangeWithClientToken(url, HttpMethod.GET, Boolean.class);
        } catch (HttpClientErrorException e) {
            return false;
        }
    }

    public boolean isDomainInAnAgencyTokenWithOrg(String domain, String orgCode) {
        try {
            String url = agencyTokensUrl + String.format("?domain=%s&code=%s", domain, orgCode);
            return exchangeWithClientToken(url, HttpMethod.GET, Boolean.class);
        } catch (HttpClientErrorException e) {
            return false;
        }
    }

    private Optional<AgencyTokenDTO> getAgencyToken(String url) {
        try {
            return Optional.of(exchangeWithClientToken(url, HttpMethod.GET, AgencyTokenDTO.class));
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }

    public Optional<AgencyTokenDTO> getAgencyTokenWithUid(String uid) {
        return getAgencyToken(agencyTokensUrl + String.format("?uid=%s", uid));
    }

    public Optional<AgencyTokenDTO> getAgencyToken(String domain, String token, String organisation) {
        return getAgencyToken(agencyTokensUrl + String.format("?domain=%s&token=%s&code=%s", domain, token, organisation));
    }

    public List<OrganisationalUnitDto> getAllOrganisations() {
        log.info("Fetching all organisations from CSRS API");
        List<OrganisationalUnitDto> organisationalUnits = new ArrayList<>();
        GetOrganisationsResponse initialResponse = getOrganisations(1, 0);
        if (initialResponse.getTotalElements() >= 1) {
            List<CompletableFuture<List<OrganisationalUnitDto>>> futures = IntStream.range(0, (int) Math.ceil((double) initialResponse.getTotalElements() / getOrganisationsMaxPageSize))
                    .boxed()
                    .map(i -> CompletableFuture.supplyAsync(() -> getOrganisations(getOrganisationsMaxPageSize, i).getContent())).collect(Collectors.toList());

            organisationalUnits = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(i -> futures.stream().flatMap(listCompletableFuture -> listCompletableFuture.join().stream()).collect(Collectors.toList())).join();

        }
        log.info(String.format("%s", organisationalUnits.size()));
        return organisationalUnits;
    }

    public void removeOrganisationalUnitFromCivilServant(String uid) {
        log.info(String.format("Removing organisation from user %s", uid));
        String url = String.format("%s/resource/%s/remove_organisation", civilServantUrl, uid);
        exchangeWithClientToken(url, HttpMethod.POST, Void.class);
    }
}
