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

//    /**
//     * - Format the agency token from parents onto descendants
//     * @param organisationalUnitDtos
//     * @return
//     */
//    public List<OrganisationalUnitDto> transformOrganisations(List<OrganisationalUnitDto> organisationalUnitDtos) {
//        List<OrganisationalUnitDto> result = new ArrayList<>();
//        List<Integer> agencyOrganisations = new ArrayList<>();
//        Map<Integer, OrganisationalUnitDto> orgMap = organisationalUnitDtos.stream().collect(Collectors.toMap(OrganisationalUnitDto::getId, Function.identity()));
//        orgMap.forEach((id, org) -> {
//            Integer parentId = org.getParentId();
//            if (parentId != null) {
//                orgMap.get(parentId).addDescendant(org);
//            }
//            result.add(org);
//            if (org.getAgencyToken() != null) {
//                agencyOrganisations.add(org.getId());
//            }
//        });
//        agencyOrganisations.forEach(i -> {
//            OrganisationalUnitDto agencyOrganisation = orgMap.get(i);
//            AgencyTokenDTO agencyToken = agencyOrganisation.getAgencyToken();
//            List<OrganisationalUnitDto> orgs = agencyOrganisation.getHierarchyAsFlatList().stream()
//                    .peek(o -> {
//                        if (o.getAgencyToken() == null) {
//                            o.setAgencyToken(agencyToken);
//                        }
//                    })
//                    .collect(Collectors.toList());
//            result.addAll(orgs);
//        });
//        result.sort(Comparator.comparing(OrganisationalUnitDto::getFormattedName));
//        return result;
//    }

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
