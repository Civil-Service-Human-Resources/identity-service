package uk.gov.cshr.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.appinsights.FilterHelper;

import javax.servlet.http.Cookie;
import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {"MAINTENANCE_ENABLED = true"})
public class MaintenanceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FilterHelper filterHelper;

    @Before
    public void overrideFilters() throws IllegalAccessException {
        filterHelper.initAppInsighsFilter(mockMvc);
    }

    @Test
    public void shouldRedirectToMaintenance() throws Exception {
        mockMvc
            .perform(get("/login"))
            .andExpect(redirectedUrl("/maintenance"));
    }

    @Test
    public void assetsShouldRedirectToMaintenance() throws Exception {
        mockMvc
            .perform(get("/assets/img/gov.uk_logotype_crown.png"))
            .andExpect(redirectedUrl(null));
    }

    @Test
    public void oauthShouldRedirectToMaintenance() throws Exception {
        mockMvc
                .perform(get("/oauth/resolve"))
                .andExpect(redirectedUrl(null));
    }

    @Test
    public void shouldRedirectToMaintenanceWhenInvalidCookieValueIsPresent() throws Exception {
        mockMvc
            .perform(
                    get("/login")
                    .cookie(new Cookie("token", "token_invalid_value")))
            .andExpect(redirectedUrl("/maintenance"));
    }

    @Test
    public void shouldNotRedirectToMaintenanceWhenValidCookieValueIsPresent() throws Exception {
        mockMvc
                .perform(
                        get("/login")
                        .cookie(new Cookie("token", "token_value")))
                .andExpect(redirectedUrl(null));
    }
}
