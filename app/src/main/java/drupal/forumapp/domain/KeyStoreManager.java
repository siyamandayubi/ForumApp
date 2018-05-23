package drupal.forumapp.domain;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

/**
 * Created by serva on 10/21/2017.
 */

public class KeyStoreManager {
    private final Context context;
    private static final String KEY_ALIAS = "ForumAppKey";
    private KeyStore keyStore;

    public KeyStoreManager(Context context) {
        this.context = context;
    }

    public void initialize() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
    }

    public String encrypt(String data) throws IOException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, KeyStoreException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, UnrecoverableEntryException {
        createIfNotExist();
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
        RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();
        Cipher input = Cipher.getInstance("RSA/ECB/Pkcs1Padding");
        input.init(Cipher.ENCRYPT_MODE, publicKey);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(
                outputStream, input);
        cipherOutputStream.write(data.getBytes("UTF-8"));
        cipherOutputStream.close();

        byte[] vals = outputStream.toByteArray();
        String encryptedText = Base64.encodeToString(vals, Base64.DEFAULT);
        return encryptedText;
    }

    public String decryptString(String encryptedText) throws IOException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, KeyStoreException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, UnrecoverableEntryException {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        Cipher output = Cipher.getInstance("RSA/ECB/Pkcs1Padding");
        output.init(Cipher.DECRYPT_MODE, privateKey);

        String cipherText = encryptedText;
        CipherInputStream cipherInputStream = new CipherInputStream(
                new ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), output);
        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i).byteValue();
        }

        String finalText = new String(bytes, 0, bytes.length, "UTF-8");
        return finalText;
    }

    private void createIfNotExist() throws NoSuchProviderException, KeyStoreException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, UnrecoverableEntryException {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 1);

            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");

            kpg.initialize(new KeyGenParameterSpec.Builder(
                    KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .build());

            kpg.generateKeyPair();
        }
    }
}
