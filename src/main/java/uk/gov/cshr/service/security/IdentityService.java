package uk.gov.cshr.service.security;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.*;
import uk.gov.cshr.dto.BatchProcessResponse;
import uk.gov.cshr.exception.*;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.TokenRepository;
import uk.gov.cshr.service.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class IdentityService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityService.class);

    private final String updatePasswordEmailTemplateId;

    private final IdentityRepository identityRepository;
    private final CompoundRoleRepository compoundRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenServices tokenServices;
    private final TokenRepository tokenRepository;
    private final NotifyService notifyService;
    private final CsrsService csrsService;
    private InviteService inviteService;
    private AgencyTokenCapacityService agencyTokenCapacityService;

    private ReactivationService reactivationService;

    public IdentityService(@Value("${govNotify.template.passwordUpdate}") String updatePasswordEmailTemplateId,
                           IdentityRepository identityRepository,
                           CompoundRoleRepository compoundRoleRepository, PasswordEncoder passwordEncoder,
                           TokenServices tokenServices,
                           @Qualifier("tokenRepository") TokenRepository tokenRepository,
                           @Qualifier("notifyServiceImpl") NotifyService notifyService,
                           CsrsService csrsService,
                           AgencyTokenCapacityService agencyTokenCapacityService,
                           @Lazy ReactivationService reactivationService) {
        this.updatePasswordEmailTemplateId = updatePasswordEmailTemplateId;
        this.identityRepository = identityRepository;
        this.compoundRoleRepository = compoundRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenServices = tokenServices;
        this.tokenRepository = tokenRepository;
        this.notifyService = notifyService;
        this.csrsService = csrsService;
        this.agencyTokenCapacityService = agencyTokenCapacityService;
        this.reactivationService = reactivationService;
    }

    @Autowired
    public void setInviteService(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Identity identity = identityRepository.findFirstByEmailEquals(username);
        if (identity == null) {
            throw new UsernameNotFoundException("No user found with email address " + username);
        } else if (!identity.isActive()) {
            boolean pendingReactivationExistsForAccount = reactivationService.pendingExistsByEmail(identity.getEmail());

            if(pendingReactivationExistsForAccount){
                throw new PendingReactivationExistsException("Pending reactivation already exists for user");
            }
            throw new AccountDeactivatedException("User account is deactivated");
        }
        return new IdentityDetails(identity);
    }

    @ReadOnlyProperty
    public boolean existsByEmail(String email) {
        return identityRepository.existsByEmail(email);
    }

    @Transactional(noRollbackFor = {UnableToAllocateAgencyTokenException.class, ResourceNotFoundException.class})
    public void createIdentityFromInviteCode(String code, String password, TokenRequest tokenRequest) {
        Invite invite = inviteService.findByCode(code);
        final String domain = getDomainFromEmailAddress(invite.getForEmail());

        Set<Role> newRoles = new HashSet<>(invite.getForRoles());

        String agencyTokenUid = null;
        if (requestHasTokenData(tokenRequest)) {
            Optional<AgencyToken> agencyTokenForDomainTokenOrganisation = csrsService.getAgencyTokenForDomainTokenOrganisation(tokenRequest.getDomain(), tokenRequest.getToken(), tokenRequest.getOrg());

            agencyTokenUid = agencyTokenForDomainTokenOrganisation
                    .map(agencyToken -> {
                        if (agencyTokenCapacityService.hasSpaceAvailable(agencyToken)) {
                            return agencyToken.getUid();
                        } else {
                            throw new UnableToAllocateAgencyTokenException("Agency token uid " + agencyToken.getUid() + " has no spaces available. Identity not created");
                        }
                    })
                    .orElseThrow(ResourceNotFoundException::new);

            log.info("Identity request has agency uid = {}", agencyTokenUid);
        } else if (!isAllowlistedDomain(domain) && !isEmailInvitedViaIDM(invite.getForEmail())) {
            log.info("Invited request neither agency, nor allowlisted, nor invited via IDM: {}", invite);
            throw new ResourceNotFoundException();
        }

        Identity identity = new Identity(UUID.randomUUID().toString(),
                invite.getForEmail(),
                passwordEncoder.encode(password),
                true,
                false,
                newRoles,
                Instant.now(),
                false,
                false,
                agencyTokenUid);

        identityRepository.save(identity);

        LOGGER.debug("New identity email = {} successfully created", identity.getEmail());
    }

    public void updatePassword(Identity identity, String password) {
        identity.setActive(true);
        identity.setDeletionNotificationSent(false);
        identity.setPassword(passwordEncoder.encode(password));
        identity.setLocked(false);
        identityRepository.save(identity);
    }

    public void lockIdentity(String email) {
        Identity identity = identityRepository.findFirstByActiveTrueAndEmailEquals(email);
        identity.setLocked(true);
        identityRepository.save(identity);
    }

    public void reactivateIdentity(Identity identity, AgencyToken agencyToken) {
        identity.setActive(true);

        if (agencyToken != null && agencyToken.getUid() != null) {
            identity.setAgencyTokenUid(agencyToken.getUid());
        }
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

    public boolean checkEmailExists(String email) {
        return identityRepository.existsByEmail(email);
    }


    public Identity setLastLoggedIn(Instant datetime, Identity identity) {
        identity.setLastLoggedIn(datetime);
        return identityRepository.save(identity);
    }

    public BatchProcessResponse removeRoles(List<String> uids, CompoundRole compoundRole) {
        return removeRoles(uids, Collections.singletonList(compoundRole));
    }

    public BatchProcessResponse removeRoles(List<String> uids, List<CompoundRole> compoundRoles) {
        log.info(String.format("Removing %s access from the following users: %s", compoundRoles, uids));
        BatchProcessResponse response = new BatchProcessResponse();
        List<Identity> identities = identityRepository.findIdentitiesByUids(uids);
        Collection<String> reportingRoles = compoundRoles.stream().flatMap(cr -> compoundRoleRepository.getRoles(cr).stream()).collect(Collectors.toList());
        List<Identity> identitiesToSave = new ArrayList<>();
        identities.forEach(i -> {
            if (i.hasAnyRole(reportingRoles)) {
                i.removeRoles(reportingRoles);
                identitiesToSave.add(i);
            }
        });
        if (!identitiesToSave.isEmpty()) {
            log.info(String.format("%s access removed from the following users: %s", compoundRoles, uids));
            identityRepository.saveAll(identitiesToSave);
            response.setSuccessfulIds(identitiesToSave.stream().map(Identity::getUid).collect(Collectors.toList()));
        }
        return response;
    }

    public BatchProcessResponse removeReportingRoles(List<String> uids) {
        return removeRoles(uids, CompoundRole.REPORTER);
    }

    public void updateEmailAddress(Identity identity, String email, AgencyToken newAgencyToken) {
        if (newAgencyToken != null && newAgencyToken.getUid() != null) {
            log.debug("Updating agency token for user: oldAgencyToken = {}, newAgencyToken = {}", identity.getAgencyTokenUid(), newAgencyToken.getUid());
            identity.setAgencyTokenUid(newAgencyToken.getUid());
        } else {
            log.debug("Setting existing agency token UID to null");
            identity.setAgencyTokenUid(null);
        }
        identity.setEmail(email);
        identity.removeRoles(compoundRoleRepository.getRoles(CompoundRole.REPORTER));
        identity.removeRoles(compoundRoleRepository.getRoles(CompoundRole.UNRESTRICTED_ORGANISATION));
        identityRepository.save(identity);
    }

    public String getDomainFromEmailAddress(String emailAddress) {
        return emailAddress.substring(emailAddress.indexOf('@') + 1);
    }

    public boolean checkValidEmail(String email) {
        final String domain = getDomainFromEmailAddress(email);
        return (isAllowlistedDomain(domain) || csrsService.isDomainInAgency(domain));
    }

    private boolean requestHasTokenData(TokenRequest tokenRequest) {
        return hasData(tokenRequest.getDomain())
                && hasData(tokenRequest.getToken())
                && hasData(tokenRequest.getOrg());
    }

    private boolean hasData(String s) {
        return s != null && s.length() > 0;
    }

    public Identity getIdentityByEmail(String email) {
        return identityRepository.findFirstByEmailEquals(email);
    }

    public Identity getIdentityByEmailAndActiveFalse(String email) {
        return identityRepository.findFirstByActiveFalseAndEmailEquals(email).orElseThrow(() -> new IdentityNotFoundException("Identity not found for email: " + email));
    }

    private boolean isEmailInvitedViaIDM(String email) {
        return inviteService.isEmailInvited(email);
    }

    public boolean isAllowlistedDomain(String domain) {
        return csrsService.getAllowlist().contains(domain.toLowerCase());
    }
}
