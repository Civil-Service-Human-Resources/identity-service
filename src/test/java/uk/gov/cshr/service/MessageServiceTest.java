package uk.gov.cshr.service;

import org.junit.Test;
import org.mockito.Mock;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.notifications.dto.factory.MessageDtoFactory;

import java.time.Instant;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

public class MessageServiceTest {

    private MessageDtoFactory messageDtoFactory = mock(MessageDtoFactory.class);

    private final String suspensionMessageTemplateId = "suspensionMessageTemplateId";

    private final MessageService messageService = new MessageService(suspensionMessageTemplateId, messageDtoFactory);

    @Test
    public void shouldCreateSuspensionMessage() {
        Identity identity = new Identity(
                "",
                "test@domain.com",
                "",
                true,
                false,
                new HashSet<>(),
                Instant.now()
        );

        MessageDto messageDto = new MessageDto();

        when(messageDtoFactory.create(any(), any(), any())).thenReturn(messageDto);

        assertEquals(messageService.createSuspensionMessage(identity), messageDto);

        verify(messageDtoFactory).create(any(), any(), any());
    }
}