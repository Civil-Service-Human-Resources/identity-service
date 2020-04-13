package uk.gov.cshr.controller.reactivation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.controller.form.EmailUpdatedRecentlyEnterTokenForm;
import uk.gov.cshr.controller.form.ReactivationEnterTokenForm;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.ReactivationService;

import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping("/reactivate")
public class ReactivationController {

    private static final String RECENTLY_REACTIVATED_ENTER_TOKEN_FORM_TEMPLATE = "reactivationEnterTokenForm";

    private static final String ENTER_TOKEN_SINCE_REACTIVATION_VIEW_NAME_TEMPLATE = "enterTokenSinceReactivation";

    private static final String STATUS_ATTRIBUTE = "status";

    private final ReactivationService reactivationService;

    private final CsrsService csrsService;

    private final String lpgUiUrl;

    public ReactivationController(
            ReactivationService reactivationService,
            CsrsService csrsService,
            @Value("${lpg.uiUrl}") String lpgUiUrl) {
        this.reactivationService = reactivationService;
        this.csrsService = csrsService;
        this.lpgUiUrl = lpgUiUrl;
    }

    @GetMapping(path = "/enterToken")
    public String enterToken(Model model) {

        log.info("User accessing token-based reactivate screen");

        if(model.containsAttribute(RECENTLY_REACTIVATED_ENTER_TOKEN_FORM_TEMPLATE)) {
            OrganisationalUnitDto[] organisations = csrsService.getOrganisationalUnitsFormatted();
            model.addAttribute("organisations", organisations);
            return ENTER_TOKEN_SINCE_REACTIVATION_VIEW_NAME_TEMPLATE;
        } else {
            String domain = (String) model.asMap().get("domain");
            String uid = (String) model.asMap().get("uid");

            OrganisationalUnitDto[] organisations = csrsService.getOrganisationalUnitsFormatted();

            model.addAttribute("organisations", organisations);
            ReactivationEnterTokenForm form = new ReactivationEnterTokenForm();
            form.setDomain(domain);
            form.setUid(uid);
            model.addAttribute(RECENTLY_REACTIVATED_ENTER_TOKEN_FORM_TEMPLATE, form);
            return ENTER_TOKEN_SINCE_REACTIVATION_VIEW_NAME_TEMPLATE;
        }

    }

    @PostMapping(path = "/enterToken")
    public String checkToken(Model model,
                             @ModelAttribute @Valid EmailUpdatedRecentlyEnterTokenForm form,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        log.info("User attempting token-based sign up since being reactivated");

        String domain = form.getDomain();
        String uid = form.getUid();

        if (bindingResult.hasErrors()) {
            model.addAttribute(RECENTLY_REACTIVATED_ENTER_TOKEN_FORM_TEMPLATE, form);
            return ENTER_TOKEN_SINCE_REACTIVATION_VIEW_NAME_TEMPLATE;
        }

        try {
            log.info("User checking Enter Token form with domain = {}, token = {}, org = {}", domain, form.getToken(), form.getOrganisation());
            reactivationService.processReactivation(uid);
            return "redirect:" + lpgUiUrl;
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Incorrect token for this organisation");
            redirectAttributes.addFlashAttribute(RECENTLY_REACTIVATED_ENTER_TOKEN_FORM_TEMPLATE, form);
            return "redirect:/reactivate/enterToken";
        } catch (Exception e) {
            return "redirect:/login";
        }

    }
}
