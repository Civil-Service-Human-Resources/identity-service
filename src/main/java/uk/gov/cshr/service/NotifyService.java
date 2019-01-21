package uk.gov.cshr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.exception.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.HashMap;

@Service
@Transactional
public class NotifyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InviteService.class);
    private static final String EMAIL_PERMISSION = "email";
    private static final String ACTIVATION_URL_PERMISSION = "activationUrl";

    private final NotificationClient notificationClient;
    private final EmailNotificationFactory emailNotificationFactory;

    public NotifyService(NotificationClient notificationClient, EmailNotificationFactory emailNotificationFactory) {
        this.notificationClient = notificationClient;
        this.emailNotificationFactory = emailNotificationFactory;
    }

    public void sendResetVerification(String email, String code, String templateId, String actionUrl) throws NotificationClientException {
        String activationUrl = String.format(actionUrl, code);

        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put(EMAIL_PERMISSION, email);
        personalisation.put(ACTIVATION_URL_PERMISSION, activationUrl);

        SendEmailResponse response = notificationClient.sendEmail(templateId, email, personalisation, "");

        LOGGER.info("Notify email sent to: {}", response.getBody());
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

    private void notify(EmailNotification notification) {
        try {
            SendEmailResponse response = notificationClient.sendEmail(notification.getTemplateId(), notification.getEmailAddress(),
                            notification.getPersonalisation(), notification.getReference());
            LOGGER.info("Notification sent: {}", response.getBody());
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }
}
