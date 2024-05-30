package uk.gov.cshr.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping
public class MaintenancePageController {

    private static final String MAINTENANCE_TEMPLATE = "maintenance";

    private final String lpgUiUrl;

    private final boolean maintenancePageEnabled;

    private final String maintenancePageContentLine1;

    private final String maintenancePageContentLine2;

    private final String maintenancePageContentLine3;

    private final String maintenancePageContentLine4;

    public MaintenancePageController(@Value("${lpg.uiUrl}") String lpgUiUrl,
                                     @Value("${maintenancePage.enabled}") boolean maintenancePageEnabled,
                                     @Value("${maintenancePage.contentLine1}") String maintenancePageContentLine1,
                                     @Value("${maintenancePage.contentLine2}") String maintenancePageContentLine2,
                                     @Value("${maintenancePage.contentLine3}") String maintenancePageContentLine3,
                                     @Value("${maintenancePage.contentLine4}") String maintenancePageContentLine4) {
        this.lpgUiUrl = lpgUiUrl;
        this.maintenancePageEnabled = maintenancePageEnabled;
        this.maintenancePageContentLine1 = maintenancePageContentLine1;
        this.maintenancePageContentLine2 = maintenancePageContentLine2;
        this.maintenancePageContentLine3 = maintenancePageContentLine3;
        this.maintenancePageContentLine4 = maintenancePageContentLine4;
    }

    @GetMapping("/maintenance")
    public String maintenancePage(Model model, HttpServletResponse response) throws IOException {
        if(!maintenancePageEnabled) {
            response.sendRedirect(lpgUiUrl);
        }
        model.addAttribute("maintenancePageContentLine1", maintenancePageContentLine1);
        model.addAttribute("maintenancePageContentLine2", maintenancePageContentLine2);
        model.addAttribute("maintenancePageContentLine3", maintenancePageContentLine3);
        model.addAttribute("maintenancePageContentLine4", maintenancePageContentLine4);
        return MAINTENANCE_TEMPLATE;
    }
}
