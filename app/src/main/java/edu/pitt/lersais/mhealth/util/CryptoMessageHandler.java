package edu.pitt.lersais.mhealth.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;

/**
 * The CryptoMessageHandler that is used to process the decrypted record.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class CryptoMessageHandler extends Handler {

    private Callback callback;
    public Object record;

    public CryptoMessageHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        this.record = msg.obj;
        if (this.callback != null) {
            callback.processCryptoRecord(record);
        }
    }

    public interface Callback {
        void processCryptoRecord(Object record);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
