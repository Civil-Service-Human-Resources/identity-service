package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.dto.IdentityDTO;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.AccessTokenService;
import uk.gov.cshr.service.IdentityService;

@Controller
@RequestMapping("/management/")
public class IdentityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private IdentityService identityService;

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private AccessTokenService accessTokenService;

    @RequestMapping(value = "/identity", method = RequestMethod.POST)
    public IdentityDTO getIdentityDetailsfromAccessToken(@RequestParam("access_token") String accessToken) {
        Identity identity = accessTokenService.findActiveAccessToken(accessToken).getIdentity();
        return new IdentityDTO(identity.getEmail(), identity.getUid(), identity.getRoles());
    }

    @GetMapping("/identities")
    public String identities(Model model) {
        LOGGER.debug("Listing all roles");

        Iterable<Identity> identities = identityService.findAll();

        model.addAttribute("identities", identities);
        model.addAttribute("identity", new Identity());

        return "identityList";
    }
}
