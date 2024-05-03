package uk.gov.cshr.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.security.IdentityDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Slf4j
@Configuration
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${lpg.uiUrl}")
    private String lpgUiSignOutUrl;

    @Value("${maintenancePage.enabled}")
    private boolean maintenancePageEnabled;

    @Value("${maintenancePage.skipForUsers}")
    private String skipMaintenancePageForUsers;

    @SneakyThrows
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
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
                    log.info("Trying to logout the user to Display Maintenance page for the user: {}", username);
                    response.sendRedirect(lpgUiSignOutUrl+"/sign-out");
                    log.info("Redirecting the user {} to lpg-ui/sign-out url to logout.", username);
                }
            }
        }
    }
}
