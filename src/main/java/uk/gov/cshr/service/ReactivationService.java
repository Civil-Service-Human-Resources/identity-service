package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;

import java.util.Optional;

@Slf4j
@Service
public class ReactivationService {

    private IdentityRepository identityRepository;

    public ReactivationService(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    public void processReactivation(String uid) {
        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
        if(optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();
            identity.setRecentlyReactivated(false);
            identityRepository.save(identity);
        } else {
            log.info("No identity found for uid {}", uid);
            throw new ResourceNotFoundException();
        }
    }
}
