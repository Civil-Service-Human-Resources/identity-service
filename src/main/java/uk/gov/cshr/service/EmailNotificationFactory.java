package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EmailNotificationFactory {

    private final String emailUpdateUrl;
    private final String emailUpdateTemplateId;

    public EmailNotificationFactory(@Value("${emailUpdate.url}") String emailUpdateUrl,
                         @Value("${emailUpdate.templateId}") String emailUpdateTemplateId
    ) {
        this.emailUpdateUrl = emailUpdateUrl;
        this.emailUpdateTemplateId = emailUpdateTemplateId;
    }

    public EmailNotification createEmailAddressUpdateVerification(String emailAddress, String code) {
        EmailNotification notification = new EmailNotification();
        String link = emailUpdateUrl + code;

        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("link", link);

        notification.setTemplateId(emailUpdateTemplateId);
        notification.setEmailAddress(emailAddress);
        notification.setPersonalisation(personalisation);

        return notification;
    }
}
