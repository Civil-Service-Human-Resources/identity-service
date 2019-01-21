package uk.gov.cshr.service;

import lombok.Data;

import java.util.Map;

@Data
public class EmailNotification {
    private String templateId;
    private String recipient;
    private Map<String, String> personalisation;
    private String reference;
}
