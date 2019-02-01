package uk.gov.cshr.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.controller.form.UpdateEmailForm;
import uk.gov.cshr.controller.form.UpdatePasswordForm;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;

import javax.validation.Valid;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final IdentityService identityService;

    public AccountController(IdentityService identityService) {
        this.identityService = identityService;
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
    public String sendEmailVerification(Model model, @Valid @ModelAttribute UpdateEmailForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("updateEmailForm", form);
            return "account/updateEmail";
        }

        return "account/emailVerificationSent";
    }
}
