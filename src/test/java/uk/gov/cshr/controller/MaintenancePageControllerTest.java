package uk.gov.cshr.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest({MaintenancePageController.class})
@WithMockUser(username = "user")
public class MaintenancePageControllerTest {

    private static final String MAINTENANCE_TEMPLATE = "maintenance/maintenance";

    @Autowired
    private MockMvc mockMvc;

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
