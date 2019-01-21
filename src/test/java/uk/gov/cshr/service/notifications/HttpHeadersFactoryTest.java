package uk.gov.cshr.service.notifications;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SecurityContextHolder.class)
@PowerMockIgnore("javax.security.auth.*")
public class HttpHeadersFactoryTest {

    private final HttpHeadersFactory factory = new HttpHeadersFactory();

    @Test
    public void createGetRequestSetsAuthenticationHeaders() {
        mockStatic(SecurityContextHolder.class);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        OAuth2AuthenticationDetails oAuth2AuthenticationDetails = mock(OAuth2AuthenticationDetails.class);
        String tokenValue = "token-value";

        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(oAuth2AuthenticationDetails);
        when(oAuth2AuthenticationDetails.getTokenValue()).thenReturn(tokenValue);

        HttpHeaders headers = factory.create();

        assertEquals("Bearer token-value", headers.get("Authorization").get(0));
    }
}