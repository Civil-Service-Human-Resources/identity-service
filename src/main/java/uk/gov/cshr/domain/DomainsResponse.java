package uk.gov.cshr.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DomainsResponse {

    @JsonProperty("_embedded")
    public void setDomains(Map<String, List<Domain>> embeddedDomains) {
        this.domains = embeddedDomains.get("domains");
    }

    private List<Domain> domains;
}
