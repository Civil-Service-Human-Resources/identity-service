package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.domain.ReactivationStatus;
import uk.gov.cshr.exception.IdentityNotFoundException;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.ReactivationRepository;
import uk.gov.cshr.service.security.IdentityService;

import java.util.Date;

@Slf4j
@Service
public class ReactivationService {

    private ReactivationRepository reactivationRepository;
    private IdentityService identityService;

    public ReactivationService(ReactivationRepository reactivationRepository,
                               IdentityService identityService) {
        this.reactivationRepository = reactivationRepository;
        this.identityService = identityService;
    }

    public Reactivation getReactivationByCodeAndStatus(String code, ReactivationStatus reactivationStatus) {
        return reactivationRepository
                .findFirstByCodeAndReactivationStatusEquals(code, reactivationStatus)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public boolean existsByCodeAndStatus(String code, ReactivationStatus reactivationStatus) {
        return reactivationRepository
                .existsByCodeAndReactivationStatusEquals(code, reactivationStatus);
    }

    public boolean pendingExistsByEmail(String email){
        return reactivationRepository
                .existsByEmailAndReactivationStatusEquals(email, ReactivationStatus.PENDING);
    }

    public void reactivateIdentity(Reactivation reactivation) {
        reactivateIdentity(reactivation, null);
    }

    public void reactivateIdentity(Reactivation reactivation, AgencyToken agencyToken) throws IdentityNotFoundException {
        Identity identity = identityService.getIdentityByEmailAndActiveFalse(reactivation.getEmail());
        identityService.reactivateIdentity(identity, agencyToken);

        reactivation.setReactivationStatus(ReactivationStatus.REACTIVATED);
        reactivation.setReactivatedAt(new Date());

        log.debug("Identity reactivated for Reactivation: {}", reactivation);
        reactivationRepository.save(reactivation);
    }

    public Reactivation saveReactivation(Reactivation reactivation){
        return reactivationRepository.save(reactivation);
    }

    public Reactivation saveReactivation(String email){
        String reactivationCode = RandomStringUtils.random(40, true, true);

        Reactivation reactivation = new Reactivation(reactivationCode, ReactivationStatus.PENDING, new Date(), email);
        return saveReactivation(reactivation);
    }
}
