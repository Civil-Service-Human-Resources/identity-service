package uk.gov.cshr.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MaintenancePageUtilTest {

    private static final String SKIP_MAINTENANCE_PAGE_PARAM_NAME = "username";
    private final String username = "tester1@domain.com";
    private final String skipMaintenancePageForUsers = "tester1@domain.com,tester2@domain.com";
    private final String maintenancePageContentLine1 = "The learning website is undergoing scheduled maintenance.";
    private final String maintenancePageContentLine2 = "It will be unavailable between the hours of 7pm to 9pm on Wednesday 24th February 2021.";
    private final String maintenancePageContentLine3 = "Apologies for the inconvenience.";
    private final String maintenancePageContentLine4 = "If the maintenance period is extended, further information will be provided here.";

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest request;

    @Test
    public void shouldNotDisplayMaintenancePageIfDisabled() {
        MaintenancePageUtil maintenancePageUtil = new MaintenancePageUtil(false,
                skipMaintenancePageForUsers, maintenancePageContentLine1, maintenancePageContentLine2,
                maintenancePageContentLine3,maintenancePageContentLine4);

        assertFalse(maintenancePageUtil.displayMaintenancePage(request, model));
        verify(request, times(1)).getParameter(SKIP_MAINTENANCE_PAGE_PARAM_NAME);
        verify(model, times(0)).addAttribute("maintenancePageContentLine1", maintenancePageContentLine1);
        verify(model, times(0)).addAttribute("maintenancePageContentLine2", maintenancePageContentLine2);
        verify(model, times(0)).addAttribute("maintenancePageContentLine3", maintenancePageContentLine3);
        verify(model, times(0)).addAttribute("maintenancePageContentLine4", maintenancePageContentLine4);
    }

    @Test
    public void shouldNotDisplayMaintenancePageIfEnabledButSkippedForUser() {
        MaintenancePageUtil maintenancePageUtil = new MaintenancePageUtil(true,
                skipMaintenancePageForUsers, maintenancePageContentLine1, maintenancePageContentLine2,
                maintenancePageContentLine3,maintenancePageContentLine4);

        when(request.getParameter("username")).thenReturn(username);

        assertFalse(maintenancePageUtil.displayMaintenancePage(request, model));
        verify(request, times(1)).getParameter(SKIP_MAINTENANCE_PAGE_PARAM_NAME);
        verify(model, times(0)).addAttribute("maintenancePageContentLine1", maintenancePageContentLine1);
        verify(model, times(0)).addAttribute("maintenancePageContentLine2", maintenancePageContentLine2);
        verify(model, times(0)).addAttribute("maintenancePageContentLine3", maintenancePageContentLine3);
        verify(model, times(0)).addAttribute("maintenancePageContentLine4", maintenancePageContentLine4);
    }

    @Test
    public void shouldDisplayMaintenancePageIfEnabled() {
        MaintenancePageUtil maintenancePageUtil = new MaintenancePageUtil(true,
                skipMaintenancePageForUsers, maintenancePageContentLine1, maintenancePageContentLine2,
                maintenancePageContentLine3,maintenancePageContentLine4);

        assertTrue(maintenancePageUtil.displayMaintenancePage(request, model));
        verify(request, times(1)).getParameter(SKIP_MAINTENANCE_PAGE_PARAM_NAME);
        verify(model, times(1)).addAttribute("maintenancePageContentLine1", maintenancePageContentLine1);
        verify(model, times(1)).addAttribute("maintenancePageContentLine2", maintenancePageContentLine2);
        verify(model, times(1)).addAttribute("maintenancePageContentLine3", maintenancePageContentLine3);
        verify(model, times(1)).addAttribute("maintenancePageContentLine4", maintenancePageContentLine4);
    }
}
