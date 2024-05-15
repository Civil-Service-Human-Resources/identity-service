package uk.gov.cshr.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.cshr.exception.GenericServerException;
import uk.gov.cshr.service.security.IdentityDetails;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Slf4j
@Component
public class MaintenancePageUtil {

    private static final String SKIP_MAINTENANCE_PAGE_PARAM_NAME = "username";

    private final boolean maintenancePageEnabled;

    private final String skipMaintenancePageForUsers;

    private final String skipMaintenancePageForUris;

    public MaintenancePageUtil(
            @Value("${maintenancePage.enabled}") boolean maintenancePageEnabled,
            @Value("${maintenancePage.skipForUsers}") String skipMaintenancePageForUsers,
            @Value("${maintenancePage.skipForUris}") String skipMaintenancePageForUris) {
        this.maintenancePageEnabled = maintenancePageEnabled;
        this.skipMaintenancePageForUsers = skipMaintenancePageForUsers;
        this.skipMaintenancePageForUris = skipMaintenancePageForUris;
    }

    public boolean skipMaintenancePageForUser(HttpServletRequest request) {
        if(!maintenancePageEnabled) {
            return true;
        }

        String method = request.getMethod();
        if(!"GET".equalsIgnoreCase(method)) {
            log.info("MaintenancePageUtil.skipMaintenancePageForUser.method is not GET returning true.");
            return true;
        }

        String username = request.getParameter(SKIP_MAINTENANCE_PAGE_PARAM_NAME);

        if(isBlank(username)) {
            Principal principal = request.getUserPrincipal();
            if (principal instanceof IdentityDetails) {
                IdentityDetails identityDetails = (IdentityDetails) principal;
                username = identityDetails.getIdentity().getEmail();
            }
        }

        if(isBlank(username)) {
            return false;
        }

        final String trimmedUsername = username.trim();

        boolean skipMaintenancePageForUser = Arrays.stream(skipMaintenancePageForUsers.split(","))
                .anyMatch(u -> u.trim().equalsIgnoreCase(trimmedUsername));

        if(skipMaintenancePageForUser) {
            log.info("MaintenancePageUtil.skipMaintenancePageForUser.Maintenance page is skipped for the user: {}",
                    username);
        }

        return skipMaintenancePageForUser;
    }

    public void skipMaintenancePageCheck(String email) {
        if(!maintenancePageEnabled) {
            return;
        }

        boolean skipMaintenancePage = Arrays.stream(skipMaintenancePageForUsers.split(","))
                .anyMatch(u -> u.trim().equalsIgnoreCase(email.trim()));

        if(skipMaintenancePage) {
            log.info("MaintenancePageUtil.skipMaintenancePageCheck.Maintenance page is skipped for the user: {}",
                    email);
            return;
        }

        log.warn("MaintenancePageUtil.skipMaintenancePageCheck." +
                "User is not allowed to access the website due to maintenance page is enabled. " +
                "Showing error page to the user: {}", email);
        throw new GenericServerException("User is not allowed to access the website due to maintenance page is enabled.");
    }

    public boolean shouldNotApplyMaintenancePageFilterForURI(HttpServletRequest request) {
        if(!maintenancePageEnabled) {
            return true;
        }

        String requestURI = request.getRequestURI();
        boolean shouldNotApplyMaintenancePageFilterForURI = isNotBlank(requestURI)
                && Arrays.stream(skipMaintenancePageForUris.split(","))
                .anyMatch(u -> u.trim().equalsIgnoreCase(requestURI.trim()));
        log.info("MaintenancePageUtil.shouldNotApplyMaintenancePageFilterForURI is: {} for requestURI: {}",
                shouldNotApplyMaintenancePageFilterForURI, requestURI);
        return shouldNotApplyMaintenancePageFilterForURI;
    }
}
