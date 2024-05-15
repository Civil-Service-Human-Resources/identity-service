package uk.gov.cshr.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uk.gov.cshr.service.security.IdentityDetails;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Arrays;

import static java.util.Locale.ROOT;
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

        String username = request.getParameter(SKIP_MAINTENANCE_PAGE_PARAM_NAME);
        log.debug("MaintenancePageUtil.skipMaintenancePageForUser.username from request param: {}", username);

        if(isBlank(username)) {
            Principal principal = request.getUserPrincipal();
            log.debug("MaintenancePageUtil.skipMaintenancePageForUser.principal from request: {}", principal);
            username = getUsernameFromPrincipal(principal);
        }

        if(isBlank(username)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication != null ? authentication.getPrincipal() : null;
            log.debug("MaintenancePageUtil.skipMaintenancePageForUser.principal from SecurityContextHolder: {}", principal);
            username = getUsernameFromPrincipal(principal);
        }

        if(isBlank(username)) {
            log.info("MaintenancePageUtil.skipMaintenancePageForUser.username is missing.");
            String method = request.getMethod();
            if("GET".equalsIgnoreCase(method)) {
                log.info("MaintenancePageUtil.skipMaintenancePageForUser.username is missing and HTTP Method is GET. " +
                        "Returning false.");
                return false;
            } else {
                log.info("MaintenancePageUtil.skipMaintenancePageForUser.username is missing and HTTP Method is not GET. " +
                        "Returning true.");
                return true;
            }
        }

        final String trimmedUsername = username.trim();

        boolean skipMaintenancePageForUser = Arrays.stream(skipMaintenancePageForUsers.split(","))
                .anyMatch(u -> u.trim().equalsIgnoreCase(trimmedUsername));

        if(skipMaintenancePageForUser) {
            log.info("MaintenancePageUtil.skipMaintenancePageForUser.Maintenance page is skipped for the user: {}",
                    username);
        } else {
            log.info("MaintenancePageUtil.skipMaintenancePageForUser.User {} is not allowed to skip the Maintenance page.",
                    username);
        }

        return skipMaintenancePageForUser;
    }

    public boolean shouldNotApplyMaintenancePageFilterForURI(HttpServletRequest request) {
        if(!maintenancePageEnabled) {
            return true;
        }

        String requestURI = request.getRequestURI();
        boolean shouldNotApplyMaintenancePageFilterForURI = isNotBlank(requestURI)
                && Arrays.stream(skipMaintenancePageForUris.split(","))
                .anyMatch(u -> requestURI.trim().toLowerCase(ROOT)
                        .contains(u.toLowerCase(ROOT)));
        log.info("MaintenancePageUtil.shouldNotApplyMaintenancePageFilterForURI is: {} for requestURI: {}",
                shouldNotApplyMaintenancePageFilterForURI, requestURI);
        return shouldNotApplyMaintenancePageFilterForURI;
    }

    private String getUsernameFromPrincipal(Object principal) {
        if (principal instanceof IdentityDetails) {
            IdentityDetails identityDetails = (IdentityDetails) principal;
            String username = identityDetails.getIdentity().getEmail();
            log.info("MaintenancePageUtil.getUsernameFromPrincipal.username from principal: {}", username);
            return username;
        }
        return null;
    }
}
