package uk.gov.cshr.controller.signup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.domain.TokenRequest;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.exception.UnableToAllocateAgencyTokenException;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.service.AgencyTokenCapacityService;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.ApplicationConstants;
import uk.gov.service.notify.NotificationClientException;

import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping("/signup")
public class SignupController {

    private static final String ENTER_TOKEN_TEMPLATE = "enterToken";
    private static final String REQUEST_INVITE_TEMPLATE = "requestInvite";
    private static final String INVITE_SENT_TEMPLATE = "inviteSent";
    private static final String SIGNUP_TEMPLATE = "signup";
    private static final String SIGNUP_SUCCESS_TEMPLATE = "signupSuccess";

    private static final String INVITE_MODEL = "invite";
    private static final String ORGANISATIONS_ATTRIBUTE = "organisations";
    private static final String TOKEN_INFO_FLASH_ATTRIBUTE = "tokenRequest";
    private static final String REQUEST_INVITE_FORM = "requestInviteForm";
    private static final String SIGNUP_FORM = "signupForm";
    private static final String ENTER_TOKEN_FORM = "enterTokenForm";

    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_SIGNUP = "redirect:/signup/";
    private static final String REDIRECT_SIGNUP_REQUEST = "redirect:/signup/request";
    private static final String REDIRECT_ENTER_TOKEN = "redirect:/signup/enterToken/";

    private static final String LPG_UI_URL = "lpgUiUrl";

    private final InviteService inviteService;

    private final IdentityService identityService;

    private final CsrsService csrsService;

    private final InviteRepository inviteRepository;

    private final AgencyTokenCapacityService agencyTokenCapacityService;

    private final String lpgUiUrl;

    public SignupController(InviteService inviteService,
                            IdentityService identityService,
                            CsrsService csrsService,
                            InviteRepository inviteRepository,
                            AgencyTokenCapacityService agencyTokenCapacityService,
                            @Value("${lpg.uiUrl}") String lpgUiUrl) {
        this.inviteService = inviteService;
        this.identityService = identityService;
        this.csrsService = csrsService;
        this.inviteRepository = inviteRepository;
        this.agencyTokenCapacityService = agencyTokenCapacityService;
        this.lpgUiUrl = lpgUiUrl;
    }

    @GetMapping(path = "/request")
    public String requestInvite(Model model) {
        model.addAttribute(REQUEST_INVITE_FORM, new RequestInviteForm());
        return REQUEST_INVITE_TEMPLATE;
    }

    @PostMapping(path = "/request")
    public String sendInvite(Model model,
                             @ModelAttribute @Valid RequestInviteForm form,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) throws NotificationClientException {

        if (bindingResult.hasErrors()) {
            model.addAttribute(REQUEST_INVITE_FORM, form);
            return REQUEST_INVITE_TEMPLATE;
        }

        final String email = form.getEmail();

        if (inviteRepository.existsByForEmailAndStatus(email, InviteStatus.PENDING)) {
            log.info("{} has already been invited", email);
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, email + " has already been invited");
            return REDIRECT_SIGNUP_REQUEST;
        }

