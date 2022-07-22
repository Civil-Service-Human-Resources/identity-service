package uk.gov.cshr.service;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class TextEncryptionServiceTest {
    @Test
    public void getEncryptedTextShouldReturnCorrectEncryptedText() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        TextEncryptionService textEncryptionService = new TextEncryptionService();
        String rawText = "learner@domain.com";
        String expectedOutput = "W+tehauG4VaW9RRQXwc/8e1ETIr28UKG0eQYbPX2oLY=";

        String actualOutput = textEncryptionService.getEncryptedText(rawText);

        MatcherAssert.assertThat(actualOutput, equalTo(expectedOutput));
    }

    @Test
    public void getDecryptedTextShouldReturnCorrectEncryptedText() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException {
        TextEncryptionService textEncryptionService = new TextEncryptionService();
        String encryptedText = "W+tehauG4VaW9RRQXwc/8e1ETIr28UKG0eQYbPX2oLY=";
        String expectedOutput = "learner@domain.com";

        String actualOutput = textEncryptionService.getDecryptedText(encryptedText);

        MatcherAssert.assertThat(actualOutput, equalTo(expectedOutput));
    }
}
