package uk.gov.cshr.service;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.domain.Reset;
import uk.gov.cshr.domain.ResetStatus;
import uk.gov.cshr.domain.factory.ResetFactory;
import uk.gov.cshr.repository.ResetRepository;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResetServiceTest {

    public static final String EMAIL = "test@example.com";
    public static final String CODE = "abc123";

    private final int validityInSeconds = 99;
    private ResetService resetService;

    @Mock
    private ResetRepository resetRepository;

    @Mock
    private NotifyService notifyService;

    @Mock
    private ResetFactory resetFactory;

    @Before
    public void setUp() {
        resetService = new ResetService(resetRepository, notifyService, resetFactory, validityInSeconds);
    }

    @Test
    public void shouldSaveNewResetWhenCreateNewResetForEmail() {
        Reset reset = new Reset();
        reset.setEmail(EMAIL);
        reset.setCode(CODE);

        when(resetFactory.create(EMAIL)).thenReturn(reset);
        resetService.notifyForResetRequest(EMAIL);

        verify(notifyService).sendPasswordResetVerification(EMAIL, CODE);
        verify(resetRepository).save(reset);
    }

    @Test
    public void shouldModifyExistingResetWhenResetSuccessFor() {
        Reset reset = new Reset();
        reset.setEmail(EMAIL);
        reset.setResetStatus(ResetStatus.PENDING);

        resetService.notifyOfSuccessfulReset(reset);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        assertEquals(dateFormat.format(new Date()), dateFormat.format(reset.getResetAt()));
        assertEquals(ResetStatus.RESET, reset.getResetStatus());

        verify(notifyService).sendPasswordResetNotification(EMAIL);
        verify(resetRepository).save(reset);
    }

    @Test
    public void isResetExpiredShouldReturnExpiredIfRequestedAtMoreThan24H() {
        Reset reset = new Reset();
        reset.setResetStatus(ResetStatus.PENDING);
        reset.setCode(CODE);
        reset.setRequestedAt(new Date(2323223232L));

        MatcherAssert.assertThat(resetService.isResetExpired(reset), equalTo(true));

        ArgumentCaptor<Reset> resetArgumentCaptor = ArgumentCaptor.forClass(Reset.class);
        verify(resetRepository).save(resetArgumentCaptor.capture());
        Reset actualReset = resetArgumentCaptor.getValue();

        MatcherAssert.assertThat(actualReset.getResetStatus(), equalTo(ResetStatus.EXPIRED));
    }
}