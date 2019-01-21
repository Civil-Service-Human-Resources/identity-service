package uk.gov.cshr.service;

import org.springframework.stereotype.Service;
import uk.gov.cshr.service.notifications.NotificationServiceClient;

@Service
public class NotifyService {
    private final NotificationServiceClient notificationClient;
    private final EmailNotificationFactory emailNotificationFactory;

    public NotifyService(NotificationServiceClient notificationClient, EmailNotificationFactory emailNotificationFactory) {
        this.notificationClient = notificationClient;
        this.emailNotificationFactory = emailNotificationFactory;
    }

    public void sendPasswordResetVerification(String email, String code) {
        notificationClient.notify(emailNotificationFactory.createPasswordResetVerification(email, code));
    }

    public void sendInviteVerification(String email, String code) {
        notificationClient.notify(emailNotificationFactory.createInviteVerification(email, code));
    }

    public void sendPasswordUpdateNotification(String email) {
        notificationClient.notify(emailNotificationFactory.createPasswordUpdateNotification(email));
    }

    public void sendEmailUpdateVerification(String email, String code) {
        notificationClient.notify(emailNotificationFactory.createEmailAddressUpdateVerification(email, code));
    }

    public void sendPasswordResetNotification(String email) {
        notificationClient.notify(emailNotificationFactory.createPasswordResetNotification(email));
    }
}
