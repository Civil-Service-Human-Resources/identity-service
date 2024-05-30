package uk.gov.cshr.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.domain.AgencyTokenCapacityUsedDto;
import uk.gov.cshr.dto.AgencyTokenDTO;
import uk.gov.cshr.repository.IdentityRepository;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AgencyTokenCapacityServiceTest {

    private static final String UID = "UID";

    @Mock
    private IdentityRepository identityRepository;

    @InjectMocks
    private AgencyTokenCapacityService agencyTokenCapacityService;

    @Test
    public void shouldReturnTrueIfManySpacesAvailable() {
        AgencyTokenDTO agencyToken = new AgencyTokenDTO();
        agencyToken.setUid(UID);
        agencyToken.setCapacity(100);

        when(identityRepository.countByAgencyTokenUid(UID)).thenReturn(1L);

        assertTrue(agencyTokenCapacityService.hasSpaceAvailable(agencyToken));
    }

    @Test
    public void shouldReturnTrueIfOneSpaceAvailable() {
        AgencyTokenDTO agencyToken = new AgencyTokenDTO();
        agencyToken.setUid(UID);
        agencyToken.setCapacity(100);

        when(identityRepository.countByAgencyTokenUid(UID)).thenReturn(99L);

        assertTrue(agencyTokenCapacityService.hasSpaceAvailable(agencyToken));
    }

    @Test
    public void shouldReturnFalseIfNoSpaceAvailable() {
        AgencyTokenDTO agencyToken = new AgencyTokenDTO();
        agencyToken.setUid(UID);
        agencyToken.setCapacity(100);

        when(identityRepository.countByAgencyTokenUid(UID)).thenReturn(100L);

        assertFalse(agencyTokenCapacityService.hasSpaceAvailable(agencyToken));
    }

    @Test
    public void shouldReturnFalseIfNoSpaceAvailableWhereCapacityReduced() {
        AgencyTokenDTO agencyToken = new AgencyTokenDTO();
        agencyToken.setUid(UID);
        agencyToken.setCapacity(100);

        when(identityRepository.countByAgencyTokenUid(UID)).thenReturn(101L);

        assertFalse(agencyTokenCapacityService.hasSpaceAvailable(agencyToken));
    }

    @Test
    public void shouldReturnSpacesUsedByAgencyToken() {
        AgencyTokenCapacityUsedDto expected = new AgencyTokenCapacityUsedDto(new Long(100));

        when(identityRepository.countByAgencyTokenUid(UID)).thenReturn(100L);

        assertEquals(expected, agencyTokenCapacityService.getSpacesUsedByAgencyToken(UID));
    }

    @Test
    public void shouldReturnCountByAgencyToken() {
        when(identityRepository.countByAgencyTokenUid(UID)).thenReturn(100L);

        Long countOfAgencyByUid = agencyTokenCapacityService.getCountOfAgencyByUid(UID);

        assertEquals(new Long(100), countOfAgencyByUid);
    }

    @Test
    public void deleteAgencyToken_shouldCallremoveAgencyToken() {
        String agencyToken = UUID.randomUUID().toString();
        agencyTokenCapacityService.deleteAgencyToken(agencyToken);
        verify(identityRepository, times(1)).removeAgencyToken(agencyToken);
    }
}
