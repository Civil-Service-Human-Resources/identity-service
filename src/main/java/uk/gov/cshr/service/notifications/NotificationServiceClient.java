package uk.gov.cshr.service.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.stereotype.Service;
import uk.gov.cshr.exception.NotificationException;
import uk.gov.cshr.service.EmailNotification;

import java.net.URI;

@Service
public class NotificationServiceClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceClient.class);

    private final OAuth2RestOperations restOperations;
    private final RequestEntityFactory requestEntityFactory;
    private final ObjectMapper objectMapper;
    private final URI url;

    public NotificationServiceClient(OAuth2RestOperations restOperations,
                                     RequestEntityFactory requestEntityFactory,
                                     ObjectMapper objectMapper,
                                     @Value("${notifications.url}") URI url) {
        this.restOperations = restOperations;
        this.requestEntityFactory = requestEntityFactory;
        this.objectMapper = objectMapper;
        this.url = url;
    }

    public void notify(EmailNotification notification)  {
        try {
            String body = objectMapper.writeValueAsString(notification);
            RequestEntity requestEntity = requestEntityFactory.createPostRequest(url, body);
            restOperations.exchange(requestEntity, Void.class);

            LOGGER.debug("Notification sent {}", notification);
        } catch (Exception e) {
            throw new NotificationException(e);
        }
    }
}
