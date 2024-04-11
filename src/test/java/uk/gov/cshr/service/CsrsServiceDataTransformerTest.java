package uk.gov.cshr.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.domain.Domain;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.dto.AgencyTokenDTO;
import uk.gov.cshr.service.csrs.CsrsServiceDataTransformer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CsrsServiceDataTransformerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private CsrsServiceDataTransformer csrsServiceDataTransformer;

    @Before
    public void setUp() {
        csrsServiceDataTransformer = new CsrsServiceDataTransformer();
    }

    private List<OrganisationalUnitDto> getSampleOrganisations() {
        OrganisationalUnitDto parent = new OrganisationalUnitDto();
        parent.setId(1);
        parent.setFormattedName("PARENT");

        OrganisationalUnitDto agencyChild = new OrganisationalUnitDto();
        agencyChild.setId(3);
        agencyChild.setParentId(1);
        agencyChild.setFormattedName("PARENT | AGENCY_CHILD");
        agencyChild.setAgencyToken(new AgencyTokenDTO("uid1", "token1", 1, Arrays.asList(new Domain(1L, "domain1.com"), new Domain(1L, "domain2.com"))));

        OrganisationalUnitDto agencyGrandchild = new OrganisationalUnitDto();
        agencyGrandchild.setId(6);
        agencyGrandchild.setParentId(3);
        agencyGrandchild.setFormattedName("PARENT | AGENCY_CHILD | AGENCY_GRANDCHILD");

        OrganisationalUnitDto parent2 = new OrganisationalUnitDto();
        parent2.setId(7);
        parent2.setFormattedName("PARENT2");
        parent2.setDomains(Collections.singletonList(new Domain(1L, "domain2.com")));

        OrganisationalUnitDto child2 = new OrganisationalUnitDto();
        child2.setId(10);
        child2.setParentId(7);
        child2.setFormattedName("PARENT2 | CHILD2");
        child2.setDomains(Collections.singletonList(new Domain(1L, "domain3.com")));

        OrganisationalUnitDto otherAgency = new OrganisationalUnitDto();
        otherAgency.setId(14);
        otherAgency.setFormattedName("OTHER_AGENCY");
        otherAgency.setAgencyToken(new AgencyTokenDTO("uid2", "token2", 1, Collections.singletonList(new Domain(1L, "domain2.com"))));

        OrganisationalUnitDto otherAgencyChild = new OrganisationalUnitDto();
        otherAgencyChild.setId(20);
        otherAgencyChild.setParentId(14);
        otherAgencyChild.setFormattedName("OTHER_AGENCY | CHILD");
        otherAgencyChild.setAgencyToken(new AgencyTokenDTO("uid3", "token3", 1, Collections.singletonList(new Domain(1L, "domain2.com"))));

        return Arrays.asList(child2, otherAgencyChild, agencyGrandchild, parent2, agencyChild, parent, otherAgency);
    }

    @Test
    public void shouldTransformOrganisations() {
        List<OrganisationalUnitDto> result = csrsServiceDataTransformer.transformOrganisations(getSampleOrganisations());
        assertEquals(7, result.size());
        assertEquals(result.get(0).getFormattedName(), "OTHER_AGENCY");
        assertEquals(result.get(0).getAgencyToken().getToken(), "token2");
        assertEquals(result.get(1).getFormattedName(), "OTHER_AGENCY | CHILD");
        assertEquals(result.get(1).getAgencyToken().getToken(), "token3");
        assertEquals(result.get(2).getFormattedName(), "PARENT");
        assertEquals(result.get(3).getFormattedName(), "PARENT | AGENCY_CHILD");
        assertEquals(result.get(3).getAgencyToken().getToken(), "token1");
        assertEquals(result.get(4).getFormattedName(), "PARENT | AGENCY_CHILD | AGENCY_GRANDCHILD");
        assertEquals(result.get(4).getAgencyToken().getToken(), "token1");
        assertEquals(result.get(5).getFormattedName(), "PARENT2");
        assertEquals(result.get(6).getFormattedName(), "PARENT2 | CHILD2");
    }

}
