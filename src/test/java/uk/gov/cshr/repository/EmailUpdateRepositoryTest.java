package uk.gov.cshr.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Role;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class EmailUpdateRepositoryTest {

    @Autowired
    private EmailUpdateRepository emailUpdateRepository;

    @Autowired
    private IdentityRepository identityRepository;

    @Test
    public void shouldFindEmailUpdateByCode() {
        String uid = UUID.randomUUID().toString();
        String email = "tester@domain.com";
        String updatedEmail = "updated@domain.com";
        String password = "_password";
        boolean active = true;
        boolean locked = false;
        Instant timestamp = Instant.now();

        Role role = new Role("TEST", "for testing");
        Identity identity = new Identity(uid, email, password, active, locked, new HashSet<>(Collections.singletonList(role)));

        identityRepository.save(identity);

        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setEmail(updatedEmail);
        emailUpdate.setIdentity(identity);
        emailUpdate.setTimestamp(timestamp);

        emailUpdateRepository.save(emailUpdate);

        EmailUpdate saved = emailUpdateRepository.findByCode(emailUpdate.getCode()).orElseThrow(
                () -> new RuntimeException("EmailUpdate not found")
        );

        assertEquals(updatedEmail, saved.getEmail());
        assertEquals(identity, saved.getIdentity());
    }
}