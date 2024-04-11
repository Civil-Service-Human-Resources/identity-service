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
}
