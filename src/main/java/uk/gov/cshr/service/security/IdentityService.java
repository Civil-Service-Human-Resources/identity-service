package uk.gov.cshr.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.TokenRepository;
import uk.gov.cshr.service.CSRSService;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.MessageService;
import uk.gov.cshr.service.NotifyService;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@Transactional
public class IdentityService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityService.class);

    private final String updatePasswordEmailTemplateId;

    private final IdentityRepository identityRepository;

    private InviteService inviteService;

    private final PasswordEncoder passwordEncoder;

    private final TokenServices tokenServices;

    private final TokenRepository tokenRepository;

    private final NotifyService notifyService;

    private final LearnerRecordService learnerRecordService;

    private final CSRSService csrsService;

    private final NotificationService notificationService;

    private final MessageService messageService;

    public IdentityService(@Value("${govNotify.template.passwordUpdate}") String updatePasswordEmailTemplateId,
                           IdentityRepository identityRepository,
                           PasswordEncoder passwordEncoder,
                           TokenServices tokenServices,
                           TokenRepository tokenRepository,
                           NotifyService notifyService,
                           LearnerRecordService learnerRecordService,
                           CSRSService csrsService,
                           NotificationService notificationService,
                           MessageService messageService) {
        this.updatePasswordEmailTemplateId = updatePasswordEmailTemplateId;
        this.identityRepository = identityRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenServices = tokenServices;
        this.tokenRepository = tokenRepository;
        this.notifyService = notifyService;
        this.learnerRecordService = learnerRecordService;
        this.csrsService = csrsService;
        this.notificationService = notificationService;
        this.messageService = messageService;
    }

    @Autowired
    public void setInviteService(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Identity identity = identityRepository.findFirstByActiveTrueAndEmailEquals(username);
        if (identity == null) {
            throw new UsernameNotFoundException("No user found with email address " + username);
        }
        return new IdentityDetails(identity);
    }

    @ReadOnlyProperty
    public boolean existsByEmail(String email) {
        return identityRepository.existsByEmail(email);
    }

    public void createIdentityFromInviteCode(String code, String password) {
        Invite invite = inviteService.findByCode(code);

        Set<Role> newRoles = new HashSet<>(invite.getForRoles());
        Identity identity = new Identity(UUID.randomUUID().toString(), invite.getForEmail(), passwordEncoder.encode(password), true, false, newRoles, Instant.now());
        identityRepository.save(identity);

        LOGGER.info("New identity {} successfully created", identity.getEmail());
    }

    public void updatePassword(Identity identity, String password) {
        identity.setPassword(passwordEncoder.encode(password));
        identityRepository.save(identity);
    }

    public void lockIdentity(String email) {
        Identity identity = identityRepository.findFirstByActiveTrueAndEmailEquals(email);
        identity.setLocked(true);
        identityRepository.save(identity);
    }

    public boolean checkPassword(String username, String password) {
        UserDetails userDetails = loadUserByUsername(username);
        return passwordEncoder.matches(password, userDetails.getPassword());
    }

    public void updatePasswordAndRevokeTokens(Identity identity, String password) {
        identity.setPassword(passwordEncoder.encode(password));
        identityRepository.save(identity);
        revokeAccessTokens(identity);
        notifyService.notify(identity.getEmail(), updatePasswordEmailTemplateId );
    }

    public void revokeAccessTokens(Identity identity) {
        tokenRepository.findAllByUserName(identity.getUid())
                .forEach(token -> tokenServices.revokeToken(token.getToken().getValue()));
    }

    public Identity setLastLoggedIn(Instant datetime, Identity identity) {
        identity.setLastLoggedIn(datetime);
        return identityRepository.save(identity);
    }

    @Transactional
    public void deleteIdentity(String uid) {
        ResponseEntity response = learnerRecordService.deleteCivilServant(uid);

        if(response.getStatusCode() == HttpStatus.NO_CONTENT) {
            response = csrsService.deleteCivilServant(uid);

            if(response.getStatusCode() == HttpStatus.NO_CONTENT) {
                Optional<Identity> result = identityRepository.findFirstByUid(uid);

                if (result.isPresent()) {
                    Identity identity = result.get();

                    inviteService.deleteInvitesByIdentity(identity);
                    identityRepository.delete(identity);
                }
            }
        }
    }

    @Transactional
    public void trackUserActivity() {
        Iterable<Identity> identities = identityRepository.findAll();

        LocalDateTime deactivationDate = LocalDateTime.now().minusMonths(13);
        LocalDateTime deletionDate = LocalDateTime.now().minusMonths(26);

        identities.forEach(identity -> {
            LocalDateTime lastLoggedIn = LocalDateTime.ofInstant(identity.getLastLoggedIn(), ZoneOffset.UTC);

            if (lastLoggedIn.isBefore(deletionDate)) {
                deleteIdentity(identity.getUid());
            } else if (identity.isActive() && lastLoggedIn.isBefore(deactivationDate)) {
                identity.setActive(false);
                identityRepository.save(identity);
                notificationService.send(messageService.createSuspensionMessage(identity));
            }
        });
    }
}
