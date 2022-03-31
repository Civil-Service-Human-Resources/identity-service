package uk.gov.cshr.validation.validators;

import org.junit.Test;
import org.mockito.Mock;
import uk.gov.cshr.service.CsrsService;

import javax.validation.ConstraintValidatorContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WhitelistedValidatorTest {
    @Mock
    private CsrsService csrsService;

    private WhitelistedValidator validator = new WhitelistedValidator(csrsService);

    private ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);

    @Test
    public void shouldReturnTrueIfValueHasWhitelistedDomain() {
        when(csrsService.getWhitelistedDomains()).thenReturn(new String[]{"user@domain.org", "user@example.org"});

        assertTrue(validator.isValid("user@domain.org", constraintValidatorContext));
        assertTrue(validator.isValid("user@example.org", constraintValidatorContext));
    }

    @Test
    public void shouldReturnFalseIfValueDoesNotHaveWhitelistedDomain() {
        when(csrsService.getWhitelistedDomains()).thenReturn(new String[]{"user@domain.org", "user@example.org"});

        assertFalse(validator.isValid("user@not-in-whitelist.org", constraintValidatorContext));
    }
}