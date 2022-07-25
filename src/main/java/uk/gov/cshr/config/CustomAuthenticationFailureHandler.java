package uk.gov.cshr.config;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Configuration
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

//    @Value("${textEncryption.encryptionKey}")
    String encryptionKey = "0123456789abcdef0123456789abcdef";

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
                String encryptedUsername = getEncryptedText(username);

                response.sendRedirect("/login?error=deactivated&username=" + URLEncoder.encode(encryptedUsername, "UTF-8"));
                break;
            case("Pending reactivation already exists for user"):
                response.sendRedirect("/login?error=pending-reactivation");
                break;
            default:
                response.sendRedirect("/login?error=failed");
        }
    }

    public String getEncryptedText(String rawText) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Key aesKey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);

        byte[] encrypted = cipher.doFinal(rawText.getBytes());
        String encryptedText = Base64.getEncoder().encodeToString(encrypted);
        return encryptedText;
    }
}
