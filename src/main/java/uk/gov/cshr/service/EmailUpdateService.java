package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.InvalidCodeException;
import uk.gov.cshr.repository.EmailUpdateRepository;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.SpringUserUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class EmailUpdateService {

    private final EmailUpdateRepository emailUpdateRepository;
    private final EmailUpdateFactory emailUpdateFactory;
    private final NotifyService notifyService;
    private final IdentityService identityService;
    private final CsrsService csrsService;
    private final SpringUserUtils springUserUtils;
    private final String updateEmailTemplateId;
    private final String inviteUrlFormat;

    public EmailUpdateService(EmailUpdateRepository emailUpdateRepository,
                              EmailUpdateFactory emailUpdateFactory,
                              @Qualifier("notifyServiceImpl") NotifyService notifyService,
                              IdentityService identityService,
                              CsrsService csrsService,
                              SpringUserUtils springUserUtils,
                              @Value("${govNotify.template.emailUpdate}") String updateEmailTemplateId,
                              @Value("${emailUpdate.urlFormat}") String inviteUrlFormat) {
        this.emailUpdateRepository = emailUpdateRepository;
        this.emailUpdateFactory = emailUpdateFactory;
        this.notifyService = notifyService;
        this.identityService = identityService;
        this.csrsService = csrsService;
        this.springUserUtils = springUserUtils;
        this.updateEmailTemplateId = updateEmailTemplateId;
        this.inviteUrlFormat = inviteUrlFormat;
    }

    public String saveEmailUpdateAndNotify(Identity identity, String email) {
        EmailUpdate emailUpdate = emailUpdateFactory.create(identity, email);
        emailUpdateRepository.save(emailUpdate);

        String activationUrl = String.format(inviteUrlFormat, emailUpdate.getCode());
        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("activationUrl", activationUrl);

        notifyService.notifyWithPersonalisation(email, updateEmailTemplateId, personalisation);

        return emailUpdate.getCode();
    }

    public boolean verifyCode(Identity identity, String code) {
        return emailUpdateRepository.findByIdentityAndCode(identity, code).isPresent();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateEmailAddress(HttpServletRequest request, Identity identity, String code) {
        EmailUpdate emailUpdate = emailUpdateRepository.findByIdentityAndCode(identity, code)
                .orElseThrow(() -> new InvalidCodeException(String.format("Code %s does not exist for identity %s", code, identity)));

        log.info("updating email address on users identity");
        // update identity in the db
        identityService.updateEmailAddressAndEmailRecentlyUpdatedFlagToTrue(identity, emailUpdate.getEmail());
        // update spring
        updateSpringWithRecentlyReactivatedFlag(request, true);
        log.info("deleting the email update config for this user");
        emailUpdateRepository.delete(emailUpdate);
        log.info("all ok");
    }

    @Transactional(rollbackFor = Exception.class)
    public void processEmailUpdatedRecentlyRequestForWhiteListedDomainUser(HttpServletRequest request, Identity identity) {
        // update identity in the db
        identityService.resetRecentlyUpdatedEmailFlagToFalse(identity);
        // update spring
        updateSpringWithRecentlyReactivatedFlag(request, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void processEmailUpdatedRecentlyRequestForAgencyTokenUser(String newDomain, String newToken,
                                                                     String newOrgCode, Identity identity, HttpServletRequest request) {
        csrsService.updateSpacesAvailable(newDomain, newToken, newOrgCode, false);
        // update identity in the db
        identityService.resetRecentlyUpdatedEmailFlagToFalse(identity);
        // update spring
        updateSpringWithRecentlyReactivatedFlag(request, false);
    }

    private void updateSpringWithRecentlyReactivatedFlag(HttpServletRequest request, boolean reactivateFlag) {
        // update spring authentication and spring session
        Identity identityFromSpringAuth = springUserUtils.getIdentityFromSpringAuthentication();
        identityFromSpringAuth.setEmailRecentlyUpdated(reactivateFlag);
        springUserUtils.updateSpringAuthenticationAndSpringSessionWithUpdatedIdentity(request, identityFromSpringAuth);
    }

}
