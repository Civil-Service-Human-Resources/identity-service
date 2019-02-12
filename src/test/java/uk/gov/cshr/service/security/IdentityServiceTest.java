package uk.gov.cshr.service.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.domain.Token;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.TokenRepository;
import uk.gov.cshr.service.CSRSService;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.MessageService;
import uk.gov.cshr.service.NotifyService;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IdentityServiceTest {

    private final String updatePasswordEmailTemplateId = "template-id";

    private IdentityService identityService;

    @Mock
    private IdentityRepository identityRepository;

    @Mock
    private InviteService inviteService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenServices tokenServices;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private NotifyService notifyService;

    @Mock
    private LearnerRecordService learnerRecordService;

    @Mock
    private CSRSService csrsService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MessageService messageService;

    @Before
    public void setUp() throws Exception {
        identityService = new IdentityService(
                updatePasswordEmailTemplateId,
                identityRepository,
                passwordEncoder,
                tokenServices,
                tokenRepository,
                notifyService,
                learnerRecordService,
                csrsService,
                notificationService,
                messageService
        );

        identityService.setInviteService(inviteService);
    }

    @Test
    public void shouldLoadIdentityByEmailAddress() {

        final String emailAddress = "test@example.org";
        final String uid = "uid";
        final Identity identity = new Identity(uid, emailAddress, "password", true, false, emptySet(), Instant.now());

        when(identityRepository.findFirstByActiveTrueAndEmailEquals(emailAddress))
                .thenReturn(identity);

        IdentityDetails identityDetails = (IdentityDetails) identityService.loadUserByUsername(emailAddress);

        assertThat(identityDetails, notNullValue());
        assertThat(identityDetails.getUsername(), equalTo(uid));
        assertThat(identityDetails.getIdentity(), equalTo(identity));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void shouldThrowErrorWhenNoClientFound() {

        final String emailAddress = "test@example.org";

        when(identityRepository.findFirstByActiveTrueAndEmailEquals(emailAddress))
                .thenReturn(null);

        identityService.loadUserByUsername(emailAddress);
    }

    @Test
    public void shouldReturnTrueWhenInvitingAnExistingUser() {
        final String emailAddress = "test@example.org";

        when(identityRepository.existsByEmail(emailAddress))
                .thenReturn(true);

        assertThat(identityService.existsByEmail("test@example.org"), equalTo(true));
    }

    @Test
    public void shouldReturnFalseWhenInvitingAnNonExistingUser() {
        assertThat(identityService.existsByEmail("test2@example.org"), equalTo(false));
    }

    @Test
    public void createIdentityFromInviteCode() {
        final String code = "123abc";
        final String email = "test@example.com";
        Role role = new Role();
        role.setName("USER");

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        Invite invite = new Invite();
        invite.setCode(code);
        invite.setForEmail(email);
        invite.setForRoles(roles);

        when(inviteService.findByCode(code)).thenReturn(invite);

        when(passwordEncoder.encode("password")).thenReturn("password");

        identityService.setInviteService(inviteService);

        identityService.createIdentityFromInviteCode(code, "password");

        ArgumentCaptor<Identity> inviteArgumentCaptor = ArgumentCaptor.forClass(Identity.class);

        verify(identityRepository).save(inviteArgumentCaptor.capture());

        Identity identity = inviteArgumentCaptor.getValue();
        assertThat(identity.getRoles().contains(role), equalTo(true));
        assertThat(identity.getPassword(), equalTo("password"));
        assertThat(identity.getEmail(), equalTo("test@example.com"));
    }

    @Test
    public void lockIdentitySetsLockedToTrue() {
        String email = "test-email";
        Identity identity = mock(Identity.class);
        when(identityRepository.findFirstByActiveTrueAndEmailEquals(email)).thenReturn(identity);

        identityService.lockIdentity(email);

        InOrder inOrder = inOrder(identity, identityRepository);

        inOrder.verify(identity).setLocked(true);
        inOrder.verify(identityRepository).save(identity);
    }

    @Test
    public void shouldRevokeAccessTokensForUser() {
        String uid = "_uid";
        Identity identity = mock(Identity.class);
        when(identity.getUid()).thenReturn(uid);

        String accessToken1Value = "token1-value";
        OAuth2AccessToken accessToken1 = mock(OAuth2AccessToken.class);
        when(accessToken1.getValue()).thenReturn(accessToken1Value);
        Token token1 = mock(Token.class);
        when(token1.getToken()).thenReturn(accessToken1);

        String accessToken2Value = "token2-value";
        OAuth2AccessToken accessToken2 = mock(OAuth2AccessToken.class);
        when(accessToken2.getValue()).thenReturn(accessToken2Value);
        Token token2 = mock(Token.class);
        when(token2.getToken()).thenReturn(accessToken2);

        when(tokenRepository.findAllByUserName(uid)).thenReturn(Arrays.asList(token1, token2));

        identityService.revokeAccessTokens(identity);

        verify(tokenServices).revokeToken(accessToken1Value);
        verify(tokenServices).revokeToken(accessToken2Value);
    }

    @Test
    public void shouldUpdatePasswordAndRevokeTokens() {
        String password = "_password";
        String encodedPassword = "encoded-password";
        String email = "learner@domain.com";
        String uid = "_uid";
        Identity identity = mock(Identity.class);
        when(identity.getUid()).thenReturn(uid);
        when(identity.getEmail()).thenReturn(email);

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        String accessToken1Value = "token1-value";
        OAuth2AccessToken accessToken1 = mock(OAuth2AccessToken.class);
        when(accessToken1.getValue()).thenReturn(accessToken1Value);
        Token token1 = mock(Token.class);
        when(token1.getToken()).thenReturn(accessToken1);

        String accessToken2Value = "token2-value";
        OAuth2AccessToken accessToken2 = mock(OAuth2AccessToken.class);
        when(accessToken2.getValue()).thenReturn(accessToken2Value);
        Token token2 = mock(Token.class);
        when(token2.getToken()).thenReturn(accessToken2);

        when(tokenRepository.findAllByUserName(uid)).thenReturn(Arrays.asList(token1, token2));

        identityService.updatePasswordAndRevokeTokens(identity, password);

        InOrder inOrder = inOrder(identity, identityRepository);

        inOrder.verify(identity).setPassword(encodedPassword);
        inOrder.verify(identityRepository).save(identity);

        verify(tokenServices).revokeToken(accessToken1Value);
        verify(tokenServices).revokeToken(accessToken2Value);

        verify(notifyService).notify(email, updatePasswordEmailTemplateId);
    }

    @Test
    public void shouldDeleteIdentityAndLearnerRecordAndCSRSProfile() {
        String uid = "identity-uid";
        Identity identity = new Identity();

        when(learnerRecordService.deleteCivilServant(uid)).thenReturn(ResponseEntity.noContent().build());
        when(csrsService.deleteCivilServant(uid)).thenReturn(ResponseEntity.noContent().build());
        when(identityRepository.findFirstByUid(uid)).thenReturn(Optional.of(identity));

        identityService.deleteIdentity(uid);

        verify(learnerRecordService).deleteCivilServant(uid);
        verify(csrsService).deleteCivilServant(uid);
        verify(identityRepository).findFirstByUid(uid);
        verify(inviteService).deleteInvitesByIdentity(identity);
        verify(identityRepository).delete(identity);
    }

    @Test
    public void shouldOnlyDeleteLearningRecordAndCSRSProfileIfNoIdentityIsFound() {
        String uid = "identity-uid";

        when(learnerRecordService.deleteCivilServant(uid)).thenReturn(ResponseEntity.noContent().build());
        when(csrsService.deleteCivilServant(uid)).thenReturn(ResponseEntity.noContent().build());
        when(identityRepository.findFirstByUid(uid)).thenReturn(Optional.empty());

        identityService.deleteIdentity(uid);

        verify(learnerRecordService).deleteCivilServant(uid);
        verify(csrsService).deleteCivilServant(uid);
        verify(identityRepository).findFirstByUid(uid);
    }

    @Test
    public void shouldUpdateLastLoggedIn() {
        Identity identity = new Identity();
        Instant lastLoggedIn = Instant.now();

        when(identityRepository.save(identity)).thenReturn(identity);

        assertEquals(identityService.setLastLoggedIn(lastLoggedIn, identity), identity);
        assertEquals(identity.getLastLoggedIn(), lastLoggedIn);

        verify(identityRepository).save(identity);
    }

    @Test
    public void shouldDeactivateInactiveAccounts() {
        Identity activeIdentity = new Identity();
        activeIdentity.setLastLoggedIn(Instant.now());
        activeIdentity.setActive(true);

        Identity inactiveIdentity = new Identity();
        inactiveIdentity.setLastLoggedIn(LocalDateTime.now().minusMonths(14).toInstant(ZoneOffset.UTC));
        inactiveIdentity.setActive(true);

        ArrayList<Identity> identities = new ArrayList<>();
        identities.add(activeIdentity);
        identities.add(inactiveIdentity);

        when(identityRepository.findAll()).thenReturn(identities);
        when(identityRepository.save(inactiveIdentity)).thenReturn(inactiveIdentity);

        identityService.trackUserActivity();

        assertFalse(inactiveIdentity.isActive());
        assertTrue(activeIdentity.isActive());

        verify(identityRepository).findAll();
        verify(identityRepository).save(inactiveIdentity);
    }

    @Test
    public void shouldDeleteInactiveAccounts() {
        Identity activeIdentity = new Identity();
        activeIdentity.setLastLoggedIn(Instant.now());
        activeIdentity.setActive(true);

        Identity inactiveIdentity = new Identity(
                "test-uid",
                "",
                "",
                false,
                false,
                new HashSet<>(),
                LocalDateTime.now().minusMonths(27).toInstant(ZoneOffset.UTC));

        ArrayList<Identity> identities = new ArrayList<>();
        identities.add(activeIdentity);
        identities.add(inactiveIdentity);

        when(identityRepository.findAll()).thenReturn(identities);
        when(learnerRecordService.deleteCivilServant("test-uid")).thenReturn(ResponseEntity.noContent().build());
        when(csrsService.deleteCivilServant("test-uid")).thenReturn(ResponseEntity.noContent().build());
        when(identityRepository.findFirstByUid("test-uid")).thenReturn(Optional.of(inactiveIdentity));

        identityService.trackUserActivity();

        assertFalse(inactiveIdentity.isActive());
        assertTrue(activeIdentity.isActive());

        verify(identityRepository).findAll();
        verify(learnerRecordService).deleteCivilServant("test-uid");
        verify(csrsService).deleteCivilServant("test-uid");
        verify(identityRepository).findFirstByUid("test-uid");
        verify(inviteService).deleteInvitesByIdentity(inactiveIdentity);
        verify(identityRepository).delete(inactiveIdentity);
    }
}
