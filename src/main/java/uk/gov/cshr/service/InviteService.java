package uk.gov.cshr.service;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.service.notify.NotificationClientException;

import java.util.Date;
import java.util.Set;

@Service
@Transactional
public class InviteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InviteService.class);

    @Autowired
    private NotifyService notifyService;

    private InviteRepository inviteRepository;

    @Value("${govNotify.template.invite}")
    private String govNotifyInviteTemplateId;

    @Value("${invite.validityInSeconds}")
    private int validityInSeconds;

    @Value("${invite.url}")
    private String signupUrlFormat;

    @Autowired
    public InviteService(InviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
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

    public void createNewInviteForEmailAndRoles(String email, Set<Role> roleSet, Identity inviter) throws NotificationClientException {
        Invite invite = new Invite();
        invite.setForEmail(email);
        invite.setForRoles(roleSet);
        invite.setInviter(inviter);
        invite.setInvitedAt(new Date());
        invite.setStatus(InviteStatus.PENDING);
        invite.setCode(RandomStringUtils.random(40, true, true));

        notifyService.sendInviteVerification(invite.getForEmail(), invite.getCode(), govNotifyInviteTemplateId, signupUrlFormat);

        inviteRepository.save(invite);
    }
}