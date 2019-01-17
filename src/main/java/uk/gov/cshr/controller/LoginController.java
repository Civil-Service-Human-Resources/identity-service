package uk.gov.cshr.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class LoginController {

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @RequestMapping("/login")
    public String login( HttpServletRequest request, HttpServletResponse response) throws IOException {
        return "login";
    }

    @RequestMapping("/management/login")
    public String managementLogin() {
        return "management-login";
    }
}