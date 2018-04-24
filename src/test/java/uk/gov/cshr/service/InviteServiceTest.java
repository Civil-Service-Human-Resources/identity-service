package uk.gov.cshr.service;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.Status;
import uk.gov.cshr.repository.InviteRepository;

import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InviteServiceTest {

    @InjectMocks
    private InviteService inviteService;

    @Mock
    private InviteRepository inviteRepository;

    @Mock
    private Invite invite;

    @Test
    public void hasEmailBeenInvitedBeforeShouldReturnFalseIfNotInInviteRepo() {
        final String emailAddress = "test@example.org";

        assertThat(inviteService.hasEmailBeenInvitedBefore(emailAddress), equalTo(false));
    }

    @Test
    public void hasEmailBeenInvitedBeforeShouldReturnTrueIfInInviteRepo() {
        final String emailAddress = "test@example.org";

        Invite invite = new Invite();
        invite.setForEmail(emailAddress);

        when(inviteRepository.findByForEmail(emailAddress))
                .thenReturn(invite);

        assertThat(inviteService.hasEmailBeenInvitedBefore(emailAddress), equalTo(true));
    }

    @Test
    public void updateInviteByCodeShouldUpdateStatusCorrectly() {
        final String code = "123abc";

        Invite invite = new Invite();
        invite.setStatus(Status.PENDING);
        invite.setCode(code);

        when(inviteRepository.findByCode(code))
                .thenReturn(invite);

        inviteService.updateInviteByCode(code, Status.ACCEPTED);

        ArgumentCaptor<Invite> inviteArgumentCaptor = ArgumentCaptor.forClass(Invite.class);

        verify(inviteRepository).save(inviteArgumentCaptor.capture());

        invite = inviteArgumentCaptor.getValue();
        MatcherAssert.assertThat(invite.getCode(), equalTo(code));
        MatcherAssert.assertThat(invite.getStatus(), equalTo(Status.ACCEPTED));
    }

    @Test
    public void inviteCodesLessThan24HrsShouldNotBeExpired() {
        final String code = "123abc";

        Invite invite = new Invite();
        invite.setStatus(Status.PENDING);
        invite.setCode(code);
        invite.setInvitedAt(new Date(2323223232L));


        when(inviteRepository.findByCode(code))
                .thenReturn(invite);

        MatcherAssert.assertThat(inviteService.isCodeExpired(code), equalTo(true));

        ArgumentCaptor<Invite> inviteArgumentCaptor = ArgumentCaptor.forClass(Invite.class);

        verify(inviteRepository).save(inviteArgumentCaptor.capture());

        invite = inviteArgumentCaptor.getValue();
        MatcherAssert.assertThat(invite.getCode(), equalTo(code));
        MatcherAssert.assertThat(invite.getStatus(), equalTo(Status.ACCEPTED));
    }
}