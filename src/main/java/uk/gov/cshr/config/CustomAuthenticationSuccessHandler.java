package uk.gov.cshr.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.security.IdentityDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Slf4j
@Configuration
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @Value("${maintenancePage.enabled}")
    private boolean maintenancePageEnabled;

    @Value("${maintenancePage.skipForUsers}")
    private String skipMaintenancePageForUsers;

    @SneakyThrows
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        this.setDefaultTargetUrl(lpgUiUrl);
        if(maintenancePageEnabled) {
            Object principal = authentication.getPrincipal();
            if(principal instanceof IdentityDetails) {
                IdentityDetails identityDetails = (IdentityDetails)principal;
                Identity identity = identityDetails.getIdentity();
                String username = identity.getEmail();
                boolean skipMaintenancePage = Arrays.stream(skipMaintenancePageForUsers.split(","))
                        .anyMatch(u -> u.trim().equalsIgnoreCase(username.trim()));
                if(skipMaintenancePage) {
                    log.info("Maintenance page is skipped for the user: {}", username);
                } else {
                    log.warn("User is not allowed to access the website due to maintenance page is enabled. Showing error page for the user: {}", username);
                    throw new RuntimeException("User is not allowed to access the website due to maintenance page is enabled.");
                }
            }
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
