package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * The SettingActivity that is used to handle setting features such as email verification and
 * password reset.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Setting_Activity";
    private static final int REQUEST_CODE_FOR_GALLERY = 5201314;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private EditText editTextCurrentPwd;
    private EditText editTextNewPwd;
    private EditText editTextConfirmPwd;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else {
            TextView textViewUID = findViewById(R.id.text_view_uid);
            textViewUID.setText(currentUser.getUid());
            TextView textViewEmail = findViewById(R.id.setting_email);
            textViewEmail.setText(currentUser.getEmail());
            TextView textViewEmailStatus = findViewById(R.id.setting_email_status);
            if (currentUser.isEmailVerified()) {
                textViewEmailStatus.setText("EMAIL NOT VERIFIED");
                findViewById(R.id.setting_button_verify_email).setOnClickListener(this);
                findViewById(R.id.setting_button_reset_password_email).setEnabled(false);
            } else {
                textViewEmailStatus.setText("EMAIL VERIFIED");
                findViewById(R.id.setting_button_verify_email).setEnabled(false);
            }
            findViewById(R.id.setting_button_reset_password).setOnClickListener(this);
            findViewById(R.id.setting_button_reset_password_email).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.setting_button_verify_email) {
            currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(SettingActivity.this, "Verification email is sent.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else if (i == R.id.setting_button_reset_password) {
            editTextCurrentPwd = findViewById(R.id.setting_edit_current_pwd);
            editTextNewPwd = findViewById(R.id.setting_edit_new_pwd);
            editTextConfirmPwd = findViewById(R.id.setting_edit_confirm_pwd);

            String email = currentUser.getEmail();
            String password = editTextCurrentPwd.getText().toString();
            final String newPassword = editTextNewPwd.getText().toString();
            String confirmPassword = editTextConfirmPwd.getText().toString();

            if (password.isEmpty()) {
                editTextCurrentPwd.setError("Require your current password");
                Toast.makeText(SettingActivity.this, "Require your current password.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (newPassword.isEmpty()) {
                editTextNewPwd.setError("Require your new password");
                Toast.makeText(SettingActivity.this, "Require your new password.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (confirmPassword.isEmpty()) {
                editTextConfirmPwd.setError("Require your confirm password");
                Toast.makeText(SettingActivity.this, "Require your confirm password.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (!confirmPassword.equals(newPassword)) {
                Toast.makeText(SettingActivity.this, "Different confirm password",
                        Toast.LENGTH_LONG).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this,
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                mAuth.getCurrentUser().updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SettingActivity.this, "Password Update Successfully",
                                                    Toast.LENGTH_LONG).show();
                                            mAuth.signOut();
                                            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                                            finish();
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(SettingActivity.this, "Password Update Failed",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                editTextCurrentPwd.setError("Your current password is wrong");
                                Toast.makeText(SettingActivity.this, "Your current password is wrong",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else if (i == R.id.setting_button_reset_password_email) {
            mAuth.sendPasswordResetEmail(currentUser.getEmail())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SettingActivity.this, "Password Reset Email Sent Successfully",
                                        Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(SettingActivity.this, "Password Reset Email Sent Failed",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
