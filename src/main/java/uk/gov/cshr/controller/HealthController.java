package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController

public class HealthController {

    @GetMapping("/health")
    public String getHealthCheck() {
        return "ONLINE";
    }
}
