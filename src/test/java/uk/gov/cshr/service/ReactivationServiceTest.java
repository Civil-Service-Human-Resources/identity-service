package uk.gov.cshr.service;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.security.IdentityService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ReactivationServiceTest {

    @MockBean
    private IdentityRepository identityRepository;

    @MockBean
    private IdentityService identityService;

    @Captor
    private ArgumentCaptor<Identity> identityArgumentCaptor;

    @Autowired
    private ReactivationService classUnderTest;

    private Optional<Identity> optionalIdentity;

    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        Identity identity = new Identity();
        optionalIdentity = Optional.of(identity);
    }

    @Test
    public void givenAValidUid_whenProcessReactivation_thenReturnsSuccessfully() {
        // given
        when(identityRepository.findFirstByUid(anyString())).thenReturn(optionalIdentity);
        when(identityRepository.save(identityArgumentCaptor.capture())).thenReturn(new Identity());

        // when
        classUnderTest.processReactivation(request, "myuid");

        // then
        assertThat(identityArgumentCaptor.getValue().isRecentlyReactivated()).isFalse();
        verify(identityRepository, times(1)).save(eq(optionalIdentity.get()));
        verify(identityService, times(1)).updateSpringWithRecentlyReactivatedFlag(eq(request), eq(false));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void givenAnInvalidUid_whenProcessReactivation_thenThrowsResourceNotFoundExceptionAndDoesNotUpdateSpring() {
        // given
        when(identityRepository.findFirstByUid(anyString())).thenReturn(Optional.empty());

        // when
        classUnderTest.processReactivation(request, "myuid");

        // then
        verify(identityRepository, never()).save(eq(any()));
        verify(identityService, never()).updateSpringWithRecentlyReactivatedFlag(any(HttpServletRequest.class), anyBoolean());
    }

    @Test(expected = RuntimeException.class)
    public void givenAValidUidAndTechnicalError_whenProcessReactivation_thenThrowsExceptionAndDoesNotUpdateSpring() {
        // given
        when(identityRepository.save(any(Identity.class))).thenThrow(new RuntimeException());

        // when
        classUnderTest.processReactivation(request, "myuid");

        // then
        assertThat(identityArgumentCaptor.getValue().isRecentlyReactivated()).isFalse();
        verify(identityRepository, times(1)).save(eq(optionalIdentity.get()));
        verify(identityService, never()).updateSpringWithRecentlyReactivatedFlag(any(HttpServletRequest.class), anyBoolean());
    }

}
