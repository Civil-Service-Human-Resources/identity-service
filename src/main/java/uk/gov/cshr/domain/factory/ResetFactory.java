package uk.gov.cshr.domain.factory;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Component;
import uk.gov.cshr.domain.Reset;
import uk.gov.cshr.domain.ResetStatus;

import java.util.Date;

@Component
public class ResetFactory {
    public Reset create(String email) {

        Reset reset = new Reset();
        reset.setEmail(email);
        reset.setRequestedAt(new Date());
        reset.setResetStatus(ResetStatus.PENDING);
        reset.setCode(RandomStringUtils.random(40, true, true));

        return reset;
    }
}
