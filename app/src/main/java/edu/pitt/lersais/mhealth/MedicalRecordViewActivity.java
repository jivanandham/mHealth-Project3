package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;
import edu.pitt.lersais.mhealth.util.Constant;
import edu.pitt.lersais.mhealth.util.CryptoMessageHandler;
import edu.pitt.lersais.mhealth.util.DecryptMedicalRecordThread;

/**
 * The MedicalRecordViewActivity that is used to view Medical Record.
 *
 * @author Haobing Huang and Runhua Xu.
 *
 */
public class MedicalRecordViewActivity extends BaseActivity implements CryptoMessageHandler.Callback {

    private static final String TAG = "MedRecordViewActivity";
    private static final String FIREBASE_DATABASE = "MedicalHistory";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private MedicalHistoryRecord mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record_view);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {

            Intent intent = new Intent(this, LoginActivity.class);
            finish();
            startActivity(intent);
        } else {

            final String ID = currentUser.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference().child(FIREBASE_DATABASE).child(ID);
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    MedicalHistoryRecord encryptedRecord = dataSnapshot.getValue(MedicalHistoryRecord.class);
                    CryptoMessageHandler messageHandler = new CryptoMessageHandler(Looper.getMainLooper());
                    messageHandler.setCallback(MedicalRecordViewActivity.this);
                    Thread decryptorThread = new DecryptMedicalRecordThread(
                            encryptedRecord,
                            ID,
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

            findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MedicalRecordViewActivity.this,
                            MedicalRecordEditActivity.class);

                    intent.putExtra("flag", "MedicalRecordViewActivity");
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("map", mMessage);
                    intent.putExtra("data", bundle);
                    startActivity(intent);
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MedicalRecordViewActivity.this,
                MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    // BEGIN
    public void processCryptoRecord(Object record) {
        MedicalHistoryRecord decryptedRecord = (MedicalHistoryRecord) record;
        // assign the decrypted record to the message in case of editing requirement.
        mMessage = decryptedRecord;

        TextView name = findViewById(R.id.name);
        name.setText(decryptedRecord.getName());
        TextView dob = findViewById(R.id.dob);
        dob.setText(decryptedRecord.getDob());
        TextView sex = findViewById(R.id.sex);
        sex.setText(decryptedRecord.getSex());
        TextView marital_status = findViewById(R.id.marital_status);
        marital_status.setText(decryptedRecord.getMarital_status());
        TextView occupation = findViewById(R.id.occupation);
        occupation.setText(decryptedRecord.getOccupation());
        TextView contact = findViewById(R.id.contact);
        contact.setText(decryptedRecord.getContact());
        TextView allergies = findViewById(R.id.allergies);
        allergies.setText(decryptedRecord.getAllergies());
        TextView diseases = findViewById(R.id.pastdiseases);
        diseases.setText(decryptedRecord.getDiseases());

        TextView father = findViewById(R.id.father);
        father.setText(decryptedRecord.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_FATHER_KEY));
        TextView mother = findViewById(R.id.mother);
        mother.setText(decryptedRecord.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_MOTHER_KEY));
        TextView sibling = findViewById(R.id.sibling);
        sibling.setText(decryptedRecord.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_SIBLING_KEY));
        TextView habitAlcohol = findViewById(R.id.Alcohol);
        habitAlcohol.setText(decryptedRecord.getHabits().get(Constant.MEDICAL_RECORD_HABIT_ALCOHOL_KEY));
        TextView habitCannabis = findViewById(R.id.Cannabis);
        habitCannabis.setText(decryptedRecord.getHabits().get(Constant.MEDICAL_RECORD_HABIT_CANNABIS_KEY));

        TextView comments = findViewById(R.id.comments);
        comments.setText(decryptedRecord.getComments());
        Log.d(TAG, "value set succeed");

        hideProgressDialog();
    }
    // END
}



