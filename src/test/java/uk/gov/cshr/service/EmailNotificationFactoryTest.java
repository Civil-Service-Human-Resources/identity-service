package uk.gov.cshr.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EmailNotificationFactoryTest {

    private final String emailUpdateUrlFormat = "http://localhost:8080/account/email/verify/%s?redirect=true";
    private final String emailUpdateTemplateId = "email-update-template-id";
    private final String passwordUpdateTemplateId = "password-update-template-id";
    private final String inviteTemplateId = "invite-template-id";
    private final String inviteUrlFormat = "http://localhost:8080/signup/%s";
    private final String passwordResetVerificationTemplateId = "password-reset-verification-template-id";
    private final String passwordResetActivationUrlFormat = "http://localhost:8080/reset/%s";
    private final String passwordResetNotificationTemplateId = "password-reset-notification-template-id";

    private final EmailNotificationFactory factory = new EmailNotificationFactory(
            emailUpdateUrlFormat,
            emailUpdateTemplateId,
            passwordUpdateTemplateId,
            inviteTemplateId,
            inviteUrlFormat,
            passwordResetVerificationTemplateId,
            passwordResetActivationUrlFormat,
            passwordResetNotificationTemplateId
    );

    @Test
    public void shouldReturnEmailAddressUpdateVerification() {
        String code = "xxx";
        String email = "learner@domain.com";

        EmailNotification notification = factory.createEmailAddressUpdateVerification(email, code);

        assertEquals("http://localhost:8080/account/email/verify/xxx?redirect=true", notification.getPersonalisation().get("link"));
        assertEquals(email, notification.getRecipient());
        assertEquals(emailUpdateTemplateId, notification.getTemplateId());
        assertNull(notification.getReference());
    }

    @Test
    public void shouldReturnPasswordUpdateNotification() {
        String email = "learner@domain.com";

        EmailNotification notification = factory.createPasswordUpdateNotification(email);

        assertEquals(passwordUpdateTemplateId, notification.getTemplateId());
        assertEquals(email, notification.getRecipient());
        assertNull(notification.getReference());
        assertNull(notification.getPersonalisation());
    }

    @Test
    public void shouldReturnInviteVerification() {
        String email = "learner@domain.com";
        String code = "xxx";

        EmailNotification notification = factory.createInviteVerification(email, code);

        assertEquals(email, notification.getRecipient());
        assertEquals(inviteTemplateId, notification.getTemplateId());
        assertEquals(String.format(inviteUrlFormat, code), notification.getPersonalisation().get("activationUrl"));
        assertEquals(email, notification.getPersonalisation().get("email"));
        assertNull(notification.getReference());
    }

    @Test
    public void shouldReturnResetVerification() {
        String email = "learner@domain.com";
        String code = "xxx";

        EmailNotification notification = factory.createPasswordResetVerification(email, code);

        assertEquals(email, notification.getRecipient());
        assertEquals(passwordResetVerificationTemplateId, notification.getTemplateId());
        assertEquals(String.format(passwordResetActivationUrlFormat, code), notification.getPersonalisation().get("activationUrl"));
        assertEquals(email, notification.getPersonalisation().get("email"));
        assertNull(notification.getReference());
    }

    @Test
    public void shouldReturnPasswordResetNotification() {
        String email = "learner@domain.com";

        EmailNotification notification = factory.createPasswordResetNotification(email);

        assertEquals(email, notification.getRecipient());
        assertEquals(passwordResetNotificationTemplateId, notification.getTemplateId());
        assertEquals(email, notification.getPersonalisation().get("email"));
        assertNull(notification.getReference());
    }
}