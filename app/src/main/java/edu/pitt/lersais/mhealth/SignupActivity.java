package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.services.cloudkms.v1.CloudKMS;
import com.google.api.services.cloudkms.v1.model.CryptoKey;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

import edu.pitt.lersais.mhealth.util.CloudKMSUtil;
import edu.pitt.lersais.mhealth.util.Constant;


/**
 * The SignupActivity is used to handle registration, email-password registration.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class SignupActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "SignupActivity";

    private FirebaseAuth mAuth;

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        mEmailEditText = findViewById(R.id.field_email);
        mPasswordEditText = findViewById(R.id.field_password);
        mConfirmPasswordEditText = findViewById(R.id.field_password_confirm);

        findViewById(R.id.email_create_account_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            registration();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void registration() {
        String email = mEmailEditText.getText().toString();
        final String password = mPasswordEditText.getText().toString();
        Log.d(TAG, "create account: " + email);

        if (!validate()) {
            return;
        }

        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "create user with email success");

                            // Create a key by adopting the service from google cloud KMS
                            FirebaseUser user = mAuth.getCurrentUser();
                            String keyid = user.getUid();
                            try{
                                initializeCryptoKey(keyid);
                            } catch (IOException e){
                                Log.d(TAG, "create the key failed");
                            }

                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        } else {
                            Log.w(TAG, "create user with email failure", task.getException());
                            Toast.makeText(getBaseContext(), "Authentication Failed.",
                                    Toast.LENGTH_LONG).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    private boolean validate() {
        boolean valid = true;

        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String passwordConfirm = mConfirmPasswordEditText.getText().toString();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailEditText.setError("enter a valid email address");
            valid = false;
        } else {
            mEmailEditText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordEditText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mPasswordEditText.setError(null);
        }

        if (passwordConfirm.isEmpty()) {
            mConfirmPasswordEditText.setError("need to confirm your password");
            valid = false;
        } else {
            if (passwordConfirm.equals(password)) {
                mConfirmPasswordEditText.setError(null);
            } else {
                mConfirmPasswordEditText.setError("different confirm password");
            }
        }
        return valid;
    }

    public void initializeCryptoKey(String cryptoKeyId) throws IOException {
        // TODO: create a thread to initialize a key using Google Cloud KMS
        // BEGIN
        final String keyid = cryptoKeyId;
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {

                    CloudKMS kms = CloudKMSUtil.getInstance().createAuthorizedClient(getApplicationContext());

                    // The resource name of the location associated with the KeyRing.
                    String parent = String.format(
                            "projects/%s/locations/%s/keyRings/%s",
                            Constant.KMS_PROJECT_ID,
                            Constant.KMS_LOCATION,
                            Constant.KMS_KEY_RING_ID);

                    // This will allow the API access to the key for encryption and decryption.
                    String purpose = "ENCRYPT_DECRYPT";
                    CryptoKey cryptoKey = new CryptoKey();
                    cryptoKey.setPurpose(purpose);

                    // Create the CryptoKey for your project.

                    CryptoKey createdKey = kms.projects().locations().keyRings().cryptoKeys()
                            .create(parent, cryptoKey)
                            .setCryptoKeyId(keyid)
                            .execute();
                    System.out.println(createdKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        // END
    }
}
