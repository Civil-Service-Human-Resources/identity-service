package uk.gov.cshr.service.csrs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.OrganisationalUnitDto;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsrsServiceDataTransformer {

    /**
     * - Format the agency token from parents onto descendants
     * @param organisationalUnitDtos
     * @return
     */
    public List<OrganisationalUnitDto> transformOrganisations(List<OrganisationalUnitDto> organisationalUnitDtos) {
        List<OrganisationalUnitDto> tree = this.transformOrgsIntoTree(organisationalUnitDtos);
        tree.forEach(OrganisationalUnitDto::applyAgencyTokenToDescendants);
        List<OrganisationalUnitDto> flattened = tree.stream().flatMap(o -> o.getHierarchyAsFlatList().stream()).collect(Collectors.toList());
        flattened.sort(Comparator.comparing(OrganisationalUnitDto::getFormattedName));
        return flattened;
    }

    public List<OrganisationalUnitDto> transformOrgsIntoTree(List<OrganisationalUnitDto> organisationalUnitDtos) {
        Map<Integer, Integer> orgIdMap = new HashMap<>();
        List<OrganisationalUnitDto> roots = new ArrayList<>();
        for (int i = 0; i < organisationalUnitDtos.size(); i++) {
            OrganisationalUnitDto organisationalUnitDto = organisationalUnitDtos.get(i);
            orgIdMap.put(organisationalUnitDto.getId(), i);
        }
        organisationalUnitDtos.forEach(o -> {
            if (o.getParentId() != null) {
                int index = orgIdMap.get(o.getParentId());
                organisationalUnitDtos.get(index).addDescendant(o);
            } else {
                roots.add(o);
            }
        });
        return roots;
    }
}
