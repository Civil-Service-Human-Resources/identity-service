package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.domain.WhitelistedDomainDto;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CsrsService {
    private RestTemplate restTemplate;
    private String agencyTokensFormat;
    private String agencyTokensByDomainFormat;
    private String agencyTokensByDomainAndOrganisationFormat;
    private String organisationalUnitsFlatUrl;
    private String whitelistedDomainsUrl;

    public CsrsService(@Autowired RestTemplate restTemplate,
                       @Value("${registry.agencyTokensFormat}") String agencyTokensFormat,
                       @Value("${registry.agencyTokensByDomainFormat}") String agencyTokensByDomainFormat,
                       @Value("${registry.agencyTokensByDomainAndOrganisationFormat}") String agencyTokensByDomainAndOrganisationFormat,
                       @Value("${registry.organisationalUnitsFlatUrl}") String organisationalUnitsFlatUrl,
                       @Value("${registry.whitelistedDomainsUrl}") String whitelistedDomainsUrl) {
        this.restTemplate = restTemplate;
        this.agencyTokensFormat = agencyTokensFormat;
        this.agencyTokensByDomainFormat = agencyTokensByDomainFormat;
        this.agencyTokensByDomainAndOrganisationFormat = agencyTokensByDomainAndOrganisationFormat;
        this.organisationalUnitsFlatUrl = organisationalUnitsFlatUrl;
        this.whitelistedDomainsUrl = whitelistedDomainsUrl;
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

    public String[] getWhitelistedDomains(){

        String[] domains;

        try{
            WhitelistedDomainDto[] whitelistedDomainDtos = restTemplate.getForObject(whitelistedDomainsUrl, WhitelistedDomainDto[].class);

            domains = new String[whitelistedDomainDtos.length];
            Arrays.stream(whitelistedDomainDtos)
                    .map(domain -> domain.getDomain())
                    .collect(Collectors.toList())
                    .toArray(domains);
        }
        catch (HttpClientErrorException e){
            log.warn(e.getMessage());
            throw e;
        }

        return domains;
    }
}
