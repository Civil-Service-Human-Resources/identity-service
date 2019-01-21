package uk.gov.cshr.domain.factory;

import org.junit.Test;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.Role;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class InviteFactoryTest {
    private final InviteFactory inviteFactory = new InviteFactory();

    @Test
    public void shouldCreateInvite() {
        String email = "learner@domain.com";
        Set<Role> roleSet = new HashSet<>(Collections.singletonList(new Role()));
        Identity inviter = mock(Identity.class);

        Invite invite = inviteFactory.create(email, roleSet, inviter);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        assertEquals(email, invite.getForEmail());
        assertEquals(roleSet, invite.getForRoles());
        assertEquals(inviter, invite.getInviter());
        assertEquals(dateFormat.format(new Date()), dateFormat.format(invite.getInvitedAt()));
        assertEquals(InviteStatus.PENDING, invite.getStatus());
        assertNotNull(invite.getCode());
        assertEquals(40, invite.getCode().length());
    }
}