package uk.gov.cshr.controller.account.reactivation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.domain.ReactivationStatus;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.service.*;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.ApplicationConstants;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Controller
@RequestMapping("/account/reactivate")
public class ReactivationController {

    private static final String ACCOUNT_REACTIVATED_TEMPLATE = "account/accountReactivated";

    private static final String REDIRECT_ACCOUNT_REACTIVATED = "redirect:/account/reactivate/updated";

    private static final String REDIRECT_ACCOUNT_REACTIVATE_AGENCY = "redirect:/account/verify/agency/";

    private static final String REDIRECT_LOGIN = "redirect:/login";

    private static final String LPG_UI_URL_ATTRIBUTE = "lpgUiUrl";

    private final ReactivationService reactivationService;

    private final IdentityService identityService;

    private final AgencyTokenService agencyTokenService;

    private NotifyService notifyService;

    private final String lpgUiUrl;

    @Value("${reactivation.emailTemplateId}")
    private String reactivationEmailTemplateId;

    @Value("${reactivation.reactivationUrl}")
    private String reactivationBaseUrl;

    @Value("${textEncryption.encryptionKey}")
    private String encryptionKey;

    public ReactivationController(ReactivationService reactivationService,
                                  IdentityService identityService,
                                  AgencyTokenService agencyTokenService,
                                  NotifyService notifyService,
                                  @Value("${lpg.uiUrl}") String lpgUiUrl) {
        this.reactivationService = reactivationService;
        this.identityService = identityService;
        this.agencyTokenService = agencyTokenService;
        this.notifyService = notifyService;
        this.lpgUiUrl = lpgUiUrl;
    }

    @GetMapping
    public String sendReactivationEmail(@RequestParam String code){

        try {
            String email = getDecryptedTextFromCode(code);
            Reactivation reactivation = reactivationService.saveReactivation(email);
            notifyUserByEmail(reactivation);
            return "reactivate";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{code}")
    public String reactivateAccount(
            @PathVariable(value = "code") String code,
            RedirectAttributes redirectAttributes) {
        try {
            Reactivation reactivation = reactivationService.getReactivationByCodeAndStatus(code, ReactivationStatus.PENDING);

            if(reactivationRequestHasExpired(reactivation)){
                log.debug("Reactivation with code {} has expired.", reactivation.getCode());
                return "redirect:/login?error=deactivated-expired&username=" + URLEncoder.encode(reactivation.getEmail(), "UTF-8");
            }

            String domain = identityService.getDomainFromEmailAddress(reactivation.getEmail());

            log.debug("Reactivating account using Reactivation: {}", reactivation);

            if (isDomainInAgency(domain)) {
                log.info("Account reactivation is agency, requires token validation for Reactivation: {}", reactivation);
                return REDIRECT_ACCOUNT_REACTIVATE_AGENCY + code;
            } else {
                log.info("Account reactivation is not agency and can reactivate without further validation for Reactivation: {}", reactivation);
                reactivationService.reactivateIdentity(reactivation);
                return REDIRECT_ACCOUNT_REACTIVATED;
            }
        } catch (ResourceNotFoundException e) {
            log.error("ResourceNotFoundException for code: {}, with status {}", ReactivationStatus.PENDING);
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.REACTIVATION_CODE_IS_NOT_VALID_ERROR_MESSAGE);
            return REDIRECT_LOGIN;
        } catch (Exception e) {
            log.error("Unexpected error for code: {}, with cause {}", code, e.getCause());
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.ACCOUNT_REACTIVATION_ERROR_MESSAGE);
            return REDIRECT_LOGIN;
        }
    }

    @GetMapping("/updated")
    public String emailUpdated(Model model) {
        log.info("Account reactivation complete");
        model.addAttribute(LPG_UI_URL_ATTRIBUTE, lpgUiUrl + "/login");

        return ACCOUNT_REACTIVATED_TEMPLATE;
    }

    private boolean isDomainInAgency(String newDomain) {
        return agencyTokenService.isDomainInAgencyToken(newDomain);
    }

    private String getDecryptedTextFromCode(String code) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Key aesKey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);

        byte[] plainText = cipher.doFinal(Base64.getDecoder()
                .decode(code));

        String decryptedText = new String(plainText);
        return decryptedText;
    }
    private void notifyUserByEmail(Reactivation reactivation){
        String learnerName = reactivation.getEmail();

        Map<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("learnerName", learnerName);
        emailPersonalisation.put("reactivationUrl", reactivationBaseUrl + reactivation.getCode());

        notifyService.notifyWithPersonalisation(reactivation.getEmail(), reactivationEmailTemplateId, emailPersonalisation);
    }

    private boolean reactivationRequestHasExpired(Reactivation reactivation){
        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
        return reactivation.getReactivationStatus() == ReactivationStatus.PENDING
            && reactivation.getRequestedAt().toInstant().isBefore(oneDayAgo);
    }
}
