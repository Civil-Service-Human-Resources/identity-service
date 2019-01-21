package uk.gov.cshr.service;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.domain.factory.InviteFactory;
import uk.gov.cshr.repository.InviteRepository;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InviteServiceTest {
    private int validityInSeconds = 99;
    private InviteService inviteService;

    @Mock
    private InviteRepository inviteRepository;

    @Mock
    private InviteFactory inviteFactory;

    @Mock
    private NotifyService notifyService;

    @Before
    public void setUp() {
        inviteService = new InviteService(notifyService, inviteRepository, inviteFactory, validityInSeconds);
    }

    @Test
    public void updateInviteByCodeShouldUpdateStatusCorrectly() {
        final String code = "123abc";

        Invite invite = new Invite();
        invite.setStatus(InviteStatus.PENDING);
        invite.setCode(code);

        when(inviteRepository.findByCode(code))
                .thenReturn(invite);

        inviteService.updateInviteByCode(code, InviteStatus.ACCEPTED);

        ArgumentCaptor<Invite> inviteArgumentCaptor = ArgumentCaptor.forClass(Invite.class);

        verify(inviteRepository).save(inviteArgumentCaptor.capture());

        invite = inviteArgumentCaptor.getValue();
        MatcherAssert.assertThat(invite.getCode(), equalTo(code));
        MatcherAssert.assertThat(invite.getStatus(), equalTo(InviteStatus.ACCEPTED));
    }

    @Test
    public void inviteCodesLessThan24HrsShouldNotBeExpired() {
        final String code = "123abc";

        Invite invite = new Invite();
        invite.setStatus(InviteStatus.PENDING);
        invite.setCode(code);
        invite.setInvitedAt(new Date(2323223232L));

        when(inviteRepository.findByCode(code))
                .thenReturn(invite);

        MatcherAssert.assertThat(inviteService.isCodeExpired(code), equalTo(true));

        ArgumentCaptor<Invite> inviteArgumentCaptor = ArgumentCaptor.forClass(Invite.class);

        verify(inviteRepository).save(inviteArgumentCaptor.capture());

        invite = inviteArgumentCaptor.getValue();
        MatcherAssert.assertThat(invite.getCode(), equalTo(code));
        MatcherAssert.assertThat(invite.getStatus(), equalTo(InviteStatus.ACCEPTED));
    }

    @Test
    public void shouldCreateInvite() {
        String email = "learner@domain.com";
        Set<Role> roleSet = new HashSet<>(Collections.singletonList(new Role()));
        Identity inviter = new Identity();

        Invite invite = new Invite();
        invite.setForEmail(email);

        when(inviteFactory.create(email, roleSet, inviter)).thenReturn(invite);

        inviteService.createNewInviteForEmailAndRoles(email, roleSet, inviter);

        verify(notifyService).sendInviteVerification(email, invite.getCode());
        verify(inviteRepository).save(invite);
    }
}