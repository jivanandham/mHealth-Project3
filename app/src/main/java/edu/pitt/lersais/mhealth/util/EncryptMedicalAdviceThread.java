package edu.pitt.lersais.mhealth.handlers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

import edu.pitt.lersais.mhealth.model.MedicalAdviceRecord;
import edu.pitt.lersais.mhealth.util.CloudKMSUtil;
import edu.pitt.lersais.mhealth.util.RandomStringUtils;
import tgio.rncryptor.RNCryptorNative;

/**
 * The EncryptMedicalAdviceThread that is a thread to encrypt the a MedicalAdviceRecord.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class EncryptMedicalAdviceThread extends Thread {
    private static final String TAG = "EncryptMedAdviceThread";

    private Handler handler;

    private MedicalAdviceRecord originalRecord;
    private MedicalAdviceRecord encryptedRecord;
    private Context context;
    private String userUid;

    public EncryptMedicalAdviceThread(
            MedicalAdviceRecord record,
            String userUid,
            Context context,
            Handler handler) {
        this.originalRecord = record;
        this.userUid = userUid;
        this.context = context;
        this.handler = handler;

        this.encryptedRecord = new MedicalAdviceRecord();
    }

    @Override
    public void run() {
        Looper.prepare();

        Log.d(TAG, "start decrypting medical advice record");
        Log.d(TAG, "begins:" + currentThread().getName());
        try {

            CloudKMSUtil kmsUtil = CloudKMSUtil.getInstance();
            RNCryptorNative rnCryptor = new RNCryptorNative();

            String nonce = RandomStringUtils.randomAlphanumeric(10);
            originalRecord.setNonce(nonce);

            encryptedRecord.setTitle(kmsUtil.encrypt(originalRecord.getTitle(), context, userUid));
            encryptedRecord.setNonce(kmsUtil.encrypt(nonce,context, userUid));
            encryptedRecord.setBitmap(new String(rnCryptor.encrypt(originalRecord.getBitmap(), nonce)));
            encryptedRecord.setContent(kmsUtil.encrypt(originalRecord.getContent(), context, userUid));

            Message msg = new Message();
            msg.obj = encryptedRecord;
            handler.sendMessage(msg);
            Log.d(TAG, "message sent");

        } catch (IOException e) {
            e.printStackTrace();
        }

        super.run();
    }
}