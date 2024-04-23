package uk.gov.cshr.controller.signup;

import lombok.Data;

@Data
public class EnterTokenWithOrganisationForm {
    private String organisation;
    private String token;
}
