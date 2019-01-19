package uk.gov.cshr.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.InvalidCodeException;
import uk.gov.cshr.repository.EmailUpdateRepository;
import uk.gov.cshr.service.security.IdentityService;

import java.util.Optional;

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

    @Mock
    private IdentityService identityService;

    @InjectMocks
    private EmailUpdateService emailUpdateService;

    @Test
    public void shouldReturnCodeAfterSavingEmailUpdate() {
        String newEmail = "new-email";

        Identity identity = mock(Identity.class);

        EmailUpdate emailUpdate = new EmailUpdate();

        when(emailUpdateFactory.create(identity, newEmail)).thenReturn(emailUpdate);

        String code = emailUpdateService.saveEmailUpdateAndNotify(identity, newEmail);

        assertEquals(emailUpdate.getCode(), code);

        verify(emailUpdateRepository).save(emailUpdate);
        verify(notifyService).sendEmailUpdateVerification(newEmail, code);
    }

    @Test
    public void shouldUpdateEmailAddress() {
        Identity identity = mock(Identity.class);
        String code = "_code";
        String emailAddress = "learner@domain.com";
        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setEmail(emailAddress);

        when(emailUpdateRepository.findByIdentityAndCode(identity, code)).thenReturn(Optional.of(emailUpdate));

        emailUpdateService.updateEmailAddress(identity, code);

        verify(identityService).updateEmailAddress(identity, emailAddress);
        verify(emailUpdateRepository).delete(emailUpdate);
    }

    @Test
    public void shouldThrowInvalidCodeException() {
        Identity identity = new Identity();
        String code = "_code";
        String emailAddress = "learner@domain.com";
        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setEmail(emailAddress);

        when(emailUpdateRepository.findByIdentityAndCode(identity, code)).thenReturn(Optional.empty());

        try {
            emailUpdateService.updateEmailAddress(identity, code);
        } catch (InvalidCodeException e) {
            assertEquals("Code _code does not exist for identity Identity{id=null, uid='null', " +
                    "email='null', password='null', active=false, locked=false, roles=null}", e.getMessage());
        }
    }
}