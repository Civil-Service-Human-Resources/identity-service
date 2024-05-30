package uk.gov.cshr.filter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.utils.MaintenancePageUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MaintenancePageFilterTest {

    @Autowired
    private MaintenancePageFilter maintenancePageFilter;

    @MockBean
    private MaintenancePageUtil maintenancePageUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain mockFilterChain;

    @Test
    public void shouldNotRedirectToMaintenancePageWhenSkipMaintenancePageForUserIsTrue() throws ServletException, IOException {
        when(maintenancePageUtil.skipMaintenancePageForUser(request)).thenReturn(true);
        maintenancePageFilter.doFilterInternal(request, response, mockFilterChain);
        verify(response, times(0)).sendRedirect("/maintenance");
        maintenancePageFilter.destroy();
    }

    @Test
    public void shouldRedirectToMaintenancePageWhenSkipMaintenancePageForUserIsFalse() throws ServletException, IOException {
        when(maintenancePageUtil.skipMaintenancePageForUser(request)).thenReturn(false);
        maintenancePageFilter.doFilterInternal(request, response, mockFilterChain);
        verify(response, times(1)).sendRedirect("/maintenance");
        maintenancePageFilter.destroy();
    }

    @Test
    public void shouldNotFilterWhenShouldNotApplyFilterForURIIsTrue() throws ServletException {
        when(maintenancePageUtil.shouldNotApplyMaintenancePageFilterForURI(request)).thenReturn(true);
        assertTrue(maintenancePageFilter.shouldNotFilter(request));
        maintenancePageFilter.destroy();
    }

    @Test
    public void shouldFilterWhenShouldNotApplyFilterForURIIsFalse() throws ServletException {
        when(maintenancePageUtil.shouldNotApplyMaintenancePageFilterForURI(request)).thenReturn(false);
        assertFalse(maintenancePageFilter.shouldNotFilter(request));
        maintenancePageFilter.destroy();
    }
}
