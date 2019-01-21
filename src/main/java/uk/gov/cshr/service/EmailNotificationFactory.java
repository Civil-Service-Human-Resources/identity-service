package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EmailNotificationFactory {
    private static final String EMAIL_PERSONALISATION_KEY = "email";
    private static final String ACTIVATION_URL_PERSONALISATION_KEY = "activationUrl";

    private final String emailUpdateUrlFormat;
    private final String emailUpdateTemplateId;
    private final String passwordUpdateTemplateId;
    private final String inviteTemplateId;
    private final String inviteUrlFormat;
    private final String resetPasswordVerificationTemplateId;
    private final String resetPasswordVerificationUrlFormat;
    private final String resetPasswordNotificationTemplateId;


    public EmailNotificationFactory(@Value("${emailUpdate.urlFormat}") String emailUpdateUrlFormat,
                                    @Value("${govNotify.template.emailUpdate}") String emailUpdateTemplateId,
                                    @Value("${govNotify.template.passwordUpdate}") String passwordUpdateTemplateId,
                                    @Value("${govNotify.template.invite}") String inviteTemplateId,
                                    @Value("${invite.urlFormat}") String inviteUrlFormat,
                                    @Value("${govNotify.template.reset}") String resetPasswordVerificationTemplateId,
                                    @Value("${reset.urlFormat}") String resetPasswordVerificationUrlFormat,
                                    @Value("${govNotify.template.resetSuccessful}") String resetPasswordNotificationTemplateId
    ) {
        this.emailUpdateUrlFormat = emailUpdateUrlFormat;
        this.emailUpdateTemplateId = emailUpdateTemplateId;
        this.passwordUpdateTemplateId = passwordUpdateTemplateId;
        this.inviteTemplateId = inviteTemplateId;
        this.inviteUrlFormat = inviteUrlFormat;
        this.resetPasswordVerificationTemplateId = resetPasswordVerificationTemplateId;
        this.resetPasswordVerificationUrlFormat = resetPasswordVerificationUrlFormat;
        this.resetPasswordNotificationTemplateId = resetPasswordNotificationTemplateId;
    }

    public EmailNotification createPasswordResetVerification(String email, String code) {
        String activationUrl = String.format(resetPasswordVerificationUrlFormat, code);

        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put(EMAIL_PERSONALISATION_KEY, email);
        personalisation.put(ACTIVATION_URL_PERSONALISATION_KEY, activationUrl);

        EmailNotification notification = new EmailNotification();
        notification.setRecipient(email);
        notification.setPersonalisation(personalisation);
        notification.setTemplateId(resetPasswordVerificationTemplateId);

        return notification;
    }

    public EmailNotification createInviteVerification(String email, String code) {
        String activationUrl = String.format(inviteUrlFormat, code);

        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put(EMAIL_PERSONALISATION_KEY, email);
        personalisation.put(ACTIVATION_URL_PERSONALISATION_KEY, activationUrl);

        EmailNotification notification = new EmailNotification();
        notification.setRecipient(email);
        notification.setPersonalisation(personalisation);
        notification.setTemplateId(inviteTemplateId);

        return notification;
    }

    public EmailNotification createEmailAddressUpdateVerification(String emailAddress, String code) {
        EmailNotification notification = new EmailNotification();
        String link = String.format(emailUpdateUrlFormat, code);

        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("link", link);

        notification.setTemplateId(emailUpdateTemplateId);
        notification.setRecipient(emailAddress);
        notification.setPersonalisation(personalisation);

        return notification;
    }

    public EmailNotification createPasswordUpdateNotification(String email) {
        EmailNotification notification = new EmailNotification();
        notification.setTemplateId(passwordUpdateTemplateId);
        notification.setRecipient(email);
        return notification;
    }

    public EmailNotification createPasswordResetNotification(String email) {
        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put(EMAIL_PERSONALISATION_KEY, email);

        EmailNotification notification = new EmailNotification();
        notification.setRecipient(email);
        notification.setPersonalisation(personalisation);
        notification.setTemplateId(resetPasswordNotificationTemplateId);

        return notification;
    }
}
