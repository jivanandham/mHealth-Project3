package edu.pitt.lersais.mhealth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.text.FirebaseVisionCloudDocumentTextDetector;
import com.google.firebase.ml.vision.cloud.text.FirebaseVisionCloudText;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.util.HashMap;
import java.util.Map;

import edu.pitt.lersais.mhealth.handlers.EncryptMedicalAdviceThread;
import edu.pitt.lersais.mhealth.model.MedicalAdviceRecord;
import edu.pitt.lersais.mhealth.util.BitmapUtil;
import edu.pitt.lersais.mhealth.util.CryptoMessageHandler;

import edu.pitt.lersais.mhealth.util.Constant;

/**
 * Add a MedicalAdviceRecord into the database.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class MedicalCaseAddActivity extends BaseActivity implements View.OnClickListener, CryptoMessageHandler.Callback{

    private static final String TAG = "MedicalCaseAddActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 123;
    private static final int ML_MODEL_FIREBASE_TEXT_DETECTOR_DEVICE = 1;
    private static final int ML_MODEL_FIREBASE_TEXT_DETECTOR_CLOUD = 2;

    private ImageView mImageView;
    private EditText mTextViewRecognizedText;
    private EditText mEditTextTitle;
    private Button mButtonTakePhoto;
    private Button mButtonRecognize;
    private Button mButtonSave;

    private Bitmap mImageBitmap;
    private String mRecognizedText;

    private FirebaseVisionImage visionImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;

    private int mMachineLearningModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_case_add);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Change the machine learning model
        mMachineLearningModel = ML_MODEL_FIREBASE_TEXT_DETECTOR_DEVICE;

        mImageView = findViewById(R.id.imageview_take_photo);
        mButtonTakePhoto = findViewById(R.id.medical_case_take_photo);
        mButtonTakePhoto.setOnClickListener(this);
        mButtonRecognize = findViewById(R.id.medical_case_recognize);
        mButtonRecognize.setOnClickListener(this);
        mTextViewRecognizedText = findViewById(R.id.textview_recognized_text);
        mEditTextTitle = findViewById(R.id.medical_advice_record_title);
        mButtonSave = findViewById(R.id.medical_case_save);
        mButtonSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.medical_case_take_photo) {
            // TODO
            // BEGIN
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
            // END
        }
        else if (v.getId() == R.id.medical_case_save) {
            // TODO
            // BEGIN
            if (mEditTextTitle != null
                    && !mEditTextTitle.equals("")
                    && mImageBitmap != null
                    && mRecognizedText != null) {
                String medicalAdviceTitle = mEditTextTitle.getText().toString();
                MedicalAdviceRecord adviceRecord = new MedicalAdviceRecord();
                adviceRecord.setTitle(medicalAdviceTitle);
                adviceRecord.setBitmap(BitmapUtil.bitmapToString(mImageBitmap));
                adviceRecord.setContent(mRecognizedText);

                // start a thread to store the record
                writeToDatabase(adviceRecord);
                showProgressDialog();
            }
            // END
        }
        else if (v.getId() == R.id.medical_case_recognize) {
            // TODO
            // BEGIN
            if (mImageBitmap != null) {
                if (mMachineLearningModel == ML_MODEL_FIREBASE_TEXT_DETECTOR_DEVICE) {
                    // TODO
                    // BEGIN
                    visionImage = FirebaseVisionImage.fromBitmap(mImageBitmap);
                    FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
                    Task<FirebaseVisionText> results = detector.detectInImage(visionImage)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    StringBuffer sb = new StringBuffer();
                                    for (FirebaseVisionText.Block block: firebaseVisionText.getBlocks()) {
                                        sb.append(block.getText() + "\n");
                                    }
                                    mRecognizedText = sb.toString();
                                    mTextViewRecognizedText.setText(mRecognizedText);
                                    Log.d(TAG, "recognised text: " + mRecognizedText);

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(
                                            MedicalCaseAddActivity.this,
                                            "recognize failed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                    // END
                }
                else if (mMachineLearningModel == ML_MODEL_FIREBASE_TEXT_DETECTOR_CLOUD) {
                    // TODO
                    // BEGIN
                    visionImage = FirebaseVisionImage.fromBitmap(mImageBitmap);
                    FirebaseVisionCloudDocumentTextDetector detector = FirebaseVision.getInstance().getVisionCloudDocumentTextDetector();

                    Task<FirebaseVisionCloudText> results = detector.detectInImage(visionImage)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionCloudText>() {
                                @Override
                                public void onSuccess(FirebaseVisionCloudText firebaseVisionCloudText) {
                                    mRecognizedText = firebaseVisionCloudText.getText();
                                    mTextViewRecognizedText.setText(mRecognizedText);
                                    Log.d(TAG, "recognised text: " + mRecognizedText);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(
                                            MedicalCaseAddActivity.this,
                                            "recognize failed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                    // END
                }
            }
            // END
        }
    }

    // BEGIN
    private void writeToDatabase(MedicalAdviceRecord adviceRecord) {
        CryptoMessageHandler messageHandler = new CryptoMessageHandler(Looper.getMainLooper());
        messageHandler.setCallback(MedicalCaseAddActivity.this);
        Thread encryptThread = new EncryptMedicalAdviceThread(
                adviceRecord,
                mCurrentUser.getUid(),
                getApplicationContext(),
                messageHandler
        );
        encryptThread.start();
    }

    @Override
    public void processCryptoRecord(Object encryptedRecord) {
        MedicalAdviceRecord encryptedAdviceRecord = (MedicalAdviceRecord) encryptedRecord;
        String key = mDatabase.child(mCurrentUser.getUid()).push().getKey();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/"+ Constant.DATABASE_MEDICAL_ADVICE + "/" + mCurrentUser.getUid() + "/"+ key,
                encryptedAdviceRecord.toMap());
        mDatabase.updateChildren(childUpdates);

        hideProgressDialog();

        Intent intent = new Intent(MedicalCaseAddActivity.this,
                MedicalCaseListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
    // END

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    // BEGIN
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            mImageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(mImageBitmap);
            mButtonTakePhoto.setText("Retake a Photo");
            Toast.makeText(MedicalCaseAddActivity.this, "take photo success",
                    Toast.LENGTH_SHORT).show();
        }
    }
    // END
}
