package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.notifications.dto.factory.MessageDtoFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageService {

    private final MessageDtoFactory messageDtoFactory;

    private final String suspensionMessageTemplateId;

    private final String deletionMessageTemplateId;

    public MessageService(@Value("govNotify.template.accountSuspension") String suspensionMessageTemplateId,
                          @Value("govNotify.template.accountDeletion") String deletionMessageTemplateId,
                          MessageDtoFactory messageDtoFactory
    ) {
        this.messageDtoFactory = messageDtoFactory;
        this.suspensionMessageTemplateId = suspensionMessageTemplateId;
        this.deletionMessageTemplateId = deletionMessageTemplateId;
    }

    public MessageDto createSuspensionMessage(Identity identity) {
        Map<String, String> map = new HashMap<>();

        map.put("learnerName", identity.getEmail());

        return messageDtoFactory.create(identity.getEmail(), suspensionMessageTemplateId, map);
    }

    public MessageDto createDeletionMessage(Identity identity) {
        Map<String, String> map = new HashMap<>();

        map.put("learnerName", identity.getEmail());

        return messageDtoFactory.create(identity.getEmail(), deletionMessageTemplateId, map);
    }
}
