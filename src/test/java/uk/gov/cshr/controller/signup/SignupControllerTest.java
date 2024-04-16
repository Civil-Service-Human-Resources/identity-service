package uk.gov.cshr.controller.signup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.domain.*;
import uk.gov.cshr.dto.AgencyTokenDTO;
import uk.gov.cshr.exception.UnableToAllocateAgencyTokenException;
import uk.gov.cshr.service.AgencyTokenCapacityService;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.csrs.CsrsService;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.ApplicationConstants;
import uk.gov.cshr.utils.CsrfRequestPostProcessor;
import uk.gov.cshr.utils.MockMVCFilterOverrider;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@WithMockUser(username = "user")
public class SignupControllerTest {

    private static final String STATUS_ATTRIBUTE = "status";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InviteService inviteService;

    @MockBean
    private IdentityService identityService;

    @MockBean
    private CsrsService csrsService;

    @MockBean
    private AgencyTokenCapacityService agencyTokenCapacityService;

    private final String GENERIC_EMAIL = "email@domain.com";
    private final String GENERIC_DOMAIN = "domain.com";
    private final String GENERIC_CODE = "ABC123";
    private final String GENERIC_ORG_CODE = "org123";

    @Before
    public void overridePatternMappingFilterProxyFilter() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy" );
    }

    private Invite generateBasicInvite(boolean authorised) {
        Invite i = new Invite();
        i.setCode(GENERIC_CODE);
        i.setForEmail(GENERIC_EMAIL);
        i.setAuthorisedInvite(authorised);
        return i;
    }

    private OrganisationalUnitDto generateBasicOrganisation() {
        OrganisationalUnitDto organisationalUnitDto = new OrganisationalUnitDto();
        organisationalUnitDto.setCode(GENERIC_ORG_CODE);
        return organisationalUnitDto;
    }

    private AgencyTokenDTO generateBasicAgencyToken() {
        AgencyTokenDTO agencyTokenDTO = new AgencyTokenDTO();
        agencyTokenDTO.setAgencyDomains(Collections.singletonList(new Domain(1L, GENERIC_DOMAIN)));
        return agencyTokenDTO;
    }

    @Test
    public void shouldReturnCreateAccountForm() throws Exception {
        mockMvc.perform(
                get("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"email\"")))
                .andExpect(content().string(containsString("id=\"confirmEmail\"")));
    }

    @Test
    public void shouldConfirmInviteSentIfAllowlistedDomainAndNotAgency() throws Exception {
        String email = "user@domain.com";
        String domain = "domain.com";

        when(inviteService.findByForEmailAndStatus(email, InviteStatus.PENDING)).thenReturn(Optional.ofNullable(null));
        when(identityService.existsByEmail(email)).thenReturn(false);
        when(identityService.getDomainFromEmailAddress(email)).thenReturn(domain);
        when(csrsService.isDomainInAgency(domain)).thenReturn(false);
        when(csrsService.isDomainAllowlisted(domain)).thenReturn(true);
        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", email)
                        .param("confirmEmail", email))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("We've sent you an email")))
                .andExpect(content().string(containsString("What happens next")))
                .andExpect(content().string(containsString("We have sent you an email with a link to <strong>continue creating your account</strong>.")));

        verify(inviteService).sendSelfSignupInvite(email, true);
    }

    @Test
    public void shouldExpireInviteIfUserReregAfterRegAllowedTimeButBeforeActivationLinkExpire() throws Exception {
        Invite invite = generateBasicInvite(true);
        invite.setInvitedAt(new Date(System.currentTimeMillis() - 25*60*60*1000));

        when(inviteService.findByForEmailAndStatus(GENERIC_EMAIL, InviteStatus.PENDING)).thenReturn(Optional.of(invite));
        when(inviteService.isInviteCodeExpired(invite)).thenReturn(false);

        when(identityService.getDomainFromEmailAddress(GENERIC_EMAIL)).thenReturn(GENERIC_DOMAIN);
        when(csrsService.isDomainInAgency(GENERIC_DOMAIN)).thenReturn(false);
        when(csrsService.isDomainAllowlisted(GENERIC_DOMAIN)).thenReturn(true);
        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", GENERIC_EMAIL)
                        .param("confirmEmail", GENERIC_EMAIL))
                .andExpect(status().isOk());

        verify(inviteService, times(1)).updateInviteByCode(GENERIC_CODE, InviteStatus.EXPIRED);
    }

    @Test
    public void shouldFailValidationIfEmailAddressesDoNotMatch() throws Exception {
        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", "user@domain.org")
                        .param("confirmEmail", "user1@domain.org"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Email addresses do not match")));
    }

    @Test
    public void shouldFailValidationIfEmailAddressIsNotValid() throws Exception {
        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", "userdomain.org")
                        .param("confirmEmail", "userdomain.org"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Email address is not valid")));
    }

    @Test
    public void shouldRedirectToSignupIfUserHasAlreadyBeenInvited() throws Exception {
        Invite invite = generateBasicInvite(true);
        invite.setInvitedAt(new Date());
        when(inviteService.findByForEmailAndStatus(GENERIC_EMAIL, InviteStatus.PENDING)).thenReturn(Optional.of(invite));

        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", GENERIC_EMAIL)
                        .param("confirmEmail", GENERIC_EMAIL))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void shouldRedirectToSignupIfUserAlreadyExists() throws Exception {
        String email = "user@domain.com";
        Invite invite = generateBasicInvite(true);
        invite.setInvitedAt(new Date());
        when(inviteService.findByForEmailAndStatus(email, InviteStatus.PENDING)).thenReturn(Optional.of(invite));
        when(identityService.checkEmailExists(email)).thenReturn(true);

        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", email)
                        .param("confirmEmail", email))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/request"));
    }

    @Test
    public void shouldConfirmInviteSentIfAgencyTokenEmail() throws Exception {
        when(inviteService.findByForEmailAndStatus(GENERIC_EMAIL, InviteStatus.PENDING)).thenReturn(Optional.ofNullable(null));
        when(identityService.existsByEmail(GENERIC_EMAIL)).thenReturn(false);
        when(identityService.getDomainFromEmailAddress(GENERIC_EMAIL)).thenReturn(GENERIC_DOMAIN);
        when(csrsService.isDomainInAgency(GENERIC_DOMAIN)).thenReturn(true);

        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", GENERIC_EMAIL)
                        .param("confirmEmail", GENERIC_EMAIL))
                .andExpect(status().isOk())
                .andExpect(view().name("inviteSent"))
                .andExpect(content().string(containsString("We've sent you an email")))
                .andExpect(content().string(containsString("What happens next")))
                .andExpect(content().string(containsString("We have sent you an email with a link to <strong>continue creating your account</strong>.")));

        verify(inviteService).sendSelfSignupInvite(GENERIC_EMAIL, false);
    }

    @Test 
    public void shouldNotSendInviteIfNotallowlistedAndNotAgencyTokenEmail() throws Exception {
        when(inviteService.findByForEmailAndStatus(GENERIC_EMAIL, InviteStatus.PENDING)).thenReturn(Optional.empty());
        when(identityService.existsByEmail(GENERIC_EMAIL)).thenReturn(false);
        when(identityService.getDomainFromEmailAddress(GENERIC_EMAIL)).thenReturn(GENERIC_DOMAIN);
        when(csrsService.isDomainInAgency(GENERIC_EMAIL)).thenReturn(false);

        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", GENERIC_EMAIL)
                        .param("confirmEmail", GENERIC_EMAIL))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/request"))
                .andExpect(flash().attribute(STATUS_ATTRIBUTE, "Your organisation is unable to use this service. Please contact your line manager."));
    }

    @Test
    public void shouldRedirectToSignupIfSignupCodeNotValid() throws Exception {
        when(inviteService.findByCode(GENERIC_CODE)).thenReturn(null);
        mockMvc.perform(
                get("/signup/" + GENERIC_CODE)
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/request"));
    }

    @Test
    public void shouldRedirectToSignupIfInviteCodeExpired() throws Exception {

        Invite invite = generateBasicInvite(false);
        when(inviteService.findByCode(GENERIC_CODE)).thenReturn(invite);
        when(inviteService.isInviteCodeExpired(invite)).thenReturn(true);
        doNothing().when(inviteService).updateInviteByCode(GENERIC_CODE, InviteStatus.EXPIRED);

        mockMvc.perform(
                get("/signup/" + GENERIC_CODE)
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/request"));
    }

    @Test
    public void shouldRedirectToSignupIfInviteCodeDoesNotExists() throws Exception {
        when(inviteService.findByCode(GENERIC_CODE)).thenReturn(null);

        mockMvc.perform(
                get("/signup/" + GENERIC_CODE)
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/request"));
    }

    @Test
    public void shouldRedirectToEnterTokenPageIfInviteNotAuthorised() throws Exception {
        Invite invite = generateBasicInvite(false);

        when(inviteService.findByCode(GENERIC_CODE)).thenReturn(invite);
        when(inviteService.isInviteCodeExpired(invite)).thenReturn(false);

        mockMvc.perform(
                get("/signup/" + GENERIC_CODE)
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/chooseOrganisation/" + GENERIC_CODE));
    }

    @Test
    public void shouldReturnSignupIfInviteAuthorised() throws Exception {
        Invite invite = generateBasicInvite(true);
        when(inviteService.findByCode(GENERIC_CODE)).thenReturn(invite);

        mockMvc.perform(
                get("/signup/" + GENERIC_CODE)
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

    @Test
    public void shouldNotPostIfPasswordsDifferent() throws Exception {
        String password = "Password1";
        String differentPassword = "differentPassword1";
        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(generateBasicInvite(true));
        mockMvc.perform(
                post("/signup/" + GENERIC_CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("password", password)
                        .param("confirmPassword", differentPassword))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

    @Test 
    public void shouldRedirectToSignUpIfFormHasError() throws Exception {
        String password = "password";
        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(generateBasicInvite(true));
        mockMvc.perform(
                post("/signup/" + GENERIC_CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("password", password)
                        .param("confirmPassword", "doesn't match"))
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("invite"));
    }

    @Test
    public void shouldRedirectToLoginIfInviteNotValid() throws Exception {
        String password = "Password1";

        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(null);

        mockMvc.perform(
                post("/signup/" + GENERIC_CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("password", password)
                        .param("confirmPassword", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void shouldRedirectToEnterTokenIfInviteNotAuthorised() throws Exception {
        String password = "Password1";
        Invite invite = generateBasicInvite(false);

        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(invite);

        mockMvc.perform(
                post("/signup/" + GENERIC_CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("password", password)
                        .param("confirmPassword", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/chooseOrganisation/" + GENERIC_CODE));
    }

    @Test
    public void shouldReturnSignupSuccessIfInviteAuthorised() throws Exception {
        String password = "Password1";
        Invite invite = generateBasicInvite(true);
        TokenRequest tokenRequest = new TokenRequest();

        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(invite);
        doNothing().when(identityService).createIdentityFromInviteCode(GENERIC_CODE, password, tokenRequest);
        doNothing().when(inviteService).updateInviteByCode(GENERIC_CODE, InviteStatus.ACCEPTED);

        mockMvc.perform(
                post("/signup/" + GENERIC_CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("password", password)
                        .param("confirmPassword", password)
                        .flashAttr("exampleEntity", tokenRequest))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("signupSuccess"));
    }

    @Test
    public void shouldRedirectToPasswordSignupIfExceptionThrown() throws Exception {
        String password = "Password1";
        Invite invite = generateBasicInvite(true);
        TokenRequest tokenRequest = new TokenRequest();

        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(invite);
        doThrow(new UnableToAllocateAgencyTokenException("Error")).when(identityService).createIdentityFromInviteCode(GENERIC_CODE, password, tokenRequest);

        mockMvc.perform(
                post("/signup/" + GENERIC_CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("password", password)
                        .param("confirmPassword", password)
                        .flashAttr("exampleEntity", tokenRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/" + GENERIC_CODE));
    }

    /*
    Enter organisation
     */

    @Test
    public void enterOrganisationRedirectToLoginWhenInviteIsInvalid() throws Exception {
        String code = "abc123";

        when(inviteService.fetchValidInviteWithCode(code)).thenReturn(null);
        mockMvc.perform(
                        get("/signup/chooseOrganisation/" + code)
                                .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void enterOrganisationRedirectToSignUpWhenInviteIsAuthorised() throws Exception {
        Invite invite = generateBasicInvite(true);
        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(invite);
        mockMvc.perform(
                        get("/signup/chooseOrganisation/" + GENERIC_CODE)
                                .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/" + GENERIC_CODE));
    }

    @Test
    public void enterOrganisationShouldRenderTemplateWhenInviteIsValid() throws Exception {
        Invite invite = generateBasicInvite(false);
        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(invite);
        mockMvc.perform(
                        get("/signup/chooseOrganisation/" + GENERIC_CODE)
                                .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("chooseOrganisation"));
    }

    /*
    Choose organisation
     */

    @Test
    public void chooseOrganisationShouldRedirectToEnterTokenIfAgency() throws Exception {
        OrganisationalUnitDto org = generateBasicOrganisation();
        org.setAgencyToken(generateBasicAgencyToken());
        List<OrganisationalUnitDto> orgs = Collections.singletonList(org);
        Invite invite = generateBasicInvite(false);
        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(invite);
        when(csrsService.getAllOrganisations()).thenReturn(orgs);
        mockMvc.perform(
                        post("/signup/chooseOrganisation/" + GENERIC_CODE)
                                .with(CsrfRequestPostProcessor.csrf())
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                .param("organisation", GENERIC_ORG_CODE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(String.format("/signup/enterToken/%s/%s", GENERIC_CODE, GENERIC_ORG_CODE)));
    }

    @Test
    public void chooseOrganisationShouldAuthoriseInviteAndRedirectToSignupIfAllowlist() throws Exception {
        OrganisationalUnitDto org = generateBasicOrganisation();
        org.setDomains(Collections.singletonList(new Domain(1L, GENERIC_DOMAIN)));
        List<OrganisationalUnitDto> orgs = Collections.singletonList(org);
        Invite invite = generateBasicInvite(false);
        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(invite);
        when(csrsService.getAllOrganisations()).thenReturn(orgs);
        mockMvc.perform(
                        post("/signup/chooseOrganisation/" + GENERIC_CODE)
                                .with(CsrfRequestPostProcessor.csrf())
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                .param("organisation", GENERIC_ORG_CODE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(String.format("/signup/%s", GENERIC_CODE)));
    }


    /*
    Enter token
     */

    private String enterTokenUrl() {
        return "/signup/enterToken/" + GENERIC_CODE + "/" + GENERIC_ORG_CODE;
    }

    @Test
    public void shouldRedirectToLoginIfInviteNotValidFromToken() throws Exception {
        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(null);

        mockMvc.perform(
                get(enterTokenUrl())
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void shouldReturnEnterToken() throws Exception {

        Invite invite = generateBasicInvite(false);
        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(invite);
        when(csrsService.getOrganisationWithCodeAndAgencyDomain(GENERIC_CODE, GENERIC_DOMAIN))
                .thenReturn(Optional.of(new OrganisationalUnitDto()));

        mockMvc.perform(
                get(enterTokenUrl())
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("enterToken"));
    }

    @Test
    public void shouldRedirectOnEnterTokenIfTokenAuth() throws Exception {
        Invite invite = generateBasicInvite(true);
        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(invite);

        mockMvc.perform(
                get(enterTokenUrl())
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/" + GENERIC_CODE));
    }

    /*
    Choose token
     */

    @Test
    public void shouldRedirectToLoginIfTokenInviteInvalid() throws Exception {
        String token = "token123";

        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(null);

        mockMvc.perform(
                post(enterTokenUrl())
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void shouldRedirectToSignupIfInviteValidAndAgencyTokenHasSpaceAvailable() throws Exception {
        String token = "token123";

        Invite invite = generateBasicInvite(true);

        AgencyTokenDTO agencyToken = new AgencyTokenDTO();
        agencyToken.setCapacity(10);
        Optional<AgencyTokenDTO> optionalAgencyToken = Optional.of(agencyToken);

        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(invite);
        when(identityService.getDomainFromEmailAddress(GENERIC_EMAIL)).thenReturn(GENERIC_DOMAIN);
        when(csrsService.getAgencyTokenForDomainTokenOrganisation(GENERIC_DOMAIN, token, GENERIC_ORG_CODE)).thenReturn(optionalAgencyToken);
        when(agencyTokenCapacityService.hasSpaceAvailable(agencyToken)).thenReturn(true);

        mockMvc.perform(
                post(enterTokenUrl())
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/" + GENERIC_CODE));
    }

    @Test
    public void shouldRedirectToTokenWithErrorIfInviteValidAndAgencyTokenDoesNotHaveSpaceAvailable() throws Exception {
        String token = "token123";

        Invite invite = generateBasicInvite(true);
        AgencyTokenDTO agencyToken = new AgencyTokenDTO();
        agencyToken.setCapacity(10);
        Optional<AgencyTokenDTO> optionalAgencyToken = Optional.of(agencyToken);

        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(invite);
        when(identityService.getDomainFromEmailAddress(GENERIC_EMAIL)).thenReturn(GENERIC_DOMAIN);
        when(csrsService.getAgencyTokenForDomainTokenOrganisation(GENERIC_DOMAIN, token, GENERIC_ORG_CODE)).thenReturn(optionalAgencyToken);
        when(agencyTokenCapacityService.hasSpaceAvailable(agencyToken)).thenReturn(false);

        mockMvc.perform(
                post(enterTokenUrl())
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("organisation", GENERIC_ORG_CODE)
                        .param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/enterToken/" + GENERIC_CODE + "/" + GENERIC_ORG_CODE));
    }

    @Test
    public void shouldRedirectToEnterTokenWithErrorMessageIfNoTokensFound() throws Exception {
        String token = "token123";

        Invite invite = generateBasicInvite(true);
        Optional<AgencyTokenDTO> emptyOptional = Optional.empty();

        when(inviteService.fetchValidInviteWithCode(GENERIC_CODE)).thenReturn(invite);
        when(identityService.getDomainFromEmailAddress(GENERIC_EMAIL)).thenReturn(GENERIC_DOMAIN);
        when(csrsService.getAgencyTokenForDomainTokenOrganisation(GENERIC_DOMAIN, token, GENERIC_ORG_CODE)).thenReturn(emptyOptional);

        mockMvc.perform(
                post(enterTokenUrl())
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(enterTokenUrl()))
                .andExpect(flash().attribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.ENTER_TOKEN_ERROR_MESSAGE));
    }

}
