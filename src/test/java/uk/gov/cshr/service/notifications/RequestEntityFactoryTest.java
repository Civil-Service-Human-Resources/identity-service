package uk.gov.cshr.service.notifications;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestEntityFactoryTest {

    @Mock
    private HttpHeadersFactory httpHeadersFactory;

    @InjectMocks
    private RequestEntityFactory requestEntityFactory;

    @Test
    public void shouldReturnPostRequestEntity() {
        URI uri = URI.create("http://localhost");
        String body = "{}";

        HttpHeaders headers = new HttpHeaders();
        headers.add("header1", "value1");
        when(httpHeadersFactory.create()).thenReturn(headers);

        RequestEntity requestEntity = requestEntityFactory.createPostRequest(uri, body);

        assertEquals(headers, requestEntity.getHeaders());
        assertEquals(uri, requestEntity.getUrl());
        assertEquals(body, requestEntity.getBody());
    }
}