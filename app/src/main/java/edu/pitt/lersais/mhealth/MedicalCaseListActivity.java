package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.pitt.lersais.mhealth.adaptor.ExpandableMedicalAdviceListAdapter;
import edu.pitt.lersais.mhealth.model.MedicalAdviceRecord;
import edu.pitt.lersais.mhealth.util.CryptoMessageHandler;
import edu.pitt.lersais.mhealth.util.DecryptMedicalAdviceThread;
import edu.pitt.lersais.mhealth.util.Constant;

/**
 * Show all MedicalAdviceRecords.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class MedicalCaseListActivity extends BaseActivity implements View.OnClickListener, CryptoMessageHandler.Callback {

    private static final String TAG = "MedicalCaseListActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;

    private ExpandableListView mExpandableListView;
    private ExpandableMedicalAdviceListAdapter mExpandableListAdapter;
    private Button mButtonAdd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_case_list);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        if (mCurrentUser == null) {

            Intent intent = new Intent(MedicalCaseListActivity.this,
                    MedicalCaseListActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else {
            mButtonAdd = findViewById(R.id.medical_advice_button_add);
            mButtonAdd.setOnClickListener(this);

            // TODO
            // BEGIN
            mDatabase = FirebaseDatabase.getInstance().getReference(Constant.DATABASE_MEDICAL_ADVICE);
            mDatabase.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    presentMedicalCaseRecordsView(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            // END
        }
    }

    // BEGIN
    @Override
    public void processCryptoRecord(Object records) {
        List<MedicalAdviceRecord> decryptedRecords = (List<MedicalAdviceRecord>) records;
        mExpandableListView = findViewById(R.id.expand_list_medical_advice);
        mExpandableListAdapter = new ExpandableMedicalAdviceListAdapter(
                this,
                decryptedRecords,
                mExpandableListView);
        mExpandableListView.setAdapter(mExpandableListAdapter);
        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return false;
            }
        });
        hideProgressDialog();
    }

    private void presentMedicalCaseRecordsView(DataSnapshot dataSnapshot) {
        Log.d(TAG, "Key :" + dataSnapshot.getKey());
        List<MedicalAdviceRecord> encryptMedicalAdviceRecords = new ArrayList<>();
        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
            Log.d(TAG, "Key :" + childSnapshot.getKey());
            MedicalAdviceRecord record = childSnapshot.getValue(MedicalAdviceRecord.class);
            encryptMedicalAdviceRecords.add(record);
        }

        CryptoMessageHandler messageHandler = new CryptoMessageHandler(Looper.getMainLooper());
        messageHandler.setCallback(MedicalCaseListActivity.this);
        DecryptMedicalAdviceThread decryptThread = new DecryptMedicalAdviceThread(
                encryptMedicalAdviceRecords,
                mCurrentUser.getUid(),
                getApplicationContext(),
                messageHandler
        );
        decryptThread.start();

        showProgressDialog();
    }
    // END

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.medical_advice_button_add) {
            Intent intent = new Intent(MedicalCaseListActivity.this,
                    MedicalCaseAddActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MedicalCaseListActivity.this,
                MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
}
