package uk.gov.cshr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Reset;
import uk.gov.cshr.domain.ResetStatus;
import uk.gov.cshr.domain.factory.ResetFactory;
import uk.gov.cshr.repository.ResetRepository;

import java.util.Date;

@Service
@Transactional
public class ResetService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResetService.class);

    private final ResetRepository resetRepository;
    private final NotifyService notifyService;
    private final ResetFactory resetFactory;
    private final int validityInSeconds;

    public ResetService(ResetRepository resetRepository,
                        NotifyService notifyService,
                        ResetFactory resetFactory,
                        @Value("${notifications.reset.validityInSeconds}") int validityInSeconds
    ) {
        this.resetRepository = resetRepository;
        this.notifyService = notifyService;
        this.resetFactory = resetFactory;
        this.validityInSeconds = validityInSeconds;
    }

    public boolean isResetExpired(Reset reset) {
        long diffInMs = new Date().getTime() - reset.getRequestedAt().getTime();

        if (diffInMs > validityInSeconds * 1000 && reset.getResetStatus().equals(ResetStatus.PENDING)) {
            reset.setResetStatus(ResetStatus.EXPIRED);
            resetRepository.save(reset);
            return true;
        }

        return false;
    }

    public boolean isResetPending(Reset reset) {
        return reset.getResetStatus().equals(ResetStatus.PENDING);
    }

    public void notifyForResetRequest(String email) {
        Reset reset = resetFactory.create(email);

        notifyService.sendPasswordResetVerification(reset.getEmail(), reset.getCode());

        resetRepository.save(reset);

        LOGGER.info("Reset request sent to {} ", email);
    }

    public void notifyOfSuccessfulReset(Reset reset) {
        reset.setResetAt(new Date());
        reset.setResetStatus(ResetStatus.RESET);

        notifyService.sendPasswordResetNotification(reset.getEmail());

        resetRepository.save(reset);

        LOGGER.info("Reset success sent to {} ", reset.getEmail());
    }
}
