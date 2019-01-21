package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EmailNotificationFactory {

    private final String emailUpdateUrlFormat;
    private final String emailUpdateTemplateId;
    private final String passwordUpdateTemplateId;

    public EmailNotificationFactory(@Value("${emailUpdate.urlFormat}") String emailUpdateUrlFormat,
                        @Value("${govNotify.template.emailUpdate}") String emailUpdateTemplateId,
                        @Value("${govNotify.template.passwordUpdate}") String passwordUpdateTemplateId
    ) {
        this.emailUpdateUrlFormat = emailUpdateUrlFormat;
        this.emailUpdateTemplateId = emailUpdateTemplateId;
        this.passwordUpdateTemplateId = passwordUpdateTemplateId;
    }

    public EmailNotification createEmailAddressUpdateVerification(String emailAddress, String code) {
        EmailNotification notification = new EmailNotification();
        String link = String.format(emailUpdateUrlFormat, code);

        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("link", link);

        notification.setTemplateId(emailUpdateTemplateId);
        notification.setEmailAddress(emailAddress);
        notification.setPersonalisation(personalisation);

        return notification;
    }

    public EmailNotification createPasswordUpdateNotification(String email) {
        EmailNotification notification = new EmailNotification();
        notification.setTemplateId(passwordUpdateTemplateId);
        notification.setEmailAddress(email);
        return notification;
    }
}
