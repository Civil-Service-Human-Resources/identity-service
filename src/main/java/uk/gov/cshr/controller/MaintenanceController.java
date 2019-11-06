package uk.gov.cshr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MaintenanceController {

    private final String maintenanceWindowMessage;

    @Autowired
    public MaintenanceController(@Value("${maintenance.maintenanceWindowMessage}") String maintenanceWindowMessage) {
        this.maintenanceWindowMessage = maintenanceWindowMessage;
    }

    @GetMapping("/maintenance")
    public String maintenance(Model model) {
        model.addAttribute("maintenanceWindow", maintenanceWindowMessage);
        return "maintenance";
    }
}
