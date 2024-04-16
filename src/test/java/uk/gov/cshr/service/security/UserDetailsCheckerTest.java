package uk.gov.cshr.service.security;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.AccountBlockedException;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.csrs.CsrsService;

import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserDetailsCheckerTest {

    private static final String EMAIL_ADDRESS = "test@example.com";
    private static final String DOMAIN = "example.com";
    private static final String UID = "UID";

    @Mock
    private CsrsService csrsService;

    @Mock
    private InviteService inviteService;

    @InjectMocks
    private UserDetailsChecker userDetailsChecker;

    @Test
    public void shouldNotThrowExceptionIfEmailIsallowlisted() {
        Identity identity = new Identity();
        identity.setLocked(false);
        identity.setEmail(EMAIL_ADDRESS);
        UserDetails userDetails = new IdentityDetails(identity);

        when(csrsService.isDomainAllowlisted(DOMAIN)).thenReturn(true);
        Assertions.assertThatCode(() -> userDetailsChecker.check(userDetails))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldNotThrowExceptionIfEmailIsInAgency() {
        Identity identity = new Identity();
        identity.setLocked(false);
        identity.setEmail(EMAIL_ADDRESS);
        identity.setAgencyTokenUid(UID);
        UserDetails userDetails = new IdentityDetails(identity);

        when(csrsService.isAgencyTokenUidValidForDomain(UID, DOMAIN)).thenReturn(true);

        Assertions.assertThatCode(() -> userDetailsChecker.check(userDetails))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldNotThrowExceptionIfEmailIsInvited() {
        Identity identity = new Identity();
        identity.setLocked(false);
        identity.setEmail(EMAIL_ADDRESS);
        UserDetails userDetails = new IdentityDetails(identity);

        when(inviteService.isEmailInvited(EMAIL_ADDRESS)).thenReturn(true);

        Assertions.assertThatCode(() -> userDetailsChecker.check(userDetails))
                .doesNotThrowAnyException();
    }

    @Test(expected = AccountBlockedException.class)
    public void shouldThrowExceptionIfEmailIsAgencyButNoTokensMatch() {
        Identity identity = new Identity();
        identity.setLocked(false);
        identity.setEmail(EMAIL_ADDRESS);
        identity.setAgencyTokenUid(UID);
        UserDetails userDetails = new IdentityDetails(identity);

        when(csrsService.isAgencyTokenUidValidForDomain(UID, DOMAIN)).thenReturn(false);
        when(inviteService.isEmailInvited(EMAIL_ADDRESS)).thenReturn(false);

        userDetailsChecker.check(userDetails);
    }

    @Test(expected = AccountBlockedException.class)
    public void shouldThrowExceptionIfEmailIsNotAllowlisted() {
        Identity identity = new Identity();
        identity.setLocked(false);
        identity.setEmail(EMAIL_ADDRESS);
        UserDetails userDetails = new IdentityDetails(identity);

        when(csrsService.isDomainAllowlisted(DOMAIN)).thenReturn(false);
        when(inviteService.isEmailInvited(EMAIL_ADDRESS)).thenReturn(false);

        userDetailsChecker.check(userDetails);
    }
}
