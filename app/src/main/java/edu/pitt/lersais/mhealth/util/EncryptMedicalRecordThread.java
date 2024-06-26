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
 * The EncryptMedicalRecordThread that is used to encrypt.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class EncryptMedicalRecordThread extends Thread {
    private static final String TAG = "RecordEncryptThread";

    private Handler handler;

    private MedicalHistoryRecord originalRecord;
    private MedicalHistoryRecord encryptedRecord;
    private Context context;
    private String userUid;

    public EncryptMedicalRecordThread(
            MedicalHistoryRecord record,
            String userUid,
            Context context,
            Handler handler) {
        this.originalRecord = record;
        this.userUid = userUid;
        this.context = context;
        this.handler = handler;

        this.encryptedRecord = new MedicalHistoryRecord();
    }

    @Override
    public void run() {
        Looper.prepare();

        Log.d(TAG, "start decrypting medical history record");
        System.out.println("begins:" + currentThread().getName());
        try {

            CloudKMSUtil kmsUtil = CloudKMSUtil.getInstance();
            CloudKMS kms = kmsUtil.createAuthorizedClient(this.context);

            encryptedRecord.setName(kmsUtil.encrypt(originalRecord.getName(), userUid, kms));
            encryptedRecord.setDob(kmsUtil.encrypt(originalRecord.getDob(), userUid, kms));
            encryptedRecord.setSex(kmsUtil.encrypt(originalRecord.getSex(), userUid, kms));
            encryptedRecord.setMarital_status(kmsUtil.encrypt(originalRecord.getMarital_status(), userUid, kms));
            encryptedRecord.setOccupation(kmsUtil.encrypt(originalRecord.getOccupation(), userUid, kms));
            encryptedRecord.setContact(kmsUtil.encrypt(originalRecord.getContact(), userUid, kms));
            encryptedRecord.setDiseases(kmsUtil.encrypt(originalRecord.getDiseases(), userUid, kms));
            encryptedRecord.setAllergies(kmsUtil.encrypt(originalRecord.getAllergies(), userUid, kms));
            encryptedRecord.setComments(kmsUtil.encrypt(originalRecord.getComments(), userUid, kms));

            HashMap<String, String> familyDiseases = new HashMap<>();
            familyDiseases.put(Constant.MEDICAL_RECORD_FAMILY_FATHER_KEY,
                    kmsUtil.encrypt(originalRecord.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_FATHER_KEY), userUid, kms));
            familyDiseases.put(Constant.MEDICAL_RECORD_FAMILY_MOTHER_KEY,
                    kmsUtil.encrypt(originalRecord.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_MOTHER_KEY), userUid, kms));
            familyDiseases.put(Constant.MEDICAL_RECORD_FAMILY_SIBLING_KEY,
                    kmsUtil.encrypt(originalRecord.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_SIBLING_KEY), userUid, kms));
            encryptedRecord.setFamily_diseases(familyDiseases);

            HashMap<String, String> habits = new HashMap<>();
            habits.put(Constant.MEDICAL_RECORD_HABIT_ALCOHOL_KEY,
                    kmsUtil.encrypt(originalRecord.getHabits().get(Constant.MEDICAL_RECORD_HABIT_ALCOHOL_KEY), userUid, kms));
            habits.put(Constant.MEDICAL_RECORD_HABIT_CANNABIS_KEY,
                    kmsUtil.encrypt(originalRecord.getHabits().get(Constant.MEDICAL_RECORD_HABIT_CANNABIS_KEY), userUid, kms));
            encryptedRecord.setHabits(habits);


            Message msg = new Message();
            msg.obj = encryptedRecord;
            System.out.println("message set finish");
            handler.sendMessage(msg);

        } catch (IOException e) {
            e.printStackTrace();
        }

        super.run();
    }
}