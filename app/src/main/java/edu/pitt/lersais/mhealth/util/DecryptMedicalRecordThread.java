package edu.pitt.lersais.mhealth.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.api.services.cloudkms.v1.CloudKMS;

import java.io.IOException;
import java.util.HashMap;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;

/**
 * The DecryptMedicalRecordThread that is used to decrypt.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class DecryptMedicalRecordThread extends Thread {
    private static final String TAG = "RecordDecryptionThread";

    private Handler handler;

    private MedicalHistoryRecord encryptedRecord;
    private MedicalHistoryRecord originalRecord;
    private Context context;
    private String userUid;

    public DecryptMedicalRecordThread (
            MedicalHistoryRecord record,
            String userUid,
            Context context,
            Handler handler) {
        this.encryptedRecord = record;
        this.userUid = userUid;
        this.context = context;
        this.handler = handler;

        this.originalRecord = new MedicalHistoryRecord();
    }

    @Override
    public void run() {
        Looper.prepare();

        Log.d(TAG, "start decrypting medical history record");
        System.out.println("begins:" + currentThread().getName());
        try {

            // Create the Cloud KMS client.
            CloudKMSUtil kmsUtil = CloudKMSUtil.getInstance();
            CloudKMS kms = kmsUtil.createAuthorizedClient(context.getApplicationContext());

            originalRecord.setName(kmsUtil.decrypt(encryptedRecord.getName(), userUid, kms));
            originalRecord.setDob(kmsUtil.decrypt(encryptedRecord.getDob(), userUid, kms));
            originalRecord.setSex(kmsUtil.decrypt(encryptedRecord.getSex(), userUid, kms));
            originalRecord.setMarital_status(kmsUtil.decrypt(encryptedRecord.getMarital_status(), userUid, kms));
            originalRecord.setOccupation(kmsUtil.decrypt(encryptedRecord.getOccupation(), userUid, kms));
            originalRecord.setContact(kmsUtil.decrypt(encryptedRecord.getContact(), userUid, kms));
            originalRecord.setDiseases(kmsUtil.decrypt(encryptedRecord.getDiseases(), userUid, kms));
            originalRecord.setAllergies(kmsUtil.decrypt(encryptedRecord.getAllergies(), userUid, kms));
            originalRecord.setComments(kmsUtil.decrypt(encryptedRecord.getComments(), userUid, kms));
            HashMap<String, String> familyDiseases = new HashMap<>();
            familyDiseases.put(Constant.MEDICAL_RECORD_FAMILY_FATHER_KEY,
                    kmsUtil.decrypt(encryptedRecord.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_FATHER_KEY), userUid, kms));
            familyDiseases.put(Constant.MEDICAL_RECORD_FAMILY_MOTHER_KEY,
                    kmsUtil.decrypt(encryptedRecord.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_MOTHER_KEY), userUid, kms));
            familyDiseases.put(Constant.MEDICAL_RECORD_FAMILY_SIBLING_KEY,
                    kmsUtil.decrypt(encryptedRecord.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_SIBLING_KEY), userUid, kms));
            originalRecord.setFamily_diseases(familyDiseases);
            HashMap<String, String> habits = new HashMap<>();
            habits.put(Constant.MEDICAL_RECORD_HABIT_ALCOHOL_KEY,
                    kmsUtil.decrypt(encryptedRecord.getHabits().get(Constant.MEDICAL_RECORD_HABIT_ALCOHOL_KEY), userUid, kms));
            habits.put(Constant.MEDICAL_RECORD_HABIT_CANNABIS_KEY,
                    kmsUtil.decrypt(encryptedRecord.getHabits().get(Constant.MEDICAL_RECORD_HABIT_CANNABIS_KEY), userUid, kms));
            originalRecord.setHabits(habits);


            Message msg = new Message();
            msg.obj = originalRecord;
            System.out.println("message set finish");
            handler.sendMessage(msg);

        } catch (IOException e) {
            e.printStackTrace();
        }

        super.run();
    }
}