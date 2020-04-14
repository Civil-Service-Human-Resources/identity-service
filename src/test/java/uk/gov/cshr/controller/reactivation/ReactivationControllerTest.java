package uk.gov.cshr.controller.reactivation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.config.SpringSecurityTestConfig;
import uk.gov.cshr.controller.form.ReactivationEnterTokenForm;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.ReactivationService;
import uk.gov.cshr.utils.CsrfRequestPostProcessor;
import uk.gov.cshr.utils.MockMVCFilterOverrider;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Import(SpringSecurityTestConfig.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class ReactivationControllerTest {

    private static final String ENTER_TOKEN_URL = "/reactivate/enterToken";

    private static final String RECENTLY_REACTIVATED_ENTER_TOKEN_FORM_TEMPLATE = "reactivationEnterTokenForm";

    private static final String ENTER_TOKEN_SINCE_REACTIVATION_VIEW_NAME_TEMPLATE = "enterTokenSinceReactivation";

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReactivationService reactivationService;

    @MockBean
    private CsrsService csrsService;

    private ReactivationController classUnderTest;

    private OrganisationalUnitDto[] organisations;

    @Before
    public void setup() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy" );

        classUnderTest = new ReactivationController(reactivationService, csrsService, lpgUiUrl);

        // set up organisations list for all test scenarios
        organisations = new OrganisationalUnitDto[1];
        organisations[0] = new OrganisationalUnitDto();
        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisations);
        // set up service method call for all non-exception test scenarios
        doNothing().when(reactivationService).processReactivation(any(HttpServletRequest.class), anyString());
    }

    @Test
    public void givenARequestToDisplayEnterTokenPage_whenEnterToken_thenShouldDisplayEnterTokenPageWithAllPossibleOrganisations() throws Exception {
        // only called with 2 flash attributes, from redirect controller.
        mockMvc.perform(
                get(ENTER_TOKEN_URL)
                        .with(CsrfRequestPostProcessor.csrf())
                        .flashAttr("uid", "myuid")
                        .flashAttr("domain", "mydomain"))
                .andExpect(status().isOk())
                .andExpect(model().size(4))
                .andExpect(model().attribute("organisations", organisations))
                .andExpect(model().attribute("domain", "mydomain"))
                .andExpect(model().attribute("uid", "myuid"))
                .andExpect(model().attributeExists(RECENTLY_REACTIVATED_ENTER_TOKEN_FORM_TEMPLATE))
                .andExpect(view().name(ENTER_TOKEN_SINCE_REACTIVATION_VIEW_NAME_TEMPLATE))
                .andDo(print());
    }

    @Test
    public void givenARequestToDisplayEnterTokenPageAndFormAlreadyExistsInModel_whenEnterToken_thenShouldDisplayEnterTokenPageWithAllPossibleOrganisationsAndTheExistingForm() throws Exception {
        // given
        ReactivationEnterTokenForm existingForm = new ReactivationEnterTokenForm();
        existingForm.setUid("myuid");
        existingForm.setDomain("mydomain");
        existingForm.setOrganisation("myorganisation");
        existingForm.setToken("mytoken");

        mockMvc.perform(
                get(ENTER_TOKEN_URL)
                        .with(CsrfRequestPostProcessor.csrf())
                        .flashAttr(RECENTLY_REACTIVATED_ENTER_TOKEN_FORM_TEMPLATE, existingForm)
                        .flashAttr("uid", "myuid")
                        .flashAttr("domain", "mydomain"))
                .andExpect(status().isOk())
                .andExpect(model().size(4))
                .andExpect(model().attribute("organisations", organisations))
                .andExpect(model().attribute("domain", "mydomain"))
                .andExpect(model().attribute("uid", "myuid"))
                .andExpect(model().attributeExists(RECENTLY_REACTIVATED_ENTER_TOKEN_FORM_TEMPLATE))
                .andExpect(view().name(ENTER_TOKEN_SINCE_REACTIVATION_VIEW_NAME_TEMPLATE))
                .andDo(print());
    }

    @Test
    public void givenAValidTokenForm_whenCheckToken_thenShouldSubmitToken() throws Exception {

        mockMvc.perform(
                post(ENTER_TOKEN_URL)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation","myorganisation")
                        .param("token","mytoken")
                        .param("domain","mydomain")
                        .param("uid","myuid")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(lpgUiUrl));

        verify(reactivationService, times(1)).processReactivation(any(HttpServletRequest.class), eq("myuid"));
    }

    @Test
    public void givenAInvalidTokenFormNoOrganisation_whenCheckToken_thenShouldRedisplayToEnterTokenPageWithOneErrorMessage() throws Exception {

        mockMvc.perform(
                post(ENTER_TOKEN_URL)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation","")
                        .param("token","mytoken")
                        .param("domain","mydomain")
                        .param("uid","myuid")
        )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(RECENTLY_REACTIVATED_ENTER_TOKEN_FORM_TEMPLATE))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasErrors(RECENTLY_REACTIVATED_ENTER_TOKEN_FORM_TEMPLATE))
                //.andExpect(model().attributeHasFieldErrorCode("ReactivationdRecentlyEnterTokenForm", "organisation", "Please confirm your new organisation"))
                .andExpect(view().name(ENTER_TOKEN_SINCE_REACTIVATION_VIEW_NAME_TEMPLATE));

        verify(reactivationService, never()).processReactivation(any(HttpServletRequest.class), anyString());
    }

    @Test
    public void givenAInvalidTokenFormNoOrganisationAndNoToken_whenCheckToken_thenShouldRedisplayToEnterTokenPageWithTwoErrorMessages() throws Exception {

        mockMvc.perform(
                post(ENTER_TOKEN_URL)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation","")
                        .param("token","")
                        .param("domain","mydomain")
                        .param("uid","myuid")
        )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(RECENTLY_REACTIVATED_ENTER_TOKEN_FORM_TEMPLATE))
                .andExpect(model().errorCount(2))
                .andExpect(model().attributeHasErrors(RECENTLY_REACTIVATED_ENTER_TOKEN_FORM_TEMPLATE))
                //.andExpect(model().attributeHasFieldErrorCode("ReactivationdRecentlyEnterTokenForm", "organisation", "Please confirm your new organisation"))
                // .andExpect(model().attributeHasFieldErrorCode("ReactivationdRecentlyEnterTokenForm", "token", "Please confirm your new token"))
                .andExpect(view().name(ENTER_TOKEN_SINCE_REACTIVATION_VIEW_NAME_TEMPLATE));

        verify(reactivationService, never()).processReactivation(any(HttpServletRequest.class), anyString());
    }

    @Test
    public void givenAValidTokenFormAndResourceNotFoundDuringReactivation_whenCheckToken_thenShouldRedirectToEnterTokenPageWithErrorMessage() throws Exception {

        doThrow(new ResourceNotFoundException()).when(reactivationService).processReactivation(any(HttpServletRequest.class), anyString());

        mockMvc.perform(
                post(ENTER_TOKEN_URL)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation","myorganisation")
                        .param("token","mytoken")
                        .param("domain","mydomain")
                        .param("uid","myuid")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ENTER_TOKEN_URL))
                .andExpect(flash().attribute("status", "Incorrect token for this organisation"))
                .andExpect(flash().attributeExists(RECENTLY_REACTIVATED_ENTER_TOKEN_FORM_TEMPLATE));

        verify(reactivationService, times(1)).processReactivation(any(HttpServletRequest.class), eq("myuid"));
    }

    @Test
    public void givenAValidTokenFormAndTechnicalErrorOccursDuringReactivation_whenCheckToken_thenShouldRedirectToLoginPage() throws Exception {

        doThrow(new RuntimeException()).when(reactivationService).processReactivation(any(HttpServletRequest.class), anyString());

        mockMvc.perform(
                post(ENTER_TOKEN_URL)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation","myorganisation")
                        .param("token","mytoken")
                        .param("domain","mydomain")
                        .param("uid","myuid")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(reactivationService, times(1)).processReactivation(any(HttpServletRequest.class), eq("myuid"));
    }
}
