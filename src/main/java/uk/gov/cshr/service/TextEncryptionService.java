//package uk.gov.cshr.service;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.crypto.BadPaddingException;
//import javax.crypto.Cipher;
//import javax.crypto.IllegalBlockSizeException;
//import javax.crypto.NoSuchPaddingException;
//import javax.crypto.spec.SecretKeySpec;
//import java.io.UnsupportedEncodingException;
//import java.security.InvalidKeyException;
//import java.security.Key;
//import java.security.NoSuchAlgorithmException;
//import java.util.Base64;
//
//@Slf4j
//@Service
//public class TextEncryptionService {
//
//    private String encryptionKey;
//
//    public TextEncryptionService(@Value("${textEncryption.encryptionKey}") String encryptionKey){
//        this.encryptionKey = encryptionKey;
//    }
//
//    public String getEncryptedText(String rawText) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
//        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
//        byte[] encrypted = cipher.doFinal(rawText.getBytes());
//        String encryptedText = Base64.getEncoder().encodeToString(encrypted);
//        return encryptedText;
//    }
//
//    private Cipher getCipher(int mode) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
//        String key = encryptionKey;
//        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
//        Cipher cipher = Cipher.getInstance("AES");
//        cipher.init(mode, aesKey);
//        return cipher;
//    }
//}
