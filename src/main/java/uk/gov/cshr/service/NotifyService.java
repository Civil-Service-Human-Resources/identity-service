package uk.gov.cshr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.cshr.exception.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

@Service
public class NotifyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InviteService.class);

    private final NotificationClient notificationClient;
    private final EmailNotificationFactory emailNotificationFactory;

    public NotifyService(NotificationClient notificationClient, EmailNotificationFactory emailNotificationFactory) {
        this.notificationClient = notificationClient;
        this.emailNotificationFactory = emailNotificationFactory;
    }

    public void sendPasswordResetVerification(String email, String code) {
        notify(emailNotificationFactory.createPasswordResetVerification(email, code));
    }

    public void sendInviteVerification(String email, String code) {
        notify(emailNotificationFactory.createInviteVerification(email, code));
    }

    public void sendPasswordUpdateNotification(String email) {
        notify(emailNotificationFactory.createPasswordUpdateNotification(email));
    }

    public void sendEmailUpdateVerification(String email, String code) {
        notify(emailNotificationFactory.createEmailAddressUpdateVerification(email, code));
    }

    public void sendPasswordResetNotification(String email) {
        notify(emailNotificationFactory.createPasswordResetNotification(email));
    }

    private void notify(EmailNotification notification) {
        try {
            SendEmailResponse response = notificationClient.sendEmail(notification.getTemplateId(), notification.getRecipient(),
                            notification.getPersonalisation(), notification.getReference());
            LOGGER.info("Notification sent: {}", response.getBody());
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }
}
