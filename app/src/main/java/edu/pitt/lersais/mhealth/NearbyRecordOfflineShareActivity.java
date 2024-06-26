package edu.pitt.lersais.mhealth;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;
import edu.pitt.lersais.mhealth.util.CryptoMessageHandler;
import edu.pitt.lersais.mhealth.util.DecryptMedicalRecordThread;

/**
 * The NearbyRecordOfflineShareActivity that is used to share record to others nearby without internet required.
 *
 * @author Haobing Huang and Runhua Xu.
 *
 */

public class NearbyRecordOfflineShareActivity extends NearbyConnectionsActivity implements SensorEventListener, CryptoMessageHandler.Callback {

    private final static String TAG = "NearbyRecordOffShare";

    private final static String CURRENT_ROLE_STATUS_SEND = "Sender";
    private final static String CURRENT_ROLE_STATUS_RECEIVE = "Receiver";
    private final static String SERVICE_ID = "edu.pitt.lersais.mhealth.SERVICE_ID";
    private final static String CONNECTION_STATUS_SUCCESS = "Established";
    private final static String CONNECTION_STATUS_DEFAULT = "No Connection";
    private final static String RECEIVE_STATUS_DEFAULT = "No Record";
    private final static String RECEIVE_STATUS_WAITING = "Waiting Record";
    private final static String RECEIVE_STATUS_DONE = "Received Record";

    private static final Strategy NEARBY_CONNECTION_STRATEGY = Strategy.P2P_STAR;
    private static final float SHAKE_THRESHOLD_GRAVITY = 2;
    private static final long VIBRATION_STRENGTH = 500;



    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;

    private Switch mSwitch;
    private TextView mTextViewStatus;
    private TextView mTextViewConnectionStatus;
    private TextView mTextViewReceiveStatus;
    private RadioGroup mRadioGroupRecordChoose;

    private State mState = State.UNKNOWN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        if (mCurrentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            mDatabase = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(mCurrentUser.getUid());
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            mTextViewStatus = findViewById(R.id.share_receive_status);
            mTextViewStatus.setText(CURRENT_ROLE_STATUS_SEND);
            mTextViewConnectionStatus = findViewById(R.id.send_receive_status);
            mTextViewConnectionStatus.setText(CONNECTION_STATUS_DEFAULT);
            mTextViewReceiveStatus = findViewById(R.id.send_textview_receive_status);
            mTextViewReceiveStatus.setText(RECEIVE_STATUS_DEFAULT);
            mSwitch = findViewById(R.id.share_switch_send);
            mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mTextViewStatus.setText(CURRENT_ROLE_STATUS_RECEIVE);
                        findViewById(R.id.receive_component).setVisibility(View.VISIBLE);
                        findViewById(R.id.receive_record_component).setVisibility(View.GONE);
                        findViewById(R.id.share_component).setVisibility(View.GONE);
                    } else {
                        mTextViewStatus.setText(CURRENT_ROLE_STATUS_SEND);
                        findViewById(R.id.share_component).setVisibility(View.VISIBLE);
                        findViewById(R.id.receive_component).setVisibility(View.GONE);
                    }
                }
            });
        }

        mRadioGroupRecordChoose = findViewById(R.id.share_radio_button_group);
        findViewById(R.id.nearby_button_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState == State.CONNECTED) {
                    int selectButtonId = mRadioGroupRecordChoose.getCheckedRadioButtonId();
                    if (selectButtonId == R.id.share_radio_button_medical_record) {
                        RadioButton selectRadioButton = findViewById(R.id.share_radio_button_medical_record);
                        Log.d(TAG, "Select Radio Button: " + selectRadioButton.getText());

                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                MedicalHistoryRecord encryptedRecord = dataSnapshot.getValue(MedicalHistoryRecord.class);
                                CryptoMessageHandler messageHandler = new CryptoMessageHandler(Looper.getMainLooper());
                                messageHandler.setCallback(NearbyRecordOfflineShareActivity.this);
                                Thread decryptorThread = new DecryptMedicalRecordThread(
                                        encryptedRecord,
                                        mCurrentUser.getUid(),
                                        getApplicationContext(),
                                        messageHandler
                                );
                                decryptorThread.start();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
                else {
                    Toast.makeText(
                            NearbyRecordOfflineShareActivity.this,
                            "Connection is not established",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        findViewById(R.id.nearby_button_shake).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSensorManager.registerListener(
                        NearbyRecordOfflineShareActivity.this,
                        mAccelerometerSensor,
                        SensorManager.SENSOR_DELAY_UI);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

        setState(State.UNKNOWN);
        mTextViewConnectionStatus.setText(CONNECTION_STATUS_DEFAULT);
        onStateChanged(State.UNKNOWN);
        mSensorManager.unregisterListener(this);

        Intent intent = new Intent(NearbyRecordOfflineShareActivity.this,
                MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD_GRAVITY && getCurrentRoleStatus().equals(CURRENT_ROLE_STATUS_SEND)) {
            logD("Device shaken to advertise");
            setState(State.ADVERTISING);
            vibrate();
            onStateChanged(State.ADVERTISING);

        } else if (gForce > SHAKE_THRESHOLD_GRAVITY && getCurrentRoleStatus().equals(CURRENT_ROLE_STATUS_RECEIVE)) {
            logD("Device shaken to discovery");
            setState(State.DISCOVERING);
            vibrate();
            onStateChanged(State.DISCOVERING);
        }
    }

    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        // A connection to another device has been initiated!
        // We'll do authentication first, and then accept or reject the connection.
        final Endpoint connectingEndpoint = endpoint;
        logD(String.format(
                "Debug Check: ConnectionInfo[name=%s,token=%s]",
                connectionInfo.getEndpointName(),
                connectionInfo.getAuthenticationToken()));

        new AlertDialog.Builder(NearbyRecordOfflineShareActivity.this)
                .setTitle("Authenticating connection to " + connectionInfo.getEndpointName())
                .setMessage("Confirm the code " + connectionInfo.getAuthenticationToken() + " is also displayed on the other device.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTextViewConnectionStatus.setText(CONNECTION_STATUS_SUCCESS);
                        if (getCurrentRoleStatus().equals(CURRENT_ROLE_STATUS_RECEIVE)) {
                            mTextViewReceiveStatus.setText(RECEIVE_STATUS_WAITING);
                        }
                        NearbyRecordOfflineShareActivity.this.acceptConnection(connectingEndpoint);
                    }
                })
                .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTextViewConnectionStatus.setText(CONNECTION_STATUS_DEFAULT);
                        NearbyRecordOfflineShareActivity.this.rejectConnection(connectingEndpoint);
                    }
                })
                .show();

        // For testing only, directly accept connection
