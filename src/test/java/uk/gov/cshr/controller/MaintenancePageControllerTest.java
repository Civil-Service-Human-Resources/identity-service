package uk.gov.cshr.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.config.SpringSecurityTestConfig;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.utils.MockMVCFilterOverrider;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Import(SpringSecurityTestConfig.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class MaintenancePageControllerTest {

    private static final String MAINTENANCE_TEMPLATE = "maintenance";
    private static final String IDENTITY_UID = "a9cc9b0c-d257-4fa6-a760-950c09143e37";

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setup() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy");

        Identity identity = new Identity(IDENTITY_UID, null, null, true, false,
                null, null, false, true);
        IdentityDetails identityDetails = new IdentityDetails(identity);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(identityDetails);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void shouldDisplayMaintenancePage() throws Exception {
        mockMvc.perform(get("/maintenance")
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name(MAINTENANCE_TEMPLATE))
                .andExpect(content().string(containsString("Maintenance")))
                .andExpect(content().string(containsString("The learning website is undergoing scheduled maintenance.")))
                .andExpect(content().string(containsString("Apologies for the inconvenience.")))
                .andDo(print());
    }
}
