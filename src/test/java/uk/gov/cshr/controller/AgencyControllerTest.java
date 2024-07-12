package uk.gov.cshr.controller;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.cshr.Application;
import uk.gov.cshr.domain.AgencyTokenCapacityUsedDto;
import uk.gov.cshr.service.AgencyTokenCapacityService;
import uk.gov.cshr.utils.MockMVCFilterOverrider;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = Application.class)
@ActiveProfiles("no-redis")
public class AgencyControllerTest {

    private static final String UID = "UID";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockBean
    private AgencyTokenCapacityService agencyTokenCapacityService;

    @Before
    public void overridePatternMappingFilterProxyFilter() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy" );
    }

    @Test
    public void getSpacesUsedForAgencyToken() throws Exception {
        String accessToken = obtainAccessToken();

        AgencyTokenCapacityUsedDto agencyTokenCapacityUsedDto = new AgencyTokenCapacityUsedDto(100L);

        when(agencyTokenCapacityService.getSpacesUsedByAgencyToken(UID)).thenReturn(agencyTokenCapacityUsedDto);

        mockMvc.perform(
                get(String.format("/agency/%s", UID))
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(gson.toJson(agencyTokenCapacityUsedDto)));
    }

    @Test
    public void deleteAgencyToken_callsAgencyTokenCapacityServiceDeleteAgencyTokenOk() throws Exception {
        String accessToken = obtainAccessToken();

        String agencyTokenUid = UUID.randomUUID().toString();

        mockMvc.perform(
                delete(String.format("/agency/%s", agencyTokenUid))
                        .header("Authorization", "Bearer " + accessToken)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(agencyTokenCapacityService, times(1)).deleteAgencyToken(agencyTokenUid);
    }

    @Test
    public void deleteAgencyToken_callsAgencyTokenCapacityServiceDeleteAgencyTokenError() throws Exception {
        String accessToken = obtainAccessToken();

        String agencyTokenUid = UUID.randomUUID().toString();

        doThrow(Exception.class).when(agencyTokenCapacityService).deleteAgencyToken(agencyTokenUid);

        mockMvc.perform(
                delete(String.format("/agency/%s", agencyTokenUid))
                        .header("Authorization", "Bearer " + accessToken)
                        .with(csrf())
        ).andExpect(status().is5xxServerError());

        verify(agencyTokenCapacityService, times(1)).deleteAgencyToken(agencyTokenUid);
    }

    private String obtainAccessToken() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", "9fbd4ae2-2db3-44c7-9544-88e80255b56e");
        params.add("client_secret", "test");
        params.add("username", "learner@domain.com");
        params.add("password", "test");

        ResultActions result = mockMvc.perform(post("/oauth/token")
                        .params(params)
                        .header("Authorization", "Basic OWZiZDRhZTItMmRiMy00NGM3LTk1NDQtODhlODAyNTViNTZlOnRlc3Q=")
                        .accept("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        String resultString = result.andReturn().getResponse().getContentAsString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resultString).get("access_token").toString();
    }
}
