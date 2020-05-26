package uk.gov.cshr.controller.emailUpdate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.config.SpringSecurityTestConfig;
import uk.gov.cshr.controller.account.email.AgencyTokenVerificationController;
import uk.gov.cshr.controller.form.EmailUpdatedRecentlyEnterTokenForm;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.service.AgencyTokenCapacityService;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.EmailUpdateService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.CsrfRequestPostProcessor;
import uk.gov.cshr.utils.MockMVCFilterOverrider;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Import(SpringSecurityTestConfig.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class AgencyTokenVerificationControllerTest {

    private static final String VERIFY_TOKEN_URL = "/account/email/verify/agency/";
    private static final String REDIRECT_EMAIL_UPDATED = "/account/email/updated";

    private static final String CODE = "7haQOIeV5n0CYk7yrfEmxzxHQtbuV5PPPN8BgCTM";
    private static final String DOMAIN = "example.com";
    private static final String TOKEN = "DOI1KFJD5D";
    private static final String IDENTITY_UID = "a9cc9b0c-d257-4fa6-a760-950c09143e37";
    private static final String ORGANISATION = "co";
    private static final String AGENCY_TOKEN_UID = "675fd21d-03f9-4922-a2ff-c19186270b04";
    private static final String EMAIL = "test@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailUpdateService emailUpdateService;

    @MockBean
    private AgencyTokenCapacityService agencyTokenCapacityService;

    @MockBean
    private CsrsService csrsService;

    @MockBean
    private IdentityService identityService;

    private AgencyTokenVerificationController agencyTokenVerificationController;

    private OrganisationalUnitDto[] organisations;

    @Before
    public void setup() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy");

        agencyTokenVerificationController = new AgencyTokenVerificationController(
                emailUpdateService,
                csrsService,
                agencyTokenCapacityService,
                identityService);

        organisations = new OrganisationalUnitDto[1];
        organisations[0] = new OrganisationalUnitDto();
        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisations);

        Authentication authentication = mock(Authentication.class);

        Identity identity = new Identity(IDENTITY_UID, null, null, true, false, null, null, false, true);
        IdentityDetails identityDetails = new IdentityDetails(identity);
        when(authentication.getPrincipal()).thenReturn(identityDetails);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void givenARequestToDisplayEnterTokenPage_whenEnterToken_thenShouldDisplayEnterTokenPageWithAllPossibleOrganisations() throws Exception {
        mockMvc.perform(
                get(VERIFY_TOKEN_URL + CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .flashAttr("uid", IDENTITY_UID)
                        .flashAttr("email", EMAIL)
                        .flashAttr("domain", DOMAIN))
                .andExpect(status().isOk())
                .andExpect(model().size(6))
                .andExpect(model().attribute("organisations", organisations))
                .andExpect(model().attribute("domain", DOMAIN))
                .andExpect(model().attribute("uid", IDENTITY_UID))
                .andExpect(model().attribute("email", EMAIL))
                .andExpect(model().attribute("code", CODE))
                .andExpect(model().attributeExists("emailUpdatedRecentlyEnterTokenForm"))
                .andExpect(view().name("enterTokenSinceEmailUpdate"));

        verify(identityService, times(1)).getDomainFromEmailAddress(EMAIL);
    }

    @Test
    public void givenARequestToDisplayEnterTokenPageAndFormAlreadyExistsInModel_whenEnterToken_thenShouldDisplayEnterTokenPageWithAllPossibleOrganisationsAndTheExistingForm() throws Exception {
        EmailUpdatedRecentlyEnterTokenForm existingForm = new EmailUpdatedRecentlyEnterTokenForm();
        existingForm.setUid(IDENTITY_UID);
        existingForm.setDomain(DOMAIN);
        existingForm.setOrganisation(ORGANISATION);
        existingForm.setToken(TOKEN);

        mockMvc.perform(
                get(VERIFY_TOKEN_URL + CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .flashAttr("emailUpdatedRecentlyEnterTokenForm", existingForm)
                        .flashAttr("uid", IDENTITY_UID)
                        .flashAttr("domain", DOMAIN))
                .andExpect(status().isOk())
                .andExpect(model().size(5))
                .andExpect(model().attribute("organisations", organisations))
                .andExpect(model().attribute("domain", DOMAIN))
                .andExpect(model().attribute("uid", IDENTITY_UID))
                .andExpect(model().attribute("code", CODE))
                .andExpect(model().attributeExists("emailUpdatedRecentlyEnterTokenForm"))
                .andExpect(view().name("enterTokenSinceEmailUpdate"));
    }

    @Test
    public void givenAValidTokenForm_whenCheckToken_thenShouldSubmitToken() throws Exception {
        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(AGENCY_TOKEN_UID);

        when(csrsService.getAgencyTokenForDomainTokenOrganisation(DOMAIN, TOKEN, ORGANISATION)).thenReturn(Optional.of(agencyToken));
        when(agencyTokenCapacityService.hasSpaceAvailable(agencyToken)).thenReturn(true);

        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setEmail(EMAIL);
        Identity identity = new Identity();
        identity.setEmail(EMAIL);
        when(emailUpdateService.getEmailUpdate(any(Identity.class), eq(CODE))).thenReturn(emailUpdate);
        doNothing().when(emailUpdateService).updateEmailAddress(any(HttpServletRequest.class), eq(identity), eq(emailUpdate), eq(agencyToken));

        mockMvc.perform(
                post(VERIFY_TOKEN_URL + CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation", ORGANISATION)
                        .param("token", TOKEN)
                        .param("domain", DOMAIN)
                        .param("uid", IDENTITY_UID)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(REDIRECT_EMAIL_UPDATED));
    }

    @Test
    public void givenAValidTokenForm_whenCheckTokenAndNoSpacesAvailable_thenShouldRedirect() throws Exception {
        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(AGENCY_TOKEN_UID);

        when(csrsService.getAgencyTokenForDomainTokenOrganisation(DOMAIN, TOKEN, ORGANISATION)).thenReturn(Optional.of(agencyToken));
        when(agencyTokenCapacityService.hasSpaceAvailable(agencyToken)).thenReturn(false);

        EmailUpdate emailUpdate = new EmailUpdate();
        Identity identity = new Identity();
        when(emailUpdateService.getEmailUpdate(identity, CODE)).thenReturn(emailUpdate);
        doNothing().when(emailUpdateService).updateEmailAddress(any(HttpServletRequest.class), eq(identity), eq(emailUpdate), eq(agencyToken));

        mockMvc.perform(
                post(VERIFY_TOKEN_URL + CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation", ORGANISATION)
                        .param("token", TOKEN)
                        .param("domain", DOMAIN)
                        .param("uid", IDENTITY_UID)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(VERIFY_TOKEN_URL + CODE));
    }
}
