package uk.gov.cshr.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.domain.ReactivationStatus;
import uk.gov.cshr.exception.IdentityNotFoundException;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.ReactivationRepository;
import uk.gov.cshr.service.security.IdentityService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReactivationServiceTest {

    private static final String EMAIL = "test@example.com";
    private static final String CODE = "code";
    private static final String UID = "UID";
    @Mock
    private ReactivationRepository reactivationRepository;

    @Mock
    private IdentityService identityService;

    @Captor
    private ArgumentCaptor<Reactivation> reactivationArgumentCaptor;

    @InjectMocks
    private ReactivationService reactivationService;

    @Test
    public void shouldReactivateIdentity() {
        Reactivation reactivation = new Reactivation();
        reactivation.setEmail(EMAIL);
        reactivation.setCode(CODE);

        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(UID);

        Identity identity = new Identity();

        ArgumentCaptor<Reactivation> reactivationArgumentCaptor = ArgumentCaptor.forClass(Reactivation.class);

        when(identityService.getIdentityByEmailAndActiveFalse(EMAIL)).thenReturn(identity);
        doNothing().when(identityService).reactivateIdentity(identity, agencyToken);

        reactivationService.reactivateIdentity(reactivation, agencyToken);

        verify(reactivationRepository).save(reactivationArgumentCaptor.capture());

        Reactivation reactivationArgumentCaptorValue = reactivationArgumentCaptor.getValue();
        assertEquals(ReactivationStatus.REACTIVATED, reactivationArgumentCaptorValue.getReactivationStatus());
    }

    @Test(expected = IdentityNotFoundException.class)
    public void shouldThrowExceptionIfIdentityNotFound() {
        Reactivation reactivation = new Reactivation();
        reactivation.setEmail(EMAIL);
        reactivation.setCode(CODE);

        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(UID);

        Identity identity = new Identity();

        doThrow(new IdentityNotFoundException("Identity not found")).when(identityService).getIdentityByEmailAndActiveFalse(EMAIL);

        reactivationService.reactivateIdentity(reactivation, agencyToken);
    }

    @Test
    public void shouldGetReactivationByCodeAndStatus() {
        Reactivation reactivation = new Reactivation();
        reactivation.setCode(CODE);

        when(reactivationRepository
                .findFirstByCodeAndReactivationStatusEquals(CODE, ReactivationStatus.PENDING))
                .thenReturn(Optional.of(reactivation));

        assertEquals(reactivation, reactivationService.getReactivationByCodeAndStatus(CODE, ReactivationStatus.PENDING));
    }

    @Test
    public void shouldReturnTrueIfExistsByReactivationByCodeAndStatus() {
        Reactivation reactivation = new Reactivation();
        reactivation.setCode(CODE);

        when(reactivationRepository
                .existsByCodeAndReactivationStatusEquals(CODE, ReactivationStatus.PENDING)).thenReturn(true);

        assertTrue(reactivationService.existsByCodeAndStatus(CODE, ReactivationStatus.PENDING));
    }

    @Test
    public void shouldReturnFalseIfDoesNotExistByReactivationByCodeAndStatus() {
        Reactivation reactivation = new Reactivation();
        reactivation.setCode(CODE);

        when(reactivationRepository
                .existsByCodeAndReactivationStatusEquals(CODE, ReactivationStatus.PENDING)).thenReturn(false);

        assertFalse(reactivationService.existsByCodeAndStatus(CODE, ReactivationStatus.PENDING));
    }


    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowResourceNotFoundExceptionIfReactivationDoesNotExist() {
        when(reactivationRepository
                .findFirstByCodeAndReactivationStatusEquals(CODE, ReactivationStatus.PENDING))
                .thenReturn(Optional.empty());

        reactivationService.getReactivationByCodeAndStatus(CODE, ReactivationStatus.PENDING);
    }

    @Test
    public void pendingExistsByEmailReturnsTrueIfPendingReactivationsExistForEmail(){
        String email = "my.name@myorg.gov.uk";

        when(reactivationRepository.existsByEmailAndReactivationStatusEqualsAndRequestedAtAfter(eq(email), eq(ReactivationStatus.PENDING), any(Date.class)))
                .thenReturn(true);

        boolean pendingReactivationExists = reactivationService.pendingExistsByEmail(email);
        assertTrue(pendingReactivationExists);
    }

    @Test
    public void pendingExistsByEmailReturnsFalseIfNoPendingReactivationsExistForEmail(){
        String email = "my.name@myorg.gov.uk";

        when(reactivationRepository.existsByEmailAndReactivationStatusEqualsAndRequestedAtAfter(eq(email), eq(ReactivationStatus.PENDING), any(Date.class)))
                .thenReturn(false);

        boolean pendingReactivationExists = reactivationService.pendingExistsByEmail(email);
        assertFalse(pendingReactivationExists);
    }


}
