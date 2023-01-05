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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    public void existsReactivationByEmailAndRequestedAtBeforeReturnsTrueIfReactivationRequestExistsForEmailBeforeGivenDate() throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        Date dateOfReactivationRequest = formatter.parse("14-Nov-2022");
        Date dateBeforeReactivationRequest = formatter.parse("12-Nov-2022");
        Date dateOfReactivationRequest2 = formatter.parse("15-Nov-2022");

        String email = "my.name@myorg.gov.uk";

        Reactivation reactivation = new Reactivation();
        reactivation.setCode(RandomStringUtils.random(40, true, true));
        reactivation.setReactivationStatus(ReactivationStatus.PENDING);
        reactivation.setRequestedAt(dateOfReactivationRequest);
        reactivation.setEmail(email);

        Reactivation reactivation2 = new Reactivation();
        reactivation2.setCode(RandomStringUtils.random(40, true, true));
        reactivation2.setReactivationStatus(ReactivationStatus.PENDING);
        reactivation2.setRequestedAt(dateOfReactivationRequest2);
        reactivation2.setEmail(email);

        reactivationRepository.save(reactivation);
        reactivationRepository.save(reactivation2);

        boolean reactivationPending = reactivationRepository.existsByEmailAndReactivationStatusEqualsAndRequestedAtAfter(email, ReactivationStatus.PENDING, dateBeforeReactivationRequest);
        assertThat(reactivationPending, equalTo(true));
    }

    @Test
    public void existsReactivationByEmailAndRequestedAtBeforeReturnsFalseIfReactivationRequestDoesNotExistForEmailBeforeGivenDate() throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        Date dateOfReactivationRequest = formatter.parse("14-Nov-2022");
        Date dateAfterReactivationRequest = formatter.parse("15-Nov-2022");

        String email = "my.name@myorg.gov.uk";

        Reactivation reactivation = new Reactivation();
        reactivation.setCode(RandomStringUtils.random(40, true, true));
        reactivation.setReactivationStatus(ReactivationStatus.PENDING);
        reactivation.setRequestedAt(dateOfReactivationRequest);
        reactivation.setEmail(email);

        reactivationRepository.save(reactivation);

        boolean reactivationPending = reactivationRepository.existsByEmailAndReactivationStatusEqualsAndRequestedAtAfter(email, ReactivationStatus.PENDING, dateAfterReactivationRequest);
        assertThat(reactivationPending, equalTo(false));
    }

    @Test
    public void existsByEmailAndReactivationStatusEqualsReturnsFalseIfReactivationDoesNotExistsForEmailAndReactivationStatus() throws ParseException {
        String email = "my.name2@myorg.gov.uk";

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        Date date = formatter.parse("12-Nov-2022");
        boolean pendingReactivationExists = reactivationRepository.existsByEmailAndReactivationStatusEqualsAndRequestedAtAfter(email, ReactivationStatus.PENDING, date);
        assertThat(pendingReactivationExists, equalTo(false));
    }
}
