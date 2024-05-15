package uk.gov.cshr.utils;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.exception.GenericServerException;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MaintenancePageUtilTest {

    private static final String SKIP_MAINTENANCE_PAGE_PARAM_NAME = "username";

    @Mock
    private HttpServletRequest request;

    private MaintenancePageUtil createMaintenancePageUtil(boolean maintenancePageEnabled) {
        String skipMaintenancePageForUsers = "tester1@domain.com,tester2@domain.com";
        String skipMaintenancePageForUris = "/health,/info,/maintenance,/error,/cookies,/privacy," +
                "/accessibility-statement,/contact-us,/webjars,/assets,/css,/img,/favicon.ico";
        return new MaintenancePageUtil(maintenancePageEnabled, skipMaintenancePageForUsers,
                skipMaintenancePageForUris);
    }

    private boolean executeSkipMaintenancePageForUser(boolean maintenancePageEnabled,
                                                      String httpMethod, String username) {
        when(request.getMethod()).thenReturn(httpMethod);
        when(request.getParameter(SKIP_MAINTENANCE_PAGE_PARAM_NAME)).thenReturn(username);
        return createMaintenancePageUtil(maintenancePageEnabled).skipMaintenancePageForUser(request);
    }

    private boolean executeShouldNotApplyMaintenancePageFilterForURI(boolean maintenancePageEnabled, String requestUri) {
        when(request.getRequestURI()).thenReturn(requestUri);
        return createMaintenancePageUtil(maintenancePageEnabled).shouldNotApplyMaintenancePageFilterForURI(request);
    }

    private void executeSkipMaintenancePageCheck(boolean maintenancePageEnabled, String username) {
        createMaintenancePageUtil(maintenancePageEnabled).skipMaintenancePageCheck(username);
    }

    @Test
    public void shouldSkipMaintenancePageIfMaintenancePageIsDisabled() {
        assertTrue(executeSkipMaintenancePageForUser(false, "GET", null));
    }

    @Test
    public void shouldSkipMaintenancePageIfMaintenancePageIsEnabledAndHttpMethodIsOtherThanGET() {
        assertTrue(executeSkipMaintenancePageForUser(true, "POST", null));
    }

    @Test
    public void shouldNotSkipMaintenancePageIfMaintenancePageIsEnabledAndHttpMethodIsGETAndUsernameIsNotPassedInRequestParam() {
        assertFalse(executeSkipMaintenancePageForUser(true, "GET", null));
    }

    @Test
    public void shouldNotSkipMaintenancePageIfMaintenancePageIsEnabledAndHttpMethodIsGETAndUsernameIsPassedInRequestParamIsNotAllowedToSkipMaintenancePage() {
        assertFalse(executeSkipMaintenancePageForUser(true, "GET", "tester3@domain.com"));
    }

    @Test
    public void shouldSkipMaintenancePageIfMaintenancePageIsEnabledAndHttpMethodIsGETAndUsernameIsPassedInRequestParamIsAllowedToSkipMaintenancePage() {
        assertTrue(executeSkipMaintenancePageForUser(true, "GET", "tester1@domain.com"));
    }

    @Test
    public void shouldNotApplyMaintenancePageFilterForURIfMaintenancePageIsDisabled() {
        assertTrue(executeShouldNotApplyMaintenancePageFilterForURI(false, "/health"));
    }

    @Test
    public void shouldSkipMaintenancePageIfMaintenancePageIsEnabledAndRequestURIIsAllowedToSkipMaintenancePage() {
        assertTrue(executeShouldNotApplyMaintenancePageFilterForURI(true, "/health"));
    }

    @Test
    public void shouldNotSkipMaintenancePageIfMaintenancePageIsEnabledAndRequestURIIsNotAllowedToSkipMaintenancePage() {
        assertFalse(executeShouldNotApplyMaintenancePageFilterForURI(true, "/create"));
    }

    @Test
    public void shouldSkipMaintenancePageOnAuthenticationIfMaintenancePageIsDisabled() {
        try {
            executeSkipMaintenancePageCheck(false, "tester1@domain.com");
        } catch (Exception e) {
            fail("No exception is thrown");
        }
    }

    @Test
    public void shouldSkipMaintenancePageOnAuthenticationIfMaintenancePageIsEnabledAndUserIsAllowedToSkipMaintenancePage() {
        try {
            executeSkipMaintenancePageCheck(true, "tester1@domain.com");
        } catch (GenericServerException e) {
            fail("GenericServerException should not be thrown here.");
        }
    }

    @Test
    public void shouldNotSkipMaintenancePageOnAuthenticationIfMaintenancePageIsEnabledAndUserIsNotAllowedToSkipMaintenancePage() {
        GenericServerException thrown = assertThrows(GenericServerException.class, () ->
                        executeSkipMaintenancePageCheck(true, "tester3@domain.com"),
                "Expected skipMaintenancePageCheck() to throw GenericServerException, but it didn't");
        assertTrue(thrown.getMessage().contains("User is not allowed to access the website due to maintenance page is enabled."));
    }
}
