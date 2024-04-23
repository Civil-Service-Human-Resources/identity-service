package uk.gov.cshr.service.csrs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.dto.AgencyTokenDTO;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsrsService {

    private final CsrsServiceClient csrsServiceClient;
    private final CsrsServiceDataTransformer csrsServiceDataTransformer;

    @Cacheable("allowlist")
    public List<String> getAllowlist() {
        return csrsServiceClient.getAllowlist();
    }

    public boolean isDomainAllowlisted(String domain) {
        return this.getAllowlist().contains(domain.toLowerCase(Locale.ROOT));
    }

    public List<OrganisationalUnitDto> getFilteredOrganisations(String domain) {
        return this.getAllOrganisations().stream().filter(o -> o.doesDomainExist(domain)).collect(Collectors.toList());
    }

    public boolean isDomainInAgency(String domain) {
        return csrsServiceClient.isDomainInAnAgencyToken(domain);
    }

    public boolean isAgencyTokenUidValidForDomain(String agencyTokenUid, String domain) {
        return csrsServiceClient.getAgencyTokenWithUid(agencyTokenUid)
                .map(token -> token.getUid().equals(agencyTokenUid) && token.isDomainAssignedToAgencyToken(domain))
                .orElse(false);
    }

    public boolean isDomainValid(String domain) {
        return !this.getFilteredOrganisations(domain).isEmpty();
    }

    public boolean isAgencyTokenUidValidForOrgAndDomain(String organisationCode, String domain) {
        return csrsServiceClient.isDomainInAnAgencyTokenWithOrg(domain, organisationCode);
    }

    public Optional<AgencyTokenDTO> getAgencyTokenForDomainTokenOrganisation(String domain, String token, String organisationCode) {
        return csrsServiceClient.getAgencyToken(domain, token, organisationCode);
    }

    @Cacheable("organisations")
    public List<OrganisationalUnitDto> getAllOrganisations() {
        List<OrganisationalUnitDto> organisationalUnitDtos = csrsServiceClient.getAllOrganisations();
        return csrsServiceDataTransformer.transformOrganisations(organisationalUnitDtos);
    }

    public void removeOrganisationalUnitFromCivilServant(String uid) {
        csrsServiceClient.removeOrganisationalUnitFromCivilServant(uid);
    }

}
