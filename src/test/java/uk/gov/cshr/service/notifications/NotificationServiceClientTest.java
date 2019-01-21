package uk.gov.cshr.service.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.cshr.exception.NotificationException;
import uk.gov.cshr.service.EmailNotification;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceClientTest {
    private final URI uri = URI.create("http://localhost:9004");

    @Mock
    private OAuth2RestOperations restOperations;

    @Mock
    private RequestEntityFactory requestEntityFactory;

    @Mock
    private ObjectMapper objectMapper;

    private NotificationServiceClient notificationServiceClient;

    @Before
    public void setUp() {
        notificationServiceClient =
                new NotificationServiceClient(restOperations, requestEntityFactory, objectMapper, uri);
    }

    @Test
    public void shouldSendEmailNotification() throws JsonProcessingException {
        EmailNotification notification = new EmailNotification();
        String body = "{}";
        RequestEntity entity = mock(RequestEntity.class);

        when(objectMapper.writeValueAsString(notification)).thenReturn(body);
        when(requestEntityFactory.createPostRequest(uri, body)).thenReturn(entity);

        notificationServiceClient.notify(notification);
        verify(restOperations).exchange(entity, Void.class);
    }

    @Test
    public void shouldCatchJsonProcessingException() throws JsonProcessingException {
        EmailNotification notification = new EmailNotification();

        JsonProcessingException exception = mock(JsonProcessingException.class);
        doThrow(exception).when(objectMapper).writeValueAsString(notification);

        try {
            notificationServiceClient.notify(notification);
            fail("Expected NotificationException");
        } catch (NotificationException e) {
            assertEquals(exception, e.getCause());
        }
        verifyZeroInteractions(restOperations);
    }

    @Test
    public void shouldCatchHttpClientErrorException() throws JsonProcessingException {
        EmailNotification notification = new EmailNotification();
        String body = "{}";
        RequestEntity entity = mock(RequestEntity.class);

        HttpClientErrorException exception = mock(HttpClientErrorException.class);

        when(objectMapper.writeValueAsString(notification)).thenReturn(body);
        when(requestEntityFactory.createPostRequest(uri, body)).thenReturn(entity);

        doThrow(exception).when(restOperations).exchange(entity, Void.class);

        try {
            notificationServiceClient.notify(notification);
            fail("Expected NotificationException");
        } catch (NotificationException e) {
            assertEquals(exception, e.getCause());
        }

        verify(restOperations).exchange(entity, Void.class);
    }
}