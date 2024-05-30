package uk.gov.cshr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.cshr.domain.Domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AgencyTokenDTO implements Serializable {

    private String uid;
    private String token;
    private Integer capacity;
    private List<Domain> agencyDomains = Collections.emptyList();

    public List<Domain> getAgencyDomains() {
        if (agencyDomains == null) {
            agencyDomains = Collections.emptyList();
        }
        return agencyDomains;
    }

    public boolean isDomainAssignedToAgencyToken(String domain) {
        return this.getAgencyDomains().stream().anyMatch(d -> d.getDomain().equals(domain));
    }
}
