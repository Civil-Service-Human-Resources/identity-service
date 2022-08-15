package uk.gov.cshr.repository;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.domain.ReactivationStatus;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ReactivationRepositoryTest {
    public static final ReactivationStatus REACTIVATION_STATUS = ReactivationStatus.PENDING;
    @Autowired
    private ReactivationRepository reactivationRepository;

    @Test
    public void existsByEmailAndReactivationStatusEqualsReturnsTrueIfReactivationExistsForEmailAndReactivationStatus(){
        String email = "my.name@myorg.gov.uk";

        Reactivation reactivation = new Reactivation();
        reactivation.setCode(RandomStringUtils.random(40, true, true));
        reactivation.setReactivationStatus(ReactivationStatus.PENDING);
        reactivation.setRequestedAt(new Date());
        reactivation.setEmail(email);

        reactivationRepository.save(reactivation);
        boolean pendingReactivationExists = reactivationRepository.existsByEmailAndReactivationStatusEquals(email, ReactivationStatus.PENDING);

        assertThat(pendingReactivationExists, equalTo(true));
    }

    @Test
    public void existsByEmailAndReactivationStatusEqualsReturnsFalseIfReactivationDoesNotExistsForEmailAndReactivationStatus(){
        String email = "my.name2@myorg.gov.uk";
        boolean pendingReactivationExists = reactivationRepository.existsByEmailAndReactivationStatusEquals(email, ReactivationStatus.PENDING);
        assertThat(pendingReactivationExists, equalTo(false));
    }
}
