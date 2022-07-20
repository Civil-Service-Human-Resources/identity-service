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
    @Autowired
    ReactivationRepository reactivationRepository;

    @Test
    public void existsByEmailAndReactivationStatusEqualsReturnsTrueIfReactivationExistsForEmailAndReactivationStatus(){
        String email = "my.name@myorg.gov.uk";
        ReactivationStatus reactivationStatus = ReactivationStatus.PENDING;

        Reactivation reactivation = new Reactivation("abc", reactivationStatus, new Date(), email);
        reactivationRepository.save(reactivation);
        boolean pendingReactivationExists = true;

        assertThat(pendingReactivationExists, equalTo(true));
    }
}
