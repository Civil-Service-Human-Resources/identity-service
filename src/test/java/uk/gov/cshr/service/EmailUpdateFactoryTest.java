package uk.gov.cshr.service;

import org.junit.Test;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmailUpdateFactoryTest {

    private final EmailUpdateFactory factory = new EmailUpdateFactory();

    @Test
    public void shouldReturnEmailUpdate() {
        Identity identity = new Identity();

        String email = "learner@domain.com";

        EmailUpdate emailUpdate = factory.create(identity, email);

        assertTrue(emailUpdate.getTimestamp().isAfter(Instant.now().minusSeconds(1)));
        assertTrue(emailUpdate.getTimestamp().isBefore(Instant.now().plusMillis(1)));
        assertEquals(email, emailUpdate.getEmail());
        assertEquals(identity, emailUpdate.getIdentity());
        assertEquals(36, emailUpdate.getCode().length());
    }
}