package uk.gov.cshr.notifications.dto.factory;

import org.junit.Test;
import uk.gov.cshr.notifications.dto.MessageDto;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MessageDtoFactoryTest {

    private final MessageDtoFactory messageDtoFactory = new MessageDtoFactory();

    @Test
    public void shouldReturnMessageDto() {
        String recipient = "user@example.org";
        String templateId = "template-id";

        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("name", "test-name");

        MessageDto messageDto = messageDtoFactory.create(recipient, templateId, personalisation);

        assertEquals(recipient, messageDto.getRecipient());
        assertEquals(templateId, messageDto.getTemplateId());
        assertEquals(personalisation, messageDto.getPersonalisation());
    }
}