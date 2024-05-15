package uk.gov.cshr.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.utils.MaintenancePageUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Configuration
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    private final MaintenancePageUtil maintenancePageUtil;

    public CustomAuthenticationSuccessHandler(MaintenancePageUtil maintenancePageUtil) {
        this.maintenancePageUtil = maintenancePageUtil;
    }

    @SneakyThrows
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        this.setDefaultTargetUrl(lpgUiUrl);
        Object principal = authentication.getPrincipal();
        if (principal instanceof IdentityDetails) {
            IdentityDetails identityDetails = (IdentityDetails) principal;
            Identity identity = identityDetails.getIdentity();
            maintenancePageUtil.skipMaintenancePageCheck(identity.getEmail());
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
