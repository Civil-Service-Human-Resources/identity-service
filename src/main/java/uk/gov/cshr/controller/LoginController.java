package uk.gov.cshr.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.cshr.utils.MaintenancePageUtil;

@Slf4j
@Controller
public class LoginController {

  @Value("${lpg.uiUrl}")
  private String lpgUiUrl;

  private final MaintenancePageUtil maintenancePageUtil;

  public LoginController(MaintenancePageUtil maintenancePageUtil) {
    this.maintenancePageUtil = maintenancePageUtil;
  }

  @RequestMapping("/login")
  public String login(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {

    if(maintenancePageUtil.displayMaintenancePage(request, model)) {
      return "maintenance";
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
