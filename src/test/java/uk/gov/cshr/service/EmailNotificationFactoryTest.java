package uk.gov.cshr.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EmailNotificationFactoryTest {

    private final String emailUpdateUrl = "email-update-url";
    private final String emailUpdateTemplateId = "email-update-template-id";
    private final String passwordUpdateTemplateId = "password-update-template-id";

    private final EmailNotificationFactory factory = new EmailNotificationFactory(emailUpdateUrl, emailUpdateTemplateId, passwordUpdateTemplateId);

    @Test
    public void shouldReturnEmailAddressUpdateVerification() {
        String code = "verification-code";
        String email = "learner@domain.com";

        EmailNotification notification = factory.createEmailAddressUpdateVerification(email, code);

        assertEquals(emailUpdateUrl + code, notification.getPersonalisation().get("link"));
        assertEquals(email, notification.getEmailAddress());
        assertEquals(emailUpdateTemplateId, notification.getTemplateId());
        assertNull(notification.getReference());
    }

    @Test
    public void shouldReturnPasswordUpdateNotification() {
        String email = "learner@domain.com";

        EmailNotification notification = factory.createPasswordUpdateNotification(email);

        assertEquals(passwordUpdateTemplateId, notification.getTemplateId());
        assertEquals(email, notification.getEmailAddress());
        assertNull(notification.getReference());
        assertNull(notification.getPersonalisation());
    }
}