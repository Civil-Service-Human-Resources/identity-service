package uk.gov.cshr.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.service.notifications.NotificationServiceClient;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotifyServiceTest {

    @Mock
    private NotificationServiceClient notificationClient;

    @Mock
    private EmailNotificationFactory emailNotificationFactory;

    @InjectMocks
    private NotifyService notifyService;

    @Test
    public void shouldSendPasswordUpdateNotification() {
        String email = "learner@domain.com";

        EmailNotification notification = new EmailNotification();

        when(emailNotificationFactory.createPasswordUpdateNotification(email)).thenReturn(notification);

        notifyService.sendPasswordUpdateNotification(email);

        verify(notificationClient).notify(notification);
    }

    @Test
    public void shouldSendEmailUpdateVerification() {
        String email = "learner@domain.com";
        String code = "verification-code";

        EmailNotification notification = new EmailNotification();

        when(emailNotificationFactory.createEmailAddressUpdateVerification(email, code)).thenReturn(notification);

        notifyService.sendEmailUpdateVerification(email, code);

        verify(notificationClient).notify(notification);
    }


    @Test
    public void shouldSendPasswordResetVerification() {
        String email = "learner@domain.com";
        String code = "verification-code";

        EmailNotification notification = new EmailNotification();

        when(emailNotificationFactory.createPasswordResetVerification(email, code)).thenReturn(notification);

        notifyService.sendPasswordResetVerification(email, code);

        verify(notificationClient).notify(notification);
    }

    @Test
    public void shouldSendPasswordResetNotification() {
        String email = "learner@domain.com";

        EmailNotification notification = new EmailNotification();

        when(emailNotificationFactory.createPasswordResetNotification(email)).thenReturn(notification);

        notifyService.sendPasswordResetNotification(email);

        verify(notificationClient).notify(notification);
    }

    @Test
    public void shouldSendInviteVerification() {
        String email = "learner@domain.com";
        String code = "verification-code";

        EmailNotification notification = new EmailNotification();

        when(emailNotificationFactory.createInviteVerification(email, code)).thenReturn(notification);

        notifyService.sendInviteVerification(email, code);

        verify(notificationClient).notify(notification);
    }

}