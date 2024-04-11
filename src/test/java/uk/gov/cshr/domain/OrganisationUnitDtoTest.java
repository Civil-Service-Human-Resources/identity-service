package uk.gov.cshr.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.dto.AgencyTokenDTO;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class OrganisationUnitDtoTest {

    @Test
    public void testAddAgencyTokenToDescendants() {
        OrganisationalUnitDto parent = new OrganisationalUnitDto();
        parent.setFormattedName("PARENT");
        parent.setAgencyToken(new AgencyTokenDTO("uid1", "token1", 1, Arrays.asList(new Domain(1L, "domain1.com"), new Domain(1L, "domain2.com"))));

        OrganisationalUnitDto agencyChild = new OrganisationalUnitDto();
        agencyChild.setFormattedName("PARENT | AGENCY_CHILD");
        parent.addDescendant(agencyChild);

        OrganisationalUnitDto agencyChildSibling = new OrganisationalUnitDto();
        agencyChildSibling.setFormattedName("PARENT | AGENCY_CHILD_SIBLING");
        agencyChildSibling.setAgencyToken(new AgencyTokenDTO("uid2", "token2", 1, Arrays.asList(new Domain(1L, "domain1.com"), new Domain(1L, "domain2.com"))));
        parent.addDescendant(agencyChildSibling);

        OrganisationalUnitDto agencyChildSiblingChild = new OrganisationalUnitDto();
        agencyChildSiblingChild.setFormattedName("PARENT | AGENCY_CHILD_SIBLING_CHILD");
        agencyChildSiblingChild.setAgencyToken(new AgencyTokenDTO("uid2", "token2", 1, Arrays.asList(new Domain(1L, "domain1.com"), new Domain(1L, "domain2.com"))));
        agencyChildSibling.addDescendant(agencyChildSiblingChild);

        OrganisationalUnitDto agencyGrandchild = new OrganisationalUnitDto();
        agencyGrandchild.setFormattedName("PARENT | AGENCY_CHILD | AGENCY_GRANDCHILD");
        agencyChild.addDescendant(agencyGrandchild);

        OrganisationalUnitDto agencyGrandchildSibling = new OrganisationalUnitDto();
        agencyGrandchild.setFormattedName("PARENT | AGENCY_CHILD | AGENCY_GRANDCHILD_SIBLING");
        agencyChild.addDescendant(agencyGrandchildSibling);

        parent.applyAgencyTokenToDescendants();

        assertEquals(agencyChild.getAgencyToken().getToken(), "token1");
        assertEquals(agencyGrandchild.getAgencyToken().getToken(), "token1");
        assertEquals(agencyChildSibling.getAgencyToken().getToken(), "token2");
        assertEquals(agencyChildSiblingChild.getAgencyToken().getToken(), "token2");
        assertEquals(agencyGrandchildSibling.getAgencyToken().getToken(), "token1");

    }
}
