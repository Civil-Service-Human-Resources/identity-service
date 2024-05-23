package uk.gov.cshr.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uk.gov.cshr.service.security.IdentityDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static java.util.Locale.ROOT;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static uk.gov.cshr.utils.TextEncryptionUtils.getDecryptedText;

@Slf4j
@Component
public class MaintenancePageUtil {

    private static final String SKIP_MAINTENANCE_PAGE_PARAM_NAME = "username";

    private final boolean maintenancePageEnabled;

    private final String skipMaintenancePageForUsers;

    private final String skipMaintenancePageForUris;

    private final String encryptionKey;

    public MaintenancePageUtil(
            @Value("${maintenancePage.enabled}") boolean maintenancePageEnabled,
            @Value("${maintenancePage.skipForUsers}") String skipMaintenancePageForUsers,
            @Value("${maintenancePage.skipForUris}") String skipMaintenancePageForUris,
            @Value("${textEncryption.encryptionKey}") String encryptionKey) {
        this.maintenancePageEnabled = maintenancePageEnabled;
        this.skipMaintenancePageForUsers = skipMaintenancePageForUsers;
        this.skipMaintenancePageForUris = skipMaintenancePageForUris;
        this.encryptionKey = encryptionKey;
    }

    public boolean skipMaintenancePageForUser(HttpServletRequest request) {
        if(!maintenancePageEnabled) {
            return true;
        }

        String username = request.getParameter(SKIP_MAINTENANCE_PAGE_PARAM_NAME);
        log.info("MaintenancePageUtil: username from request param: {}", username);

        if(isBlank(username)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication != null ? authentication.getPrincipal() : null;
            log.debug("MaintenancePageUtil: Authentication principal from SecurityContextHolder: {}", principal);
            username = getUsernameFromPrincipal(principal);
        }

        String requestURI = request.getRequestURI();

        if(isBlank(username)) {
            if("GET".equalsIgnoreCase(request.getMethod())) {
                log.info("MaintenancePageUtil: username is missing and HTTP Method is GET. " +
                        "Returning false for skipMaintenancePageForUser for requestURI {}", requestURI);
                return false;
            } else {
                log.info("MaintenancePageUtil: username is missing and HTTP Method is not GET. " +
                        "Returning true for skipMaintenancePageForUser for requestURI {}", requestURI);
                return true;
            }
        }

        final String trimmedUsername = username.trim();

        boolean skipMaintenancePageForUser = Arrays.stream(skipMaintenancePageForUsers.split(","))
                .anyMatch(u -> u.trim().equalsIgnoreCase(trimmedUsername));

        if(!skipMaintenancePageForUser) {
            //Below try catch block is for the deactivated account scenario
            try {
                final String decryptedUsername = getDecryptedText(username, encryptionKey);
                skipMaintenancePageForUser = Arrays.stream(skipMaintenancePageForUsers.split(","))
                        .anyMatch(u -> u.trim().equalsIgnoreCase(decryptedUsername));
                username = decryptedUsername; //For logging in the subsequent code below
            } catch (Exception e) {
                log.debug("MaintenancePageUtil: trimmedUsername is not encrypted.");
            }
        }

        if(skipMaintenancePageForUser) {
            log.info("MaintenancePageUtil: Maintenance page is skipped for the username {} for requestURI {}",
                    username, requestURI);
        } else {
            log.info("MaintenancePageUtil: username {} is not allowed to skip the Maintenance page for requestURI {}",
                    username, requestURI);
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
        log.debug("MaintenancePageUtil: shouldNotApplyMaintenancePageFilterForURI is {} for requestURI {}",
                shouldNotApplyMaintenancePageFilterForURI, requestURI);
        return shouldNotApplyMaintenancePageFilterForURI;
    }

    private String getUsernameFromPrincipal(Object principal) {
        if (principal instanceof IdentityDetails) {
            IdentityDetails identityDetails = (IdentityDetails) principal;
            String username = identityDetails.getIdentity().getEmail();
            log.info("MaintenancePageUtil: username from Authentication principal is {}", username);
            return username;
        }
        return null;
    }
}
