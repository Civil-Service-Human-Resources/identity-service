package uk.gov.cshr.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.controller.form.UpdateEmailForm;
import uk.gov.cshr.controller.form.UpdatePasswordForm;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.EmailUpdateService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;

import javax.validation.Valid;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final IdentityService identityService;
    private final EmailUpdateService emailUpdateService;

    public AccountController(IdentityService identityService, EmailUpdateService emailUpdateService) {
        this.identityService = identityService;
        this.emailUpdateService = emailUpdateService;
    }

    @GetMapping("/password")
    public String updatePasswordForm(Model model, @ModelAttribute UpdatePasswordForm form) {
        model.addAttribute("updatePasswordForm", form);
        return "account/updatePassword";
    }

    @GetMapping("/passwordUpdated")
    public String passwordUpdated() {
        return "account/passwordUpdated";
    }

    @PostMapping("/password")
    public String updatePassword(Model model, @Valid @ModelAttribute UpdatePasswordForm form, BindingResult bindingResult, Authentication authentication) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("updatePasswordForm", form);
            return "account/updatePassword";
        }

        identityService.updatePasswordAndRevokeTokens(((IdentityDetails) authentication.getPrincipal()).getIdentity(), form.getNewPassword());

        return "redirect:/account/passwordUpdated";
    }

    @GetMapping("/email")
    public String updateEmailForm(Model model, @ModelAttribute UpdateEmailForm form) {
        model.addAttribute("updateEmailForm", form);
        return "account/updateEmail";
    }

    @PostMapping("/email")
    public String sendEmailVerification(Model model, @Valid @ModelAttribute UpdateEmailForm form, BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("updateEmailForm", form);
            return "account/updateEmail";
        }

        emailUpdateService.saveEmailUpdateAndNotify(((IdentityDetails) authentication.getPrincipal()).getIdentity(), form.getEmail());

        return "account/emailVerificationSent";
    }

    @GetMapping("/email/verify/{code}")
    public String verifyEmail(@PathVariable String code, Authentication authentication) {
        Identity identity = ((IdentityDetails) authentication.getPrincipal()).getIdentity();

        emailUpdateService.updateEmailAddress(identity, code);

        return "redirect:/account/passwordUpdated";
    }
}
