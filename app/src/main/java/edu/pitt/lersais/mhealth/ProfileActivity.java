package edu.pitt.lersais.mhealth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import edu.pitt.lersais.mhealth.util.DownloadImageTask;

/**
 * The SettingActivity that is used to handle profile features such as update display name and
 * profile photo.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Profile_Activity";
    private static final int REQUEST_CODE_FOR_GALLERY = 520;

    private TextView uidTextView;
    private EditText nameEditText;
    private ImageView photoImageView;

    private FirebaseStorage mStorage;
    private FirebaseAuth mAuth;
    private StorageReference mStorageReference;
    private StorageReference mProfileImagesReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            mProfileImagesReference = mStorageReference.child("images/profile/"+currentUser.getUid()+".jpeg");

            uidTextView = findViewById(R.id.text_view_uid);
            uidTextView.setText(currentUser.getUid());
            nameEditText = findViewById(R.id.edit_text_display_name);
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                nameEditText.setText(currentUser.getDisplayName());
            }
            photoImageView = findViewById(R.id.image_view_profile_photo);
            if (currentUser.getPhotoUrl() != null) {
                Uri photoUrl = currentUser.getPhotoUrl();
                new DownloadImageTask(photoImageView).execute(photoUrl.toString());
            }

            findViewById(R.id.button_update_profile).setOnClickListener(this);
            findViewById(R.id.button_chose_photo).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_update_profile) {
            // upload the photo first
            photoImageView.setDrawingCacheEnabled(true);
            photoImageView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable)photoImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = mProfileImagesReference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, "Upload Photo Failure",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(ProfileActivity.this, "Upload Photo Success",
                            Toast.LENGTH_SHORT).show();
                }
            });
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return mProfileImagesReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Toast.makeText(ProfileActivity.this, "Get photo download url success",
                                Toast.LENGTH_LONG).show();

                        String name = nameEditText.getText().toString();
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(downloadUri)
                                .build();
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "MedicalHistoryRecord profile updated.");
                                    Toast.makeText(ProfileActivity.this, "profile update success",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                    finish();
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                                }
                            }
                        });
                    }
                    else {
                        Toast.makeText(ProfileActivity.this, "Get photo download url failed",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });

        } else if (i == R.id.button_chose_photo) {
            startActivityForResult(
                    new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                    REQUEST_CODE_FOR_GALLERY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FOR_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImageUri = data.getData();
            photoImageView.setImageURI(selectedImageUri);
            Toast.makeText(ProfileActivity.this, "select profile photo success",
                    Toast.LENGTH_SHORT).show();
//            Bitmap bitmap = null;
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
//                photoImageView.setImageBitmap(bitmap);
//            } catch (FileNotFoundException fnfe) {
//                fnfe.printStackTrace();
//            } catch (IOException ioe) {
//                ioe.printStackTrace();
//            }
        }
    }
}
