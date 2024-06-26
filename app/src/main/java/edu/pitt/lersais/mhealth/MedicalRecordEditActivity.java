package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.pitt.lersais.mhealth.model.MedicalHistoryRecord;
import edu.pitt.lersais.mhealth.util.Constant;
import edu.pitt.lersais.mhealth.util.CryptoMessageHandler;
import edu.pitt.lersais.mhealth.util.EncryptMedicalRecordThread;

/**
 * The MedicalRecordEditActivity that is used to edit Medical Record.
 *
 * @author Haobing Huang and Runhua Xu.
 */
public class MedicalRecordEditActivity extends BaseActivity implements CryptoMessageHandler.Callback {

    private static final String TAG = "MedicalRecordEditActivity";
    private static final String FIREBASE_DATABASE = "MedicalHistory";

    // BEGIN
    private EditText mName;
    private EditText mDob;
    private EditText mOccupation;
    private EditText mContact;
    private EditText mAllergies;
    private EditText mFather_diseases;
    private EditText mMother_diseases;
    private EditText mSibling_diseases;
    private EditText mComments;
    private RadioGroup mSex;
    private RadioGroup mMarital_status;
    // END
    private List<CheckBox> mDiseasesList = new ArrayList<CheckBox>();
    private HashMap<String, RadioGroup> mHabits = new HashMap<>();


    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        // If edit existing medical history record
        Intent getIntent = getIntent();
        String flag = getIntent.getStringExtra("flag");
        if (flag.equals("MedicalRecordViewActivity")) {
            Bundle bundle = getIntent.getBundleExtra("data");
            MedicalHistoryRecord record = (MedicalHistoryRecord) bundle.getSerializable("map");
            preSetMedicalHistoryRecordValue(record);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference(FIREBASE_DATABASE).child(mCurrentUser.getUid());

        findViewById(R.id.save_record).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    MedicalHistoryRecord record = getMedicalHistoryRecordFromView();
                    if(!valueValidate(record)){
                        return;
                    }
                    else{
                        encryptAndSaveRecord(record);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Acquire the medical history record information from view to construct a MedicalHistoryRecord instance.
     * @return A MedicalHistoryRecord instance.
     * @throws IOException
     */
    public MedicalHistoryRecord getMedicalHistoryRecordFromView() throws IOException{
        // TODO
        // BEGIN

        mName = findViewById(R.id.name_edit);
        mDob = findViewById(R.id.dob_edit);
        mOccupation = findViewById(R.id.occupation_edit);
        mContact = findViewById(R.id.contact_edit);
        mAllergies = findViewById(R.id.allergies_edit);
        mFather_diseases = findViewById(R.id.father_edit);
        mMother_diseases = findViewById(R.id.mother_edit);
        mSibling_diseases = findViewById(R.id.sibling_edit);
        mComments = findViewById(R.id.comments_edit);
        mSex = findViewById(R.id.sex_group);
        mMarital_status = findViewById(R.id.marital_status_group);

        String name = mName.getText().toString();
        String dob = mDob.getText().toString();
        String occupation = mOccupation.getText().toString();
        String contact = mContact.getText().toString();
        String allergies = mAllergies.getText().toString();
        String father_diseases = mFather_diseases.getText().toString();
        String mother_diseases = mMother_diseases.getText().toString();
        String sibling_diseases = mSibling_diseases.getText().toString();
        String comments = mComments.getText().toString();
        String sex = "";
        String marital_status = "";

        if(father_diseases.isEmpty()){
            father_diseases = "N/A";
        }
        if(mother_diseases.isEmpty()){
            mother_diseases = "N/A";
        }
        if(sibling_diseases.isEmpty()){
            sibling_diseases = "N/A";
        }
        HashMap<String, String> family_diseases = new HashMap<>();
        family_diseases.put(Constant.MEDICAL_RECORD_FAMILY_FATHER_KEY, father_diseases);
        family_diseases.put(Constant.MEDICAL_RECORD_FAMILY_MOTHER_KEY, mother_diseases);
        family_diseases.put(Constant.MEDICAL_RECORD_FAMILY_SIBLING_KEY, sibling_diseases);

        for (int i = 0; i < mSex.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) mSex.getChildAt(i);
            if (radioButton.isChecked()) {
                sex = radioButton.getText().toString();
                break;
            }
        }

        for (int i = 0; i < mMarital_status.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) mMarital_status.getChildAt(i);
            if (radioButton.isChecked()) {
                marital_status = radioButton.getText().toString();
                break;
            }
        }
        CheckBox checkBox1, checkBox2, checkBox3;

        checkBox1 = findViewById(R.id.diseases1);
        checkBox2 = findViewById(R.id.diseases2);
        checkBox3 = findViewById(R.id.diseases3);
        mDiseasesList.add(checkBox1);
        mDiseasesList.add(checkBox2);
        mDiseasesList.add(checkBox3);

        String diseases = "";
        for (CheckBox checkbox : mDiseasesList) {
            if (checkbox.isChecked()) {
                diseases = checkbox.getText().toString() + ";" + diseases;
            }
        }
        if(diseases.isEmpty()){
            diseases = "N/A";
        }
        if(comments.isEmpty()){
            comments = "N/A";
        }

        RadioGroup radio1 = findViewById(R.id.Alcohol);
        RadioGroup radio2 = findViewById(R.id.Cannabis);
        HashMap<String, String> habits = new HashMap<>();
        mHabits.put(Constant.MEDICAL_RECORD_HABIT_ALCOHOL_KEY, radio1);
        mHabits.put(Constant.MEDICAL_RECORD_HABIT_CANNABIS_KEY, radio2);

        for (String key : mHabits.keySet()) {
            RadioGroup radio = mHabits.get(key);
            for (int i = 0; i < radio.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) radio.getChildAt(i);
                habits.put(key,"");
                if (radioButton.isChecked()) {
                    habits.put(key, radioButton.getText().toString());
                    break;
                }
            }
        }

