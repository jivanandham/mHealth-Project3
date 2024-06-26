package edu.pitt.lersais.mhealth.util;

import android.content.Context;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudkms.v1.CloudKMS;
import com.google.api.services.cloudkms.v1.CloudKMSScopes;
import com.google.api.services.cloudkms.v1.model.DecryptRequest;
import com.google.api.services.cloudkms.v1.model.DecryptResponse;
import com.google.api.services.cloudkms.v1.model.EncryptRequest;
import com.google.api.services.cloudkms.v1.model.EncryptResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * The CloudKMSUtil that is a toolkit to encrypt/decrypt using Google Cloud KMS.
 *
 * @author Haobing Huang and Runhua Xu.
 *
 */

public class CloudKMSUtil {

    private CloudKMSUtil() {
    }

    private static class CloudKMSUtilHolder {
        private static final CloudKMSUtil INSTANCE = new CloudKMSUtil();
    }

    public static final CloudKMSUtil getInstance() {
        return CloudKMSUtilHolder.INSTANCE;
    }

    public CloudKMS createAuthorizedClient(Context context) throws IOException {

        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        InputStream input = context.getAssets().open("healthapp-ac092-firebase-adminsdk-zjmus-50867ea766.json");
        GoogleCredential credential = GoogleCredential.fromStream(input);

        input.close();

        if (credential.createScopedRequired()) {
            credential = credential.createScoped(CloudKMSScopes.all());
        }

        return new CloudKMS.Builder(transport, jsonFactory, credential)
                .setApplicationName("CloudKMS mHealthDemo")
                .build();
    }

    public String encrypt(String plaintext, String userUid, CloudKMS kms)
            throws IOException {
        String resourceName = String.format(
                "projects/%s/locations/%s/keyRings/%s/cryptoKeys/%s",
                Constant.KMS_PROJECT_ID, Constant.KMS_LOCATION, Constant.KMS_KEY_RING_ID, userUid);

        byte[] plaintextbyte = plaintext.getBytes("ISO-8859-1");

        EncryptRequest request = new EncryptRequest().encodePlaintext(plaintextbyte);
        EncryptResponse response = kms.projects().locations().keyRings().cryptoKeys()
                .encrypt(resourceName, request)
                .execute();

        return new String(response.decodeCiphertext(), "ISO-8859-1");
    }

    public String decrypt(String ciphertext, String userUid, CloudKMS kms)
            throws IOException {

        String resourceName = String.format(
                "projects/%s/locations/%s/keyRings/%s/cryptoKeys/%s",
                Constant.KMS_PROJECT_ID, Constant.KMS_LOCATION, Constant.KMS_KEY_RING_ID, userUid);

        byte[] ciphertextbyte = ciphertext.getBytes("ISO-8859-1");

        DecryptRequest request = new DecryptRequest().encodeCiphertext(ciphertextbyte);
        DecryptResponse response = kms.projects().locations().keyRings().cryptoKeys()
                .decrypt(resourceName, request).execute();

        return new String(response.decodePlaintext(), "ISO-8859-1");
    }

    public String encrypt(String plaintext, Context context, String userUid)
            throws IOException {
        String resourceName = String.format(
                "projects/%s/locations/%s/keyRings/%s/cryptoKeys/%s",
                Constant.KMS_PROJECT_ID, Constant.KMS_LOCATION, Constant.KMS_KEY_RING_ID, userUid);
        CloudKMS kms = createAuthorizedClient(context);

        byte[] plaintextbyte = plaintext.getBytes("ISO-8859-1");


        EncryptRequest request = new EncryptRequest().encodePlaintext(plaintextbyte);
        EncryptResponse response = kms.projects().locations().keyRings().cryptoKeys()
                .encrypt(resourceName, request)
                .execute();

        return new String(response.decodeCiphertext(), "ISO-8859-1");

    }

    public String decrypt(String ciphertext, Context context, String userUid)
            throws IOException {
        String resourceName = String.format(
                "projects/%s/locations/%s/keyRings/%s/cryptoKeys/%s",
                Constant.KMS_PROJECT_ID, Constant.KMS_LOCATION, Constant.KMS_KEY_RING_ID, userUid);
        CloudKMS kms = createAuthorizedClient(context);


        byte[] ciphertextbyte = ciphertext.getBytes("ISO-8859-1");
        DecryptRequest request = new DecryptRequest().encodeCiphertext(ciphertextbyte);
        DecryptResponse response = kms.projects().locations().keyRings().cryptoKeys()
                .decrypt(resourceName, request).execute();

        return new String(response.decodePlaintext(), "ISO-8859-1");
    }
}
