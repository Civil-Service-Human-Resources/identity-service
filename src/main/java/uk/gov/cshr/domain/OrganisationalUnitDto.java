package uk.gov.cshr.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.cshr.dto.AgencyTokenDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationalUnitDto implements Serializable {
    private Integer id;
    protected String name;
    protected String href;
    protected String abbreviation;
    protected String formattedName;
    private Integer parentId;
    private String code;
    private List<Domain> domains = Collections.emptyList();
    private AgencyTokenDTO agencyToken;
    private List<OrganisationalUnitDto> children = new ArrayList<>();

    public List<String> getDomains() {
        return domains.stream().map(Domain::getDomain).collect(Collectors.toList());
    }

    public boolean doesDomainExist(String domain) {
        return isDomainLinked(domain) || isDomainAgencyAssigned(domain);
    }

    public boolean isDomainLinked(String domain) {
        return this.getDomains().contains(domain);
    }

    public boolean isDomainAgencyAssigned(String domain) {
        if (agencyToken != null) {
            return agencyToken.getAgencyDomains().stream().anyMatch(d -> d.getDomain().equals(domain));
        }
        return false;
    }

    public List<OrganisationalUnitDto> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    public void addDescendant(OrganisationalUnitDto org) {
        this.getChildren().add(org);
    }

    public List<OrganisationalUnitDto> getHierarchyAsFlatList() {
        ArrayList<OrganisationalUnitDto> hierarchy = new ArrayList<>(Collections.singletonList(this));
        getChildren().forEach(c -> hierarchy.addAll(c.getHierarchyAsFlatList()));
        return hierarchy;
    }

    public void applyAgencyTokenToDescendants() {
        this.getChildren().forEach(o -> {
            if (o.agencyToken == null) {
                o.setAgencyToken(this.getAgencyToken());
            }
            o.applyAgencyTokenToDescendants();
        });
    }

}
