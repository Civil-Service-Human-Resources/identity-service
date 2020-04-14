package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.utils.SpringUserUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Slf4j
@Service
public class ReactivationService {

    private IdentityRepository identityRepository;

    private SpringUserUtils springUserUtils;

    public ReactivationService(IdentityRepository identityRepository, SpringUserUtils springUserUtils) {
        this.identityRepository = identityRepository;
        this.springUserUtils = springUserUtils;
    }

    public void processReactivation(HttpServletRequest request, String uid) {
        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
        if(optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();
            // update identity in the db
            identity.setRecentlyReactivated(false);
            Identity updatedIdentity = identityRepository.save(identity);
            // update spring authentication and spring session
            Identity identityFromSpringAuth = springUserUtils.getIdentityFromSpringAuthentication();
            identityFromSpringAuth.setRecentlyReactivated(false);
            springUserUtils.updateSpringAuthenticationAndSpringSessionWithUpdatedIdentity(request, identityFromSpringAuth);
            log.info("updated identity in db - isRecentlyReactivated flag=" + updatedIdentity.isRecentlyReactivated());
            log.info("updated identity in spring - isRecentlyReactivated flag=" + springUserUtils.getIdentityFromSpringAuthentication().isRecentlyReactivated());
        } else {
            log.info("No identity found for uid {}", uid);
            throw new ResourceNotFoundException();
        }
    }

}
