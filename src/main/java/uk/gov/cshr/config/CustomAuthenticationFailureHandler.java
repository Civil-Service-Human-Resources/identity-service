package uk.gov.cshr.config;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import uk.gov.cshr.service.TextEncryptionService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

@Configuration
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Autowired
    TextEncryptionService textEncryptionService;

    @SneakyThrows
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String exceptionMessage = exception.getMessage();

        switch (exceptionMessage) {
            case ("User account is locked"):
                response.sendRedirect("/login?error=locked");
                break;
            case ("User account is blocked"):
                response.sendRedirect("/login?error=blocked");
                break;
            case ("User account is deactivated"):
                String username = request.getParameter("username");
                String encryptedUsername = textEncryptionService.getEncryptedText(username);

                response.sendRedirect("/login?error=deactivated&username=" + URLEncoder.encode(encryptedUsername, "UTF-8"));
                break;
            case("Pending reactivation already exists for user"):
                response.sendRedirect("/login?error=pending-reactivation");
                break;
            default:
                response.sendRedirect("/login?error=failed");
        }
    }
}
