package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.DomainsResponse;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.service.security.IdentityClientTokenService;
import uk.gov.cshr.service.security.OAuthToken;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CsrsService {
    private RestTemplate restTemplate;
    private String domainsUrl;
    private IdentityClientTokenService identityClientTokenService;
    private String agencyTokensFormat;
    private String agencyTokensByDomainFormat;
    private String agencyTokensByDomainAndOrganisationFormat;
    private String organisationalUnitsFlatUrl;

    public CsrsService(@Autowired RestTemplate restTemplate,
                       @Value("${registry.domainsUrl}") String domainsUrl,
                       IdentityClientTokenService identityClientTokenService,
                       @Value("${registry.agencyTokensFormat}") String agencyTokensFormat,
                       @Value("${registry.agencyTokensByDomainFormat}") String agencyTokensByDomainFormat,
                       @Value("${registry.agencyTokensByDomainAndOrganisationFormat}") String agencyTokensByDomainAndOrganisationFormat,
                       @Value("${registry.organisationalUnitsFlatUrl}") String organisationalUnitsFlatUrl) {
        this.restTemplate = restTemplate;
        this.domainsUrl = domainsUrl;
        this.identityClientTokenService = identityClientTokenService;
        this.agencyTokensFormat = agencyTokensFormat;
        this.agencyTokensByDomainFormat = agencyTokensByDomainFormat;
        this.agencyTokensByDomainAndOrganisationFormat = agencyTokensByDomainAndOrganisationFormat;
        this.organisationalUnitsFlatUrl = organisationalUnitsFlatUrl;
    }

    @Cacheable("allowlist")
    public List<String> getAllowlist() {
        OAuthToken token = identityClientTokenService.getClientToken();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token.getAccessToken());
        HttpEntity<HttpHeaders> request = new HttpEntity<>(headers);
        ResponseEntity<DomainsResponse> response = restTemplate.exchange(domainsUrl, HttpMethod.GET, request, DomainsResponse.class);
        DomainsResponse body = response.getBody();
        if (body == null) {
            throw new RuntimeException("Allowlist returned null");
        }
        return response.getBody().getDomains().stream().map(d -> d.getDomain().toLowerCase()).collect(Collectors.toList());
    }

    @CacheEvict(value = "allowlist", allEntries = true)
    @Scheduled(fixedRateString = "${registry.cache.allowlistTTL}")
    public void emptyHotelsCache() {
        log.info("emptying Allowlist cache");
    }

    public Boolean isDomainInAgency(String domain) {
        try {
            return restTemplate.getForObject(String.format(agencyTokensByDomainFormat, domain), Boolean.class);
        } catch (HttpClientErrorException e) {
            log.error("An error occurred checking if domain in agency", e);
            return false;
        }
    }

    public AgencyToken[] getAgencyTokensForDomain(String domain) {
        try {
            return restTemplate.getForObject(String.format(agencyTokensByDomainFormat, domain), AgencyToken[].class);
        } catch (HttpClientErrorException e) {
            return new AgencyToken[]{};
        }
    }

    public Optional<AgencyToken> getAgencyTokenForDomainTokenOrganisation(String domain, String token, String organisation) {
        try {
            return Optional.of(restTemplate.getForObject(String.format(agencyTokensFormat, domain, token, organisation), AgencyToken.class));
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }

    public Optional<AgencyToken> getAgencyTokenForDomainAndOrganisation(String domain, String organisation) {
        try {
            return Optional.of(restTemplate.getForObject(String.format(agencyTokensByDomainAndOrganisationFormat, domain, organisation), AgencyToken.class));
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }

    public OrganisationalUnitDto[] getOrganisationalUnitsFormatted() {
        OrganisationalUnitDto[] organisationalUnitDtos;
        try {
            organisationalUnitDtos = restTemplate.getForObject(organisationalUnitsFlatUrl, OrganisationalUnitDto[].class);
        } catch (HttpClientErrorException e) {
            organisationalUnitDtos = new OrganisationalUnitDto[0];
        }
        return organisationalUnitDtos;
    }
}
