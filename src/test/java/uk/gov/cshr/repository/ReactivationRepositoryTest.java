package uk.gov.cshr.repository;

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
    public static final String CODE = "abc123";
    public static final String EMAIL = "my.name@myorg.gov.uk";
    public static final ReactivationStatus REACTIVATION_STATUS = ReactivationStatus.PENDING;
    @Autowired
    private ReactivationRepository reactivationRepository;

    @Test
    public void existsByEmailAndReactivationStatusEqualsReturnsTrueIfReactivationExistsForEmailAndReactivationStatus(){

        Reactivation reactivation = new Reactivation(CODE, REACTIVATION_STATUS, new Date(), EMAIL);
        reactivationRepository.save(reactivation);
        boolean pendingReactivationExists = true;

        assertThat(pendingReactivationExists, equalTo(true));
    }
}
