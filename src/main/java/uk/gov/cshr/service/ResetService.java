package uk.gov.cshr.service;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Reset;
import uk.gov.cshr.domain.ResetStatus;
import uk.gov.cshr.repository.ResetRepository;
import uk.gov.service.notify.NotificationClientException;

import java.util.Date;

@Service
@Transactional
public class ResetService {

    @Autowired
    private NotifyService notifyService;

    @Value("${govNotify.template.reset}")
    private String govNotifyResetTemplateId;

    @Value("${reset.url}")
    private String resetUrlFormat;

    private ResetRepository resetRepository;

    @Autowired
    public ResetService(ResetRepository resetRepository) {
        this.resetRepository = resetRepository;
    }

    public void createNewResetForEmail(String email) throws NotificationClientException {
        Reset reset = new Reset();
        reset.setEmail(email);
        reset.setRequestedAt(new Date());
        reset.setResetStatus(ResetStatus.PENDING);
        reset.setCode(RandomStringUtils.random(40, true, true));

        notifyService.notify(reset.getEmail(), reset.getCode(), govNotifyResetTemplateId, resetUrlFormat);

        resetRepository.save(reset);
    }
}