package uk.gov.cshr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.controller.AuthenticationController;

@Service
public class CSRSService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    private final RestTemplate restTemplate;

    private final RequestEntityFactory requestEntityFactory;

    private final String csrsServiceUrl;

    private final String csrsDeletePath;

    public CSRSService(@Value("csrs.serviceUrl") String csrsServiceUrl,
                       @Value("csrs.deleteUrl") String csrsDeletePath,
                       RestTemplate restTemplate,
                       RequestEntityFactory requestEntityFactory
    ) {
        this.restTemplate = restTemplate;
        this.requestEntityFactory = requestEntityFactory;
        this.csrsServiceUrl = csrsServiceUrl;
        this.csrsDeletePath = csrsDeletePath;
    }

    public ResponseEntity deleteCivilServant(String uid) {
        try {
            RequestEntity requestEntity = requestEntityFactory.createDeleteRequest(String.format(csrsServiceUrl + csrsDeletePath, uid));
            ResponseEntity responseEntity = restTemplate.exchange(requestEntity, Void.class);
            return responseEntity;
        } catch(RequestEntityException | RestClientException e) {
            LOGGER.error("Could not delete user from csrs service: " + e);
            return null;
        }
    }
}