//        acceptConnection(endpoint);
    }

    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {
        if (getCurrentRoleStatus().equals(CURRENT_ROLE_STATUS_RECEIVE)) {
            if (payload.getType() == Payload.Type.BYTES) {
                Gson gson = new Gson();
                String record = new String(payload.asBytes());
                Log.d(TAG, "received original medical record: " + record);
                MedicalHistoryRecord medicalHistoryRecord = gson.fromJson(record, MedicalHistoryRecord.class);
                displayMedicalHistoryRecord(medicalHistoryRecord);
            }
        }
    }

    private void displayMedicalHistoryRecord(MedicalHistoryRecord medicalHistoryRecord) {
        findViewById(R.id.receive_record_component).setVisibility(View.VISIBLE);
        mTextViewReceiveStatus.setText(RECEIVE_STATUS_DONE);
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

    private void sendData(MedicalHistoryRecord medicalHistoryRecord) {
        Gson gson = new Gson();
        String record = gson.toJson(medicalHistoryRecord);
        Log.d(TAG, "medical record: " + record);
        send(Payload.fromBytes(record.getBytes()));
    }

    @Override
    public void processCryptoRecord(Object object) {
        MedicalHistoryRecord decryptedRecord = (MedicalHistoryRecord) object;
        sendData(decryptedRecord);
    }

    @Override
    protected void onEndpointConnected(Endpoint endpoint) {
        Toast.makeText(
                this, "connected successfully", Toast.LENGTH_SHORT)
                .show();
        setState(State.CONNECTED);
    }

    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        if (!isConnecting()) {
            connectToEndpoint(endpoint);
        }
    }

    private void onStateChanged(State state) {
        switch (state) {
            case UNKNOWN:
                stopAllEndpoints();
                break;
            case CONNECTED:
                if (isDiscovering()) {
                    stopDiscovering();
                }
                else if (isAdvertising()) {
                    // if we want to continue to advertise, so others can still connect,
                    // update here
                    stopAdvertising();
                }
                break;
            case ADVERTISING:
                if (isDiscovering()) {
                    stopDiscovering();
                }
                disconnectFromAllEndpoints();
                if (!isAdvertising()) {
                    startAdvertising();
                }
                else {
                    logD("already in advertising");
                }
                break;
            case DISCOVERING:
                if (isAdvertising()) {
                    stopAdvertising();
                }
                disconnectFromAllEndpoints();
                if (!isDiscovering()) {
                    startDiscovering();
                }
                else {
                    logD("already in discovering");
                }
                break;
            default:
                break;
        }
    }

    private String getCurrentRoleStatus() {
        return mTextViewStatus.getText().toString();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (hasPermissions(this, Manifest.permission.VIBRATE) && vibrator.hasVibrator()) {
            vibrator.vibrate(VIBRATION_STRENGTH);
        }
    }


    @Override
    public String getServiceId() {
        return SERVICE_ID;
    }

    @Override
    protected String getName() {
        return String.format("%s[%s]", mCurrentUser.getDisplayName(), mCurrentUser.getEmail());
    }

    @Override
    public Strategy getStrategy() {
        return NEARBY_CONNECTION_STRATEGY;
    }

    public enum State {
        UNKNOWN,
        DISCOVERING,
        ADVERTISING,
        CONNECTED
    }

    private State getState() {
        return mState;
    }

    private void setState(State state) {
        mState = state;
    }
}
