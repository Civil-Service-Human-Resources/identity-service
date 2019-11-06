package uk.gov.cshr.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class MaintenanceFilter implements Filter {

    private static final String MAINTENANCE_URI = "/maintenance";
    private static final String MAIN_URI = "/";
    private final boolean maintenanceEnabled;
    private final String maintenanceOverrideTokenName;
    private final String maintenanceOverrideTokenValue;
    private static final String ASSETS_PATH = "/assets";

    @Autowired
    public MaintenanceFilter(@Value("${maintenance.enabled}") boolean maintenanceEnabled,
                             @Value("${maintenance.overrideTokenName}") String maintenanceOverrideTokenName,
                             @Value("${maintenance.overrideTokenValue}") String maintenanceOverrideTokenValue) {
        this.maintenanceEnabled = maintenanceEnabled;
        this.maintenanceOverrideTokenName = maintenanceOverrideTokenName;
        this.maintenanceOverrideTokenValue = maintenanceOverrideTokenValue;

    }

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (shouldRedirectToMaintenancePage(request)) {
            httpResponse.sendRedirect(MAINTENANCE_URI);
            return;
        }

        if (shouldRedirectToMainPage(request)) {
            httpResponse.sendRedirect(MAIN_URI);
            return;
        }

        chain.doFilter(request, response);
    }


    @Override
    public void destroy() {

    }

    private boolean shouldRedirectToMaintenancePage(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        return maintenanceEnabled &&
                !isMaintenaceRequest(httpRequest) &&
                !isAssetsRequest(httpRequest) &&
                !isMaintenanceOverrideCookiePresent(httpRequest);
    }

    private boolean shouldRedirectToMainPage(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        return !maintenanceEnabled &&
                isMaintenaceRequest(httpRequest);
    }

    private boolean isMaintenaceRequest(HttpServletRequest httpRequest) {
        return httpRequest.getRequestURI().equalsIgnoreCase(MAINTENANCE_URI);
    }

    private boolean isAssetsRequest(HttpServletRequest httpRequest) {
        return httpRequest.getRequestURI().startsWith(ASSETS_PATH);
    }

    private boolean isMaintenanceOverrideCookiePresent(HttpServletRequest httpRequest) {
        if (httpRequest.getCookies() != null) {
            for (Cookie cookie : httpRequest.getCookies()) {
                if (isMaintainanceOverrideCookieProvided(cookie)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isMaintainanceOverrideCookieProvided(Cookie cookie) {
        return cookie.getName().equalsIgnoreCase(maintenanceOverrideTokenName) &&
                cookie.getValue().equals(maintenanceOverrideTokenValue);
    }
}
