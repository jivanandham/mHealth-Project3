package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Random;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;
import edu.pitt.lersais.mhealth.util.CryptoMessageHandler;
import edu.pitt.lersais.mhealth.util.DecryptMedicalRecordThread;
import tgio.rncryptor.RNCryptorNative;
/**
 * The NearbyRecordOnlineShareActivity that is used to share record to others nearby.
 *
 * @author Haobing Huang and Runhua Xu.
 *
 */
public class NearbyRecordOnlineShareActivity extends BaseActivity implements CryptoMessageHandler.Callback {

    private final static String TAG = "SHARE_RECORD_ACTIVITY";
    private final static String MESSAGE_DEFAULT = "DEFAULT MESSAGE";

    private final static String CURRENT_STATUS_SHARE = "Ready to Share";
    private final static String CURRENT_STATUS_RECEIVE = "Ready to Receive";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextView mTextViewPasscode;
    private EditText mEditTextPasscode;
    private Switch mSwitch;
    private TextView mTextViewStatus;
    private RadioGroup mRadioGroupRecordChoose;

    private Message mMessage;
    private MessageListener mMessageListener;

    private String mPasscode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {

            mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

            mTextViewPasscode = findViewById(R.id.share_text_passcode);
            mEditTextPasscode = findViewById(R.id.share_receive_passcode);
            mSwitch = findViewById(R.id.share_switch);
            mTextViewStatus = findViewById(R.id.share_receive_status);

            mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mTextViewStatus.setText(CURRENT_STATUS_RECEIVE);
                        findViewById(R.id.receive_component).setVisibility(View.VISIBLE);
                        findViewById(R.id.receive_record_component).setVisibility(View.GONE);
                        findViewById(R.id.share_component).setVisibility(View.GONE);
                    } else {
                        mTextViewStatus.setText(CURRENT_STATUS_SHARE);
                        findViewById(R.id.share_component).setVisibility(View.VISIBLE);
                        findViewById(R.id.receive_component).setVisibility(View.GONE);
                    }
                }
            });

            findViewById(R.id.share_button_generate).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int random = new Random().nextInt(1000000);
                    mTextViewPasscode.setText("" + random);
                }
            });

            mRadioGroupRecordChoose = findViewById(R.id.share_radio_button_group);

            findViewById(R.id.share_button_share).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPasscode = (String) mTextViewPasscode.getText();
                    if (mPasscode != null && !mPasscode.equals("")) {

                        int selectButtonId = mRadioGroupRecordChoose.getCheckedRadioButtonId();
                        if (selectButtonId == R.id.share_radio_button_medical_record) {
                            RadioButton selectRadioButton = findViewById(R.id.share_radio_button_medical_record);
                            Log.d(TAG, "Select Radio Button: " + selectRadioButton.getText());

                            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    MedicalHistoryRecord encryptedRecord = dataSnapshot.getValue(MedicalHistoryRecord.class);
                                    CryptoMessageHandler messageHandler = new CryptoMessageHandler(Looper.getMainLooper());
                                    messageHandler.setCallback(NearbyRecordOnlineShareActivity.this);
                                    Thread decryptorThread = new DecryptMedicalRecordThread(
                                            encryptedRecord,
                                            currentUser.getUid(),
                                            getApplicationContext(),
                                            messageHandler
                                    );
                                    decryptorThread.start();
                                    showProgressDialog();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            mMessage = new Message(MESSAGE_DEFAULT.getBytes());
                            Nearby.getMessagesClient(NearbyRecordOnlineShareActivity.this)
                                    .publish(mMessage)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Publish Successfully");
                                            } else {
                                                Log.d(TAG, "Publish Failed");
                                            }
                                        }
                                    });
                        }

                    } else {
                        snackbarNotify(view, "Please generate the passcode first");
                    }
                }
            });

            findViewById(R.id.share_button_receive).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPasscode = mEditTextPasscode.getText().toString();
                    if (mPasscode != null && !mPasscode.equals("")) {
                        mMessageListener = new MessageListener() {
                            @Override
                            public void onFound(Message message) {
                                Log.d(TAG, "Found message: " + new String(message.getContent()));
//                                displayMedicalHistoryRecord(messageToMedicalRecord(message));
                                displayMedicalHistoryRecord(secureMessageToMedicalRecord(message, mPasscode));
                            }

                            @Override
                            public void onLost(Message message) {
                                Log.d(TAG, "Lost sight of message: " + new String(message.getContent()));
                            }
                        };
                        Log.d(TAG, "Subscribing");
                        Nearby.getMessagesClient(NearbyRecordOnlineShareActivity.this)
                                .subscribe(mMessageListener)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "Subscribe Successfully");
                                        } else {
                                            Log.d(TAG, "Subscribe Failed");
                                        }
                                    }
                                });
                        showProgressDialog();
                    }
                    else {
                        snackbarNotify(view, "Please acquire the passcode first.");
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private Message medicalRecordToMessage(MedicalHistoryRecord medicalHistoryRecord) {
        Gson gson = new Gson();
        String record = gson.toJson(medicalHistoryRecord);
        Log.d(TAG, "medical record: " + record);
        Message message = new Message(record.getBytes());
        return message;
    }

    private Message secureMedicalRecordToMessage(MedicalHistoryRecord medicalHistoryRecord, String passcode) {
        Gson gson = new Gson();
        RNCryptorNative rnCryptor = new RNCryptorNative();

        String record = gson.toJson(medicalHistoryRecord);
        Log.d(TAG, "original medical record: " + record);
        String encryptedRecord = new String(rnCryptor.encrypt(record, passcode));
        Log.d(TAG, "encrypted medical record: " + encryptedRecord);
        Message message = new Message(encryptedRecord.getBytes());
        return message;
    }

    private MedicalHistoryRecord messageToMedicalRecord(Message message) {
        Gson gson = new Gson();
        String record = new String(message.getContent());
        Log.d(TAG, "received original medical record: " + record);
        MedicalHistoryRecord medicalHistoryRecord = gson.fromJson(record, MedicalHistoryRecord.class);

        return medicalHistoryRecord;
    }

    private MedicalHistoryRecord secureMessageToMedicalRecord(Message message, String passcode) {
        Gson gson = new Gson();
        RNCryptorNative rnCryptor = new RNCryptorNative();
        String encryptedRecord = new String(message.getContent());
        Log.d(TAG, "received encrypted medical record: " + encryptedRecord);
        String record = rnCryptor.decrypt(encryptedRecord, passcode);
        Log.d(TAG, "received original medical record: " + record);
        MedicalHistoryRecord medicalHistoryRecord = gson.fromJson(record, MedicalHistoryRecord.class);

        return medicalHistoryRecord;
    }

    private void displayMedicalHistoryRecord(MedicalHistoryRecord medicalHistoryRecord) {
        hideProgressDialog();
        findViewById(R.id.receive_record_component).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.name)).setText(medicalHistoryRecord.getName());
        ((TextView) findViewById(R.id.dob)).setText(medicalHistoryRecord.getDob());
        ((TextView) findViewById(R.id.sex)).setText(medicalHistoryRecord.getSex());
        ((TextView) findViewById(R.id.marital_status)).setText(medicalHistoryRecord.getMarital_status());
        ((TextView) findViewById(R.id.occupation)).setText(medicalHistoryRecord.getOccupation());
        ((TextView) findViewById(R.id.contact)).setText(medicalHistoryRecord.getContact());
        ((TextView) findViewById(R.id.allergies)).setText(medicalHistoryRecord.getAllergies());
        ((TextView) findViewById(R.id.pastdiseases)).setText(medicalHistoryRecord.getDiseases());
        ((TextView) findViewById(R.id.father)).setText(medicalHistoryRecord.getFamily_diseases().get("Father"));
        ((TextView) findViewById(R.id.mother)).setText(medicalHistoryRecord.getFamily_diseases().get("Mother"));
        ((TextView) findViewById(R.id.sibling)).setText(medicalHistoryRecord.getFamily_diseases().get("Sibling"));
        ((TextView) findViewById(R.id.Alcohol)).setText(medicalHistoryRecord.getHabits().get("Alcohol"));
        ((TextView) findViewById(R.id.Cannabis)).setText(medicalHistoryRecord.getHabits().get("Cannabis"));
        ((TextView) findViewById(R.id.comments)).setText(medicalHistoryRecord.getComments());
    }

    @Override
    public void onStop() {
        if (mMessage != null) {
            Nearby.getMessagesClient(this).unpublish(mMessage);
        }
        if (mMessageListener != null) {
            Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
        }
        super.onStop();
    }

    @Override
    public void processCryptoRecord(Object object) {
//        mMessage = medicalRecordToMessage(record);
        MedicalHistoryRecord decryptedRecord = (MedicalHistoryRecord) object;
        mMessage = secureMedicalRecordToMessage(decryptedRecord, mPasscode);
        Nearby.getMessagesClient(NearbyRecordOnlineShareActivity.this)
                .publish(mMessage)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Publish Successfully");
                        } else {
                            Log.d(TAG, "Publish Failed");
                        }
                    }
                });
        hideProgressDialog();
    }

}
