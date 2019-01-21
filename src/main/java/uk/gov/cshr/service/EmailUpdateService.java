package uk.gov.cshr.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.InvalidCodeException;
import uk.gov.cshr.repository.EmailUpdateRepository;
import uk.gov.cshr.service.security.IdentityService;

@Service
@Transactional
public class EmailUpdateService {

    private final EmailUpdateRepository emailUpdateRepository;
    private final EmailUpdateFactory emailUpdateFactory;
    private final NotifyService notifyService;
    private final IdentityService identityService;

    public EmailUpdateService(EmailUpdateRepository emailUpdateRepository, EmailUpdateFactory emailUpdateFactory, NotifyService notifyService, IdentityService identityService) {
        this.emailUpdateRepository = emailUpdateRepository;
        this.emailUpdateFactory = emailUpdateFactory;
        this.notifyService = notifyService;
        this.identityService = identityService;
    }

    public String saveEmailUpdateAndNotify(Identity identity, String email) {
        EmailUpdate emailUpdate = emailUpdateFactory.create(identity, email);
        emailUpdateRepository.save(emailUpdate);
        notifyService.sendEmailUpdateVerification(email, emailUpdate.getCode());

        return emailUpdate.getCode();
    }

    public void updateEmailAddress(Identity identity, String code) {
        EmailUpdate emailUpdate = emailUpdateRepository.findByIdentityAndCode(identity, code)
                .orElseThrow(() -> new InvalidCodeException(String.format("Code %s does not exist for identity %s", code, identity)));

        identityService.updateEmailAddress(identity, emailUpdate.getEmail());

        emailUpdateRepository.delete(emailUpdate);
    }

    public boolean verifyCode(Identity identity, String code) {
        return emailUpdateRepository.findByIdentityAndCode(identity, code).isPresent();
    }
}