        if (identityService.existsByEmail(email)) {
            log.info("{} is already a user", email);
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, "User already exists with email address " + email);
            return REDIRECT_SIGNUP_REQUEST;
        }

        final String domain = identityService.getDomainFromEmailAddress(email);

        if (identityService.isWhitelistedDomain(domain)) {
            log.debug("Sending invite to whitelisted user {}", email);
            inviteService.sendSelfSignupInvite(email, true);
            return INVITE_SENT_TEMPLATE;
        } else {
            if (csrsService.isDomainInAgency(domain)) {
                log.debug("Sending invite to agency user {}", email);
                inviteService.sendSelfSignupInvite(email, false);
                return INVITE_SENT_TEMPLATE;
            } else {
                log.debug("The domain of user {} is neither Whitelisted nor part of an Agency token", email);
                redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, "Your organisation is unable to use this service. Please contact your line manager.");
                return REDIRECT_SIGNUP_REQUEST;
            }
        }
    }

    @GetMapping("/{code}")
    public String signup(Model model, @PathVariable(value = "code") String code) {
        if (inviteService.isInviteValid(code)) {
            Invite invite = inviteRepository.findByCode(code);

            if (!invite.isAuthorisedInvite()) {
                log.debug("Invite email = {} not yet authorised - redirecting to enter token screen", invite.getForEmail());
                return REDIRECT_ENTER_TOKEN + code;
            }

            model.addAttribute(INVITE_MODEL, invite);
            model.addAttribute(SIGNUP_FORM, new SignupForm());

            if(model.containsAttribute(TOKEN_INFO_FLASH_ATTRIBUTE)) {
                TokenRequest tokenRequest = (TokenRequest) model.asMap().get(TOKEN_INFO_FLASH_ATTRIBUTE);
                model.addAttribute(TOKEN_INFO_FLASH_ATTRIBUTE, tokenRequest);
            } else {
                model.addAttribute(TOKEN_INFO_FLASH_ATTRIBUTE, new TokenRequest());
            }

            log.debug("Invite email = {} valid and authorised - redirecting to set password screen", invite.getForEmail());
            return SIGNUP_TEMPLATE;
        } else {
            log.debug("Signup code for invite not valid - redirecting to login");
            return REDIRECT_LOGIN;
        }
    }

    @PostMapping("/{code}")
    @Transactional(noRollbackFor = {UnableToAllocateAgencyTokenException.class, ResourceNotFoundException.class})
    public String signup(@PathVariable(value = "code") String code,
                         @ModelAttribute @Valid SignupForm signupForm,
                         BindingResult signUpFormBindingResult,
                         @ModelAttribute TokenRequest tokenRequest,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (signUpFormBindingResult.hasErrors()) {
            model.addAttribute(INVITE_MODEL, inviteRepository.findByCode(code));
            return SIGNUP_TEMPLATE;
        }

        if (inviteService.isInviteValid(code)) {
            Invite invite = inviteRepository.findByCode(code);
            if (!invite.isAuthorisedInvite()) {
                return REDIRECT_ENTER_TOKEN + code;
            }

            log.debug("Invite and signup credentials valid - creating identity and updating invite to 'Accepted'");
            try {
                identityService.createIdentityFromInviteCode(code, signupForm.getPassword(), tokenRequest);
            } catch (UnableToAllocateAgencyTokenException e) {
                log.debug("UnableToAllocateAgencyTokenException. Redirecting to set password with no spaces error: " + e);

                model.addAttribute(INVITE_MODEL, invite);
                redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.SIGNUP_NO_SPACES_AVAILABLE_ERROR_MESSAGE);
                redirectAttributes.addFlashAttribute(TOKEN_INFO_FLASH_ATTRIBUTE, tokenRequest);
                return REDIRECT_SIGNUP + code;
            } catch (ResourceNotFoundException e) {
                log.debug("ResourceNotFoundException. Redirecting to set password with error: " + e);

                model.addAttribute(INVITE_MODEL, invite);
                redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.SIGNUP_RESOURCE_NOT_FOUND_ERROR_MESSAGE);
                redirectAttributes.addFlashAttribute(TOKEN_INFO_FLASH_ATTRIBUTE, tokenRequest);
                return REDIRECT_SIGNUP + code;
            }
            inviteService.updateInviteByCode(code, InviteStatus.ACCEPTED);

            // This provides the next template the URL for LPG-UI so a user can begin the login process
            model.addAttribute(LPG_UI_URL, lpgUiUrl);

            return SIGNUP_SUCCESS_TEMPLATE;
        } else {
            return REDIRECT_LOGIN;
        }
    }

    @GetMapping(path = "/enterToken/{code}")
    public String enterToken(Model model, @PathVariable(value = "code") String code) {
        if (inviteService.isInviteValid(code)) {
            Invite invite = inviteRepository.findByCode(code);
            if (invite.isAuthorisedInvite()) {
                return REDIRECT_SIGNUP + code;
            }

            log.debug("Invite email = {} accessing enter token screen for validation", invite.getForEmail());

            OrganisationalUnitDto[] organisations = csrsService.getOrganisationalUnitsFormatted();

            model.addAttribute(ORGANISATIONS_ATTRIBUTE, organisations);
            model.addAttribute(ENTER_TOKEN_FORM, new EnterTokenForm());

            return ENTER_TOKEN_TEMPLATE;
        } else {
            return REDIRECT_LOGIN;
        }
    }

    @PostMapping(path = "/enterToken/{code}")
    public String checkToken(Model model,
                             @PathVariable(value = "code") String code,
                             @ModelAttribute @Valid EnterTokenForm form,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(ENTER_TOKEN_FORM, form);
            return ENTER_TOKEN_TEMPLATE;
        }

        if (inviteService.isInviteValid(code)) {
            Invite invite = inviteRepository.findByCode(code);

            final String emailAddress = invite.getForEmail();
            final String domain = identityService.getDomainFromEmailAddress(emailAddress);

            return csrsService.getAgencyTokenForDomainTokenOrganisation(domain, form.getToken(), form.getOrganisation())
                    .map(agencyToken -> {
                        if (!agencyTokenCapacityService.hasSpaceAvailable(agencyToken)) {
                            log.info("Agency token uid = {}, capacity = {}, has no spaces available. User {} unable to signup");
                            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.NO_SPACES_AVAILABLE_ERROR_MESSAGE);
                            return REDIRECT_ENTER_TOKEN + code;
                        }

                        invite.setAuthorisedInvite(true);
                        inviteRepository.save(invite);

                        model.addAttribute(INVITE_MODEL, invite);

                        redirectAttributes.addFlashAttribute(TOKEN_INFO_FLASH_ATTRIBUTE, addAgencyTokenInfo(domain, form.getToken(), form.getOrganisation()));

                        log.debug("Enter token form has passed domain, token, organisation validation");

                        return REDIRECT_SIGNUP + code;
                    }).orElseGet(() -> {
                        log.debug("Enter token form has failed domain, token, organisation validation");
                        redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.ENTER_TOKEN_ERROR_MESSAGE);
                        return REDIRECT_ENTER_TOKEN + code;
                    });
        } else {
            return REDIRECT_LOGIN;
        }
    }

    private TokenRequest addAgencyTokenInfo(String domain, String token, String org) {
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setDomain(domain);
        tokenRequest.setToken(token);
        tokenRequest.setOrg(org);

        return tokenRequest;
    }
}
