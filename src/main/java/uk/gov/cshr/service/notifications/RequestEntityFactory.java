package uk.gov.cshr.service.notifications;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class RequestEntityFactory {
    private final HttpHeadersFactory httpHeadersFactory;

    public RequestEntityFactory(HttpHeadersFactory httpHeadersFactory) {
        this.httpHeadersFactory = httpHeadersFactory;
    }

    public RequestEntity createPostRequest(URI uri, Object body) {
        HttpHeaders headers = httpHeadersFactory.create();
        return new RequestEntity(body, headers, HttpMethod.POST, uri);
    }
}