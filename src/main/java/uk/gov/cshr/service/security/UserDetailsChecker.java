package uk.gov.cshr.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.AccountBlockedException;
import uk.gov.cshr.exception.InvalidUserDetailsType;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.csrs.CsrsService;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDetailsChecker extends AccountStatusUserDetailsChecker {

    private final CsrsService csrsService;
    private final InviteService inviteService;

    @Override
    public void check(UserDetails user) {
        super.check(user);

        if (user instanceof IdentityDetails) {
            IdentityDetails userDetails = (IdentityDetails) user;
            Identity identity = userDetails.getIdentity();

            if (!isUserValid(identity)) {
                throw new AccountBlockedException(messages.getMessage("UserDetailsChecker.blocked", "User account is blocked"));
            }

        } else {
            throw new InvalidUserDetailsType("Wrong user type received");
        }
    }

    private boolean isUserValid(Identity identity) {
        String email = identity.getEmail();
        String domain = identity.getDomain();
        String agencyTokenUid = identity.getAgencyTokenUid();
        if (inviteService.isEmailInvited(email)) {
            log.debug(String.format("User %s has a valid invite from another user", identity.getId()));
            return true;
        }
        if (agencyTokenUid != null) {
            log.debug(String.format("Checking domain %s against agency token %s for user %s", domain, agencyTokenUid, identity.getId()));
            return csrsService.isAgencyTokenUidValidForDomain(agencyTokenUid, domain);
        }

        log.debug(String.format("Checking domain %s against allowlist for user %s", domain, identity.getId()));
        return csrsService.isDomainAllowlisted(domain);

    }
}
