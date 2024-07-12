package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Qualifier;
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
import uk.gov.service.notify.NotificationClientException;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class InviteService {
    private final String govNotifyInviteTemplateId;
    private final int validityInSeconds;
    private final String signupUrlFormat;
    private final NotifyService notifyService;
    private final InviteRepository inviteRepository;
    private final InviteFactory inviteFactory;

    public InviteService(
            @Value("${govNotify.template.invite}") String govNotifyInviteTemplateId,
            @Value("${invite.validityInSeconds}") int validityInSeconds,
            @Value("${invite.url}") String signupUrlFormat,
            @Qualifier("notifyServiceImpl") NotifyService notifyService,
            @Qualifier("inviteRepository") InviteRepository inviteRepository,
            InviteFactory inviteFactory) {
        this.govNotifyInviteTemplateId = govNotifyInviteTemplateId;
        this.validityInSeconds = validityInSeconds;
        this.signupUrlFormat = signupUrlFormat;
        this.notifyService = notifyService;
        this.inviteRepository = inviteRepository;
        this.inviteFactory = inviteFactory;
    }

    @ReadOnlyProperty
    public Invite findByCode(String code) {
        return inviteRepository.findByCode(code);
    }

    @ReadOnlyProperty
    public Optional<Invite> findByForEmailAndStatus(String email, InviteStatus status) {
        return inviteRepository.findByForEmailAndStatus(email, status);
    }

    public boolean isInviteCodeExpired(Invite invite) {
        long diffInMs = new Date().getTime() - invite.getInvitedAt().getTime();
        return diffInMs > validityInSeconds * 1000L;
    }

    public void updateInviteByCode(String code, InviteStatus newStatus) {
        Invite invite = inviteRepository.findByCode(code);
        invite.setStatus(newStatus);
        inviteRepository.save(invite);
    }

    public void createNewInviteForEmailAndRoles(String email, Set<Role> roleSet, Identity inviter)
            throws NotificationClientException {
        Invite invite = inviteFactory.create(email, roleSet, inviter);

        notifyService.notify(invite.getForEmail(), invite.getCode(), govNotifyInviteTemplateId, signupUrlFormat);

        inviteRepository.save(invite);
    }

    public void sendSelfSignupInvite(String email, boolean isAuthorisedInvite) throws NotificationClientException {
        Invite invite = inviteFactory.createSelfSignUpInvite(email);
        invite.setAuthorisedInvite(isAuthorisedInvite);

        notifyService.notify(invite.getForEmail(), invite.getCode(), govNotifyInviteTemplateId, signupUrlFormat);

        inviteRepository.save(invite);
    }

    public Invite fetchValidInviteWithCode(String code) {
        Invite invite = inviteRepository.findByCode(code);
        if (invite != null && isInviteCodeExpired(invite)) {
            return null;
        }
        return invite;
    }

    public boolean isEmailInvited(String email) {
        return inviteRepository.existsByForEmailAndInviterIdIsNotNull(email);
    }

    public void authoriseAndSave(Invite invite) {
        invite.setAuthorisedInvite(true);
        inviteRepository.save(invite);
    }
}
