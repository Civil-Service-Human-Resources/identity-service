package uk.gov.cshr.domain.factory;

import org.junit.Test;
import uk.gov.cshr.domain.Reset;
import uk.gov.cshr.domain.ResetStatus;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ResetFactoryTest {
    private ResetFactory resetFactory = new ResetFactory();

    @Test
    public void shouldReturnReset() {
        String email = "learner@domain.com";

        Reset reset = resetFactory.create(email);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        assertEquals(email, reset.getEmail());
        assertNotNull(reset.getCode());
        assertEquals(40, reset.getCode().length());
        assertEquals(dateFormat.format(new Date()), dateFormat.format(reset.getRequestedAt()));
        assertEquals(ResetStatus.PENDING, reset.getResetStatus());
    }
}