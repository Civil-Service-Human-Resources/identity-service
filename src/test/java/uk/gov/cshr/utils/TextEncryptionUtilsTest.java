package uk.gov.cshr.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class TextEncryptionUtilsTest {
    @Value("${textEncryption.key}")
    private String encryptionKey;

    public static final String RAW_TEXT = "learner@domain.com";
    public static final String ENCRYPTED_TEXT = "jYlyqFrhPuzCK6psm9v39+nYK8D0bI4ZtyFoy146TD0=";

    @Test
    public void encryptTextShouldReturnCorrectEncryptedTextGivenRawText() throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String encryptedText = TextEncryptionUtils.encryptText(RAW_TEXT, encryptionKey);
        assertThat(encryptedText, equalTo(ENCRYPTED_TEXT));
    }

    @Test
    public void decryptTextShouldReturnCorrectRawTextGivenEncryptedText() throws IllegalBlockSizeException, NoSuchPaddingException, UnsupportedEncodingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String rawText = TextEncryptionUtils.decryptText(ENCRYPTED_TEXT, encryptionKey);
        assertThat(rawText, equalTo(RAW_TEXT));
    }
}
