package uk.gov.cshr.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.domain.Domain;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.service.csrs.CsrsService;
import uk.gov.cshr.service.csrs.CsrsServiceClient;
import uk.gov.cshr.service.csrs.CsrsServiceDataTransformer;

import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CsrsServiceTest {

    private CsrsServiceClient csrsServiceClient = mock(CsrsServiceClient.class);
    private CsrsServiceDataTransformer csrsServiceDataTransformer = mock(CsrsServiceDataTransformer.class);
    CsrsService csrsService = new CsrsService(csrsServiceClient, csrsServiceDataTransformer);

    @Before
    public void before() {
        OrganisationalUnitDto org =  new OrganisationalUnitDto();
        org.setDomains(Collections.singletonList(new Domain(1L, "example.com")));
        when(csrsServiceClient.getAllOrganisations()).thenReturn(
                Collections.singletonList(org)
        );
    }

    @Test
    public void testIsallowlistedDomainMixedCase(){
        boolean validDomain = csrsService.isDomainAllowlisted("ExAmPlE.cOm");

        assertTrue(validDomain);
    }

    @Test
    public void testIsallowlistedDomainLowerCase(){
        boolean validDomain = csrsService.isDomainAllowlisted("example.com");

        assertTrue(validDomain);
    }

    @Test
    public void testIsallowlistedDomainUpperCase(){
        boolean validDomain = csrsService.isDomainAllowlisted("EXAMPLE.COM");

        assertTrue(validDomain);
    }
}
