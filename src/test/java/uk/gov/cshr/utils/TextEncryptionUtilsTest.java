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

    public static final String RAW_TEXT = "learner@domain.com";
    public static final String ENCRYPTED_TEXT = "W+tehauG4VaW9RRQXwc/8e1ETIr28UKG0eQYbPX2oLY=";

    @Test
    public void encryptTextShouldReturnCorrectEncryptedTextGivenRawText() throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String encryptedText = TextEncryptionUtils.encryptText(RAW_TEXT);
        assertThat(encryptedText, equalTo(ENCRYPTED_TEXT));
    }

    @Test
    public void decryptTextShouldReturnCorrectRawTextGivenEncryptedText() throws IllegalBlockSizeException, NoSuchPaddingException, UnsupportedEncodingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String rawText = TextEncryptionUtils.decryptText(ENCRYPTED_TEXT);
        assertThat(rawText, equalTo(RAW_TEXT));
    }
}
