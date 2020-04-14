package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.security.IdentityService;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Slf4j
@Service
public class ReactivationService {

    private IdentityRepository identityRepository;

    private IdentityService identityService;

    public ReactivationService(IdentityRepository identityRepository, IdentityService identityService) {
        this.identityRepository = identityRepository;
        this.identityService = identityService;
    }

    public void processReactivation(HttpServletRequest request, String uid) {
        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
        if(optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();
            // update identity in the db
            identity.setRecentlyReactivated(false);
            Identity updatedIdentity = identityRepository.save(identity);
            // update spring
            identityService.updateSpringWithRecentlyReactivatedFlag(request, false);
        } else {
            log.info("No identity found for uid {}", uid);
            throw new ResourceNotFoundException();
        }
    }

}
