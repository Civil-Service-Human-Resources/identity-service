package uk.gov.cshr.service.csrs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.cshr.domain.OrganisationalUnitDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetOrganisationsResponse {
    private List<OrganisationalUnitDto> content;
    private Integer page;
    private Integer totalPages;
    private Integer totalElements;
    private Integer size;
}
