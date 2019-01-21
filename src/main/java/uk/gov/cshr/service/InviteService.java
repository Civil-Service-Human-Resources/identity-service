package uk.gov.cshr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.domain.factory.InviteFactory;
import uk.gov.cshr.repository.InviteRepository;

import java.util.Date;
import java.util.Set;

@Service
@Transactional
public class InviteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InviteService.class);

    private final NotifyService notifyService;
    private final InviteRepository inviteRepository;
    private final InviteFactory inviteFactory;
    private final int validityInSeconds;

    public InviteService(NotifyService notifyService, InviteRepository inviteRepository, InviteFactory inviteFactory,
                         @Value("${notifications.invite.validityInSeconds}") int validityInSeconds) {
        this.notifyService = notifyService;
        this.inviteRepository = inviteRepository;
        this.inviteFactory = inviteFactory;
        this.validityInSeconds = validityInSeconds;
    }

    @ReadOnlyProperty
    public Invite findByCode(String code) {
        return inviteRepository.findByCode(code);
    }

    public boolean isCodeExpired(String code) {
        Invite invite = inviteRepository.findByCode(code);
        long diffInMs = new Date().getTime() - invite.getInvitedAt().getTime();

        if (diffInMs > validityInSeconds * 1000 && invite.getStatus().equals(InviteStatus.PENDING)) {
            updateInviteByCode(code, InviteStatus.ACCEPTED);
            return true;
        }

        updateInviteByCode(code, InviteStatus.EXPIRED);
        return false;
    }

    public void updateInviteByCode(String code, InviteStatus newStatus) {
        Invite invite = inviteRepository.findByCode(code);
        invite.setStatus(newStatus);
        inviteRepository.save(invite);
    }

    public void createNewInviteForEmailAndRoles(String email, Set<Role> roleSet, Identity inviter) {
        Invite invite = inviteFactory.create(email, roleSet, inviter);

        notifyService.sendInviteVerification(invite.getForEmail(), invite.getCode());

        inviteRepository.save(invite);
    }
}