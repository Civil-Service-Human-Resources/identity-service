package uk.gov.cshr.controller;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Controller
public class LoginController {

  private static final String SKIP_MAINTENANCE_PAGE_PARAM_NAME = "username";

  @Value("${lpg.uiUrl}")
  private String lpgUiUrl;

  @Value("${maintenancePage.enabled}")
  private boolean maintenancePageEnabled;

  @Value("${maintenancePage.contentLine1}")
  private String maintenancePageContentLine1;

  @Value("${maintenancePage.contentLine2}")
  private String maintenancePageContentLine2;

  @Value("${maintenancePage.contentLine3}")
  private String maintenancePageContentLine3;

  @Value("${maintenancePage.contentLine4}")
  private String maintenancePageContentLine4;

  @Value("${maintenancePage.skipForUsers}")
  private String skipMaintenancePageForUsers;

  @RequestMapping("/login")
  public String login(HttpServletRequest request, HttpServletResponse response) throws IOException {

    if(maintenancePageEnabled) {
      String skipMaintenancePageForUser = request.getParameter(SKIP_MAINTENANCE_PAGE_PARAM_NAME);

      boolean skipMaintenancePage = isNotBlank(skipMaintenancePageForUser) &&
              Arrays.stream(skipMaintenancePageForUsers.split(","))
                      .anyMatch(u -> u.trim().equalsIgnoreCase(skipMaintenancePageForUser.trim()));

      if (!skipMaintenancePage) {
        request.setAttribute("maintenancePageContentLine1", maintenancePageContentLine1);
        request.setAttribute("maintenancePageContentLine2", maintenancePageContentLine2);
        request.setAttribute("maintenancePageContentLine3", maintenancePageContentLine3);
        request.setAttribute("maintenancePageContentLine4", maintenancePageContentLine4);
        return "maintenance";
      }
      log.info("Maintenance page is skipped for the user: {}", skipMaintenancePageForUser);
    }

    DefaultSavedRequest dsr =
        (DefaultSavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
    if (dsr != null && dsr.getQueryString() == null) {
      response.sendRedirect(lpgUiUrl);
    }
    return "login";
  }

  @RequestMapping("/management/login")
  public String managementLogin() {
    return "management-login";
  }
}