        MedicalHistoryRecord medicalHistoryRecord = new MedicalHistoryRecord(
                name, dob, sex, marital_status, occupation, contact, allergies, diseases,
                family_diseases, habits, comments);
        return medicalHistoryRecord;

        // END
    }

    /**
     * Call an encryption thread to encrypt the record, the thread will callback the message handler to save the encrypted record.
     * @param medicalHistoryRecord
     * @throws IOException
     */
    public void encryptAndSaveRecord(MedicalHistoryRecord medicalHistoryRecord) throws IOException {

            CryptoMessageHandler messageHandler = new CryptoMessageHandler(Looper.getMainLooper());
            messageHandler.setCallback(MedicalRecordEditActivity.this);

            Thread encryptorThread = new EncryptMedicalRecordThread(
                    medicalHistoryRecord,
                    mCurrentUser.getUid(),
                    getApplicationContext(),
                    messageHandler);
            encryptorThread.start();

            showProgressDialog();

    }

    /**
     * Save the encrypted record the cloud database
     *
     * @param record
     */
    public void processCryptoRecord(Object record) {
        MedicalHistoryRecord encryptedRecord = (MedicalHistoryRecord) record;

        mDatabase.setValue(encryptedRecord);

        hideProgressDialog();

        Intent intent = new Intent(MedicalRecordEditActivity.this,
                MedicalRecordViewActivity.class);
        startActivity(intent);
    }

    public boolean valueValidate(MedicalHistoryRecord record){
        if(record.getName().isEmpty()){
            Toast.makeText(MedicalRecordEditActivity.this, "Name can not be empty.",
                    Toast.LENGTH_LONG).show();
            return false;

        }
        if(record.getDob().isEmpty()){
            Toast.makeText(MedicalRecordEditActivity.this, "Date of Birth can not be empty.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if(record.getSex().isEmpty()){
            Toast.makeText(MedicalRecordEditActivity.this, "Sex can not be empty.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if(record.getMarital_status().isEmpty()){
            Toast.makeText(MedicalRecordEditActivity.this, "Marital Status can not be empty.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if(record.getOccupation().isEmpty()){
            Toast.makeText(MedicalRecordEditActivity.this, "Occupation can not be empty.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if(record.getContact().isEmpty()){
            Toast.makeText(MedicalRecordEditActivity.this, "Contact can not be empty.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if(record.getAllergies().isEmpty()){
            Toast.makeText(MedicalRecordEditActivity.this, "Allergies can not be empty.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        for(String habit: record.getHabits().keySet()){
            if(record.getHabits().get(habit).isEmpty()){
                Toast.makeText(MedicalRecordEditActivity.this, "habit-"+habit+" can not be empty.",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    /**
     * preset the decrypted medical record for editing
     * @param data
     */
    public void preSetMedicalHistoryRecordValue(MedicalHistoryRecord data) {

        // BEGIN
        EditText name = findViewById(R.id.name_edit);
        name.setText(data.getName());
        EditText dob = findViewById(R.id.dob_edit);
        dob.setText(data.getDob());
        EditText occupation = findViewById(R.id.occupation_edit);
        occupation.setText(data.getOccupation());
        EditText contact = findViewById(R.id.contact_edit);
        contact.setText(data.getContact());
        EditText allergies = findViewById(R.id.allergies_edit);
        allergies.setText(data.getAllergies());
        EditText father = findViewById(R.id.father_edit);
        father.setText(data.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_FATHER_KEY));
        EditText mother = findViewById(R.id.mother_edit);
        mother.setText(data.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_MOTHER_KEY));
        EditText sibling = findViewById(R.id.sibling_edit);
        sibling.setText(data.getFamily_diseases().get(Constant.MEDICAL_RECORD_FAMILY_SIBLING_KEY));
        EditText comments = findViewById(R.id.comments_edit);
        comments.setText(data.getComments());

        CheckBox checkBox1 = findViewById(R.id.diseases1);
        CheckBox checkBox2 = findViewById(R.id.diseases2);
        CheckBox checkBox3 = findViewById(R.id.diseases3);
        String[] diseases = data.getDiseases().split(";");
        for (String disease: diseases) {
            if (disease.trim().equals("Heart attack")) {
                checkBox1.setChecked(true);
                continue;
            }
            if (disease.trim().equals("Rheumatic Fever")) {
                checkBox2.setChecked(true);
                continue;
            }
            if (disease.trim().equals("Heart murmur")) {
                checkBox3.setChecked(true);
                continue;
            }
        }

        RadioGroup sexGroup = findViewById(R.id.sex_group);
        String sex = data.getSex();
        if (sex.equals("Male")) {
            sexGroup.check(R.id.male);
        } else if (sex.equals("Female")) {
            sexGroup.check(R.id.female);
        }

        RadioGroup alcoholGroup = findViewById(R.id.Alcohol);
        String alcohol = data.getHabits().get(Constant.MEDICAL_RECORD_HABIT_ALCOHOL_KEY);
        if (alcohol.equals("Yes")) {
            alcoholGroup.check(R.id.y1);
        } else if (alcohol.equals("No")){
            alcoholGroup.check(R.id.n1);
        }

        RadioGroup cannabisGroup = findViewById(R.id.Cannabis);
        String cannabis = data.getHabits().get(Constant.MEDICAL_RECORD_HABIT_CANNABIS_KEY);
        if (cannabis.equals("Yes")) {
            cannabisGroup.check(R.id.y2);
        } else if (alcohol.equals("No")){
            cannabisGroup.check(R.id.n2);
        }

        RadioGroup marrital_status_Group = findViewById(R.id.marital_status_group);
        String marital_status = data.getMarital_status();
        switch (marital_status) {
            case "Single":
                marrital_status_Group.check(R.id.single);
                break;
            case "Married":
                marrital_status_Group.check(R.id.married);
                break;
            case "Divorced":
                marrital_status_Group.check(R.id.divorced);
                break;
            case "Widowed":
                marrital_status_Group.check(R.id.widowed);
                break;
            default:
                break;
        }
        // END

    }
}
