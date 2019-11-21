package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.dto.AgencyTokenDTO;
import uk.gov.cshr.dto.CheckValidTokenDTO;
import uk.gov.cshr.exception.*;

import java.util.Optional;

@Slf4j
@Service
public class CsrsService {
    private RestTemplate restTemplate;
    private String agencyTokensFormat;
    private String agencyTokensByDomainFormat;
    private String organisationalUnitsFlatUrl;
    private String updateSpacesAvailableUrl;
    private String getSpacesAvailableUrl;
    private String checkTokenAvailableUrl;

    public CsrsService(RestTemplate restTemplate,
                        @Value("${registry.agencyTokensFormat}") String agencyTokensFormat,
                        @Value("${registry.agencyTokensByDomainFormat}") String agencyTokensByDomainFormat,
                        @Value("${registry.organisationalUnitsFlatUrl}") String organisationalUnitsFlatUrl,
                        @Value("${registry.updateSpacesAvailableUrl}") String updateSpacesAvailableUrl,
                        @Value("${registry.getSpacesAvailableUrl}") String getSpacesAvailableUrl,
                        @Value("${registry.checkTokenAvailableUrl}") String checkTokenAvailableUrl)

    {
        this.restTemplate = restTemplate;
        this.agencyTokensFormat = agencyTokensFormat;
        this.agencyTokensByDomainFormat = agencyTokensByDomainFormat;
        this.organisationalUnitsFlatUrl = organisationalUnitsFlatUrl;
        this.updateSpacesAvailableUrl = updateSpacesAvailableUrl;
        this.checkTokenAvailableUrl = checkTokenAvailableUrl;
    }

    public AgencyToken[] getAgencyTokensForDomain(String domain) {
        try {
            return restTemplate.getForObject(String.format(agencyTokensByDomainFormat, domain), AgencyToken[].class);
        } catch (HttpClientErrorException e) {
            System.out.println(e);
            return new AgencyToken[]{};
        }
    }

    public Optional<AgencyToken> getAgencyTokenForDomainTokenOrganisation(String domain, String token, String organisation) {
        try {
            return Optional.of(restTemplate.getForObject(String.format(agencyTokensFormat, domain, token, organisation), AgencyToken.class));
        } catch (HttpClientErrorException e) {
            System.out.println(e);
            return Optional.empty();
        }
    }

    public boolean checkIfTokenValid(String domain, String token, String organisation, boolean removeUser) {
      //  try {
            return checkCsrs(domain, token, organisation, removeUser);
        /*} catch (HttpClientErrorException e) {
            log.warn("*****httpClientException");
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                throw new ResourceNotFoundException();
            } else if (HttpStatus.CONFLICT.equals(e.getStatusCode())) {
                throw new NotEnoughSpaceAvailableException("Not enough spaces available for AgencyToken " + token);
            } else {
                throw new BadRequestException();
            }
        }*/
    }

    public void updateSpacesAvailable(String domain, String token, String organisation, boolean removeUser) {
        try {
             // should
             updateCsrs(domain, token, organisation, removeUser);
        } catch (HttpClientErrorException e) {
            log.warn("*****httpClientException");
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                throw new ResourceNotFoundException();
            } else if (HttpStatus.CONFLICT.equals(e.getStatusCode())) {
                throw new NotEnoughSpaceAvailableException("Not enough spaces available for AgencyToken " + token);
            } else {
                throw new BadRequestException();
            }
        } catch (HttpServerErrorException e) {
            log.warn("*****httpServerException");
            throw new UnableToAllocateAgencyTokenException(String.format("Error: Unable to update AgencyToken %s ", token));
        } catch (Exception e) {
            log.warn("*****Exception");
            throw new UnableToAllocateAgencyTokenException(String.format("Unexpected Error: Unable to update AgencyToken %s ", token));
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

    private void updateCsrs(String domain, String token, String organisation, boolean removeUser) {
        AgencyTokenDTO requestDTO = buildAgencyTokenDTO(domain, token, organisation, removeUser);
        restTemplate.put(updateSpacesAvailableUrl, requestDTO);
    }

    public boolean checkCsrs(String domain, String token, String organisation, boolean isRemoveUser) {
            String requestURL = String.format(checkTokenAvailableUrl,
                    domain, token, organisation, isRemoveUser);

            ResponseEntity<CheckValidTokenDTO> responseEntity = restTemplate.getForEntity(requestURL, CheckValidTokenDTO.class);

            if(responseEntity != null) {
                if(responseEntity.getStatusCode() == HttpStatus.OK) {
                    return responseEntity.getBody().isValid();
                } else if(responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {
                    throw new ResourceNotFoundException();
                } else {
                    return false;
                }
            } else {
                return false;
            }
    }

    private AgencyTokenDTO buildAgencyTokenDTO(String domain, String token, String organisation, boolean removeUser) {
        AgencyTokenDTO agencyTokenDTO = new AgencyTokenDTO();
        agencyTokenDTO.setDomain(domain);
        agencyTokenDTO.setToken(token);
        agencyTokenDTO.setCode(organisation);
        agencyTokenDTO.setRemoveUser(removeUser);
        return agencyTokenDTO;
    }
}
