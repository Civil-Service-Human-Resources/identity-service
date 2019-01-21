package uk.gov.cshr.domain.factory;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Component;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.Role;

import java.util.Date;
import java.util.Set;

@Component
public class InviteFactory {
    public Invite create(String email, Set<Role> roleSet, Identity inviter) {
        Invite invite = new Invite();
        invite.setForEmail(email);
        invite.setForRoles(roleSet);
        invite.setInviter(inviter);
        invite.setInvitedAt(new Date());
        invite.setStatus(InviteStatus.PENDING);
        invite.setCode(RandomStringUtils.random(40, true, true));

        return invite;
    }

}
