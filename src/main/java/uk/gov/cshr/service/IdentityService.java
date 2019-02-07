package uk.gov.cshr.service;

import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.IdentityRoleRepository;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;

import java.util.Optional;

@Service
public class IdentityService {

    private final LearnerRecordService learnerRecordService;

    private final CSRSService csrsService;

    private final IdentityRepository identityRepository;

    private final IdentityRoleRepository identityRoleRepository;

    public IdentityService(LearnerRecordService learnerRecordService, CSRSService csrsService, IdentityRepository identityRepository, IdentityRoleRepository identityRoleRepository) {
        this.learnerRecordService = learnerRecordService;
        this.csrsService = csrsService;
        this.identityRepository = identityRepository;
        this.identityRoleRepository = identityRoleRepository;
    }

    public void deleteIdentity(String uid) {
        learnerRecordService.deleteCivilServant(uid);
        csrsService.deleteCivilServant(uid);

        Optional<Identity> identity = identityRepository.findFirstByUid(uid);

        if(identity.isPresent()) {
            identityRoleRepository.deleteByIdentity(identity.get());
            identityRepository.delete(identity.get());
        }
    }
}
