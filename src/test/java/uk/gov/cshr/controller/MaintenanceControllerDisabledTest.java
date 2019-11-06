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
import uk.gov.cshr.AppInsightsHelper.Helper;

import javax.servlet.http.Cookie;
import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {"MAINTENANCE_ENABLED = false"})
public class MaintenanceControllerDisabledTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Helper helper;

    @Before
    public void overrideFilters() throws IllegalAccessException {
        helper.initAppInsighsFilter(mockMvc);
    }

    @Test
    public void shouldRedirectToMaintenance() throws Exception {
        mockMvc
            .perform(get("/maintenance"))
            .andExpect(redirectedUrl("/"));
    }

}
