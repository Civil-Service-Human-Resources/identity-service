package uk.gov.cshr.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MaintenancePageUtilTest {

    private static final String SKIP_MAINTENANCE_PAGE_PARAM_NAME = "username";

    @Mock
    private HttpServletRequest request;

    private MaintenancePageUtil createMaintenancePageUtil(boolean maintenancePageEnabled) {
        String skipMaintenancePageForUsers = "tester1@domain.com,tester2@domain.com";
        String skipMaintenancePageForUris = "/health,/maintenance,/error,/logout,/webjars,/css,/img,/js,/favicon.ico," +
                "/oauth/revoke,/oauth/resolve,/oauth/token,/oauth/check_token," +
                "/api/identities,/signup/chooseOrganisation,/signup/enterToken," +
                "/account/verify/agency,/account/reactivate/updated,/account/email/updated";
        String encryptionKey = "0123456789abcdef0123456789abcdef";
        return new MaintenancePageUtil(maintenancePageEnabled, skipMaintenancePageForUsers,
                skipMaintenancePageForUris, encryptionKey);
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

    @Test
    public void shouldSkipMaintenancePageIfMaintenancePageIsDisabled() {
        assertTrue(executeSkipMaintenancePageForUser(false, "GET", null));
    }

    @Test
    public void shouldSkipMaintenancePageIfMaintenancePageIsEnabledAndHttpMethodIsOtherThanGET() {
        assertTrue(executeSkipMaintenancePageForUser(true, "POST", null));
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
        assertTrue(executeShouldNotApplyMaintenancePageFilterForURI(true, "/assets/css/main.css"));
    }

    @Test
    public void shouldNotSkipMaintenancePageIfMaintenancePageIsEnabledAndRequestURIIsNotAllowedToSkipMaintenancePage() {
        assertFalse(executeShouldNotApplyMaintenancePageFilterForURI(true, "/create"));
    }
}
