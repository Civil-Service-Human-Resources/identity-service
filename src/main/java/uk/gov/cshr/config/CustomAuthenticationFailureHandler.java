package uk.gov.cshr.config;

import lombok.SneakyThrows;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import uk.gov.cshr.utils.TextEncryptionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @SneakyThrows
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String exceptionMessage = exception.getMessage();

        String username = request.getParameter("username");
        String encryptedUsername = TextEncryptionUtils.encryptText(username);

        switch (exceptionMessage) {
            case ("User account is locked"):
                response.sendRedirect("/login?error=locked");
                break;
            case ("User account is blocked"):
                response.sendRedirect("/login?error=blocked");
                break;
            case ("User account is deactivated"):
                response.sendRedirect("/login?error=deactivated&username=" + encryptedUsername);
                break;
            default:
                response.sendRedirect("/login?error=failed");
        }
    }
}
