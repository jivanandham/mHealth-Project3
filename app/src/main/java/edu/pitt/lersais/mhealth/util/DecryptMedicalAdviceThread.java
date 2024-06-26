package edu.pitt.lersais.mhealth.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.pitt.lersais.mhealth.model.MedicalAdviceRecord;
import tgio.rncryptor.RNCryptorNative;

/**
 * The DecryptMedicalAdviceThread that is a thread to decrypt the a group of MedicalAdviceRecords.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class DecryptMedicalAdviceThread extends Thread {
    private static final String TAG = "DecryptMedAdvThread";

    private Handler handler;

    private List<MedicalAdviceRecord> encryptedRecords;
    private List<MedicalAdviceRecord> originalRecords;
    private Context context;
    private String userUid;

    public DecryptMedicalAdviceThread(
            List<MedicalAdviceRecord> records,
            String userUid,
            Context context,
            Handler handler) {
        this.encryptedRecords = records;
        this.userUid = userUid;
        this.context = context;
        this.handler = handler;

        this.originalRecords = new ArrayList<>();
    }

    @Override
    public void run() {
        Looper.prepare();

        Log.d(TAG, "start decrypting medical history record");
        Log.d(TAG, "begins:" + currentThread().getName());
        try {

            // Create the Cloud KMS client.
            CloudKMSUtil kmsUtil = CloudKMSUtil.getInstance();
            RNCryptorNative rnCryptor = new RNCryptorNative();

            for (MedicalAdviceRecord encryptedRecord: encryptedRecords) {
                MedicalAdviceRecord originalRecord = new MedicalAdviceRecord();
                originalRecord.setTitle(kmsUtil.decrypt(encryptedRecord.getTitle(), context, userUid));
                String nonce = kmsUtil.decrypt(encryptedRecord.getNonce(), context, userUid);
                originalRecord.setBitmap(rnCryptor.decrypt(encryptedRecord.getBitmap(), nonce));
                originalRecord.setContent(kmsUtil.decrypt(encryptedRecord.getContent(), context, userUid));
                originalRecords.add(originalRecord);
            }

            Message msg = new Message();
            msg.obj = originalRecords;
            handler.sendMessage(msg);
            Log.d(TAG, "message sent");

        } catch (IOException e) {
            e.printStackTrace();
        }

        super.run();
    }
}