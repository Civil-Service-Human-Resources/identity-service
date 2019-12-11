package uk.gov.cshr.controller.emailUpdate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class RedirectController {

    private static final String STATUS_ATTRIBUTE = "status";

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @Value("${lpg.changeOrgUrl}")
    private String lpgChangeOrgUrl;

    @GetMapping("/invalid")
    public RedirectView notAValidEmailDomain(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // TODO - ASK WHAT SHOULD HAPPEN IN THIS SCENARIO
        redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Your organisation is unable to use this service. Please contact your line manager.");
        return new RedirectView("/logout");
    }

    @RequestMapping("/redirectToUIHomePage")
    public RedirectView goToUIHomePage(Model model) {
        model.asMap().clear();
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(lpgUiUrl);
        return redirectView;
    }

    @RequestMapping("/redirectToChangeOrgPage/{domain}/{uid}")
    public RedirectView goToChangeOrgPage(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @PathVariable String domain, @PathVariable String uid) {
        redirectAttributes.addFlashAttribute("domain", domain);
        redirectAttributes.addFlashAttribute("uid", uid);
        RedirectView redirectView = new RedirectView();
        String url = "/updateOrganisation/enterOrganisation";
        redirectView.setUrl(url);
        return redirectView;
    }

    @RequestMapping("/redirectToEnterTokenPage/{domain}/{uid}")
    public RedirectView goToEnterTokenSinceEmailUpdatePage(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @PathVariable String domain, @PathVariable String uid) {
        redirectAttributes.addFlashAttribute("domain", domain);
        redirectAttributes.addFlashAttribute("uid", uid);
        RedirectView redirectView = new RedirectView();
        String url = "/emailUpdated/enterToken";
        redirectView.setUrl(url);
        return redirectView;
    }

}