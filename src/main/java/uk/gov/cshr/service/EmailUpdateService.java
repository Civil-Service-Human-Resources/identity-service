package uk.gov.cshr.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.EmailUpdateRepository;

@Service
@Transactional
public class EmailUpdateService {

    private final EmailUpdateRepository emailUpdateRepository;
    private final EmailUpdateFactory emailUpdateFactory;
    private final NotifyService notifyService;

    public EmailUpdateService(EmailUpdateRepository emailUpdateRepository, EmailUpdateFactory emailUpdateFactory, NotifyService notifyService) {
        this.emailUpdateRepository = emailUpdateRepository;
        this.emailUpdateFactory = emailUpdateFactory;
        this.notifyService = notifyService;
    }

    public String saveEmailUpdateAndNotify(Identity identity, String email) {
        EmailUpdate emailUpdate = emailUpdateFactory.create(identity, email);
        emailUpdateRepository.save(emailUpdate);
        notifyService.sendEmailUpdateVerification(identity.getEmail(), emailUpdate.getCode());

        return emailUpdate.getCode();
    }
}
