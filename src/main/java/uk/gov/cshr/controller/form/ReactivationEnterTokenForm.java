package uk.gov.cshr.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReactivationEnterTokenForm {
    @NotBlank(message = "{validation.reactivate.organisation.NotBlank}")
    private String organisation;
    @NotBlank(message = "{validation.reactivate.token.NotBlank}")
    private String token;
    private String domain;
    private String uid;
}
