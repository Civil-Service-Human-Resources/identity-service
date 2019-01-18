package uk.gov.cshr.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.EmailUpdateRepository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmailUpdateServiceTest {

    @Mock
    private EmailUpdateFactory emailUpdateFactory;

    @Mock
    private EmailUpdateRepository emailUpdateRepository;

    @Mock
    private NotifyService notifyService;

    @InjectMocks
    private EmailUpdateService emailUpdateService;

    @Test
    public void shouldReturnCodeAfterSavingEmailUpdate() {
        String newEmail = "new-email";
        String oldEmail = "old-email";

        Identity identity = mock(Identity.class);
        when(identity.getEmail()).thenReturn(oldEmail);

        EmailUpdate emailUpdate = new EmailUpdate();

        when(emailUpdateFactory.create(identity, newEmail)).thenReturn(emailUpdate);

        String code = emailUpdateService.saveEmailUpdateAndNotify(identity, newEmail);

        assertEquals(emailUpdate.getCode(), code);

        verify(emailUpdateRepository).save(emailUpdate);
        verify(notifyService).sendEmailUpdateVerification(oldEmail, code);
    }
}