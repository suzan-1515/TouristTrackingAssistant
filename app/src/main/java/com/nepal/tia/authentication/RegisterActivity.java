package com.nepal.tia.authentication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nepal.tia.R;
import com.nepal.tia.model.Role;
import com.nepal.tia.model.User;
import com.nepal.tia.utils.Navigator;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getName();
    private static final int OPEN_DOCUMENT_CODE = 2;

    private TextInputEditText mNameEditText;
    private TextInputEditText mEmailEditText;
    private TextInputEditText mContactEditText;
    private TextInputEditText mPasswordEditText;
    private TextInputEditText mConfirmPasswordEditText;
    private MaterialButton mSignUpButton;
    private ProgressBar mProgressBar;

    private FirebaseAuth mFirebaseAuthentication;
    private CollectionReference mUserCollection;
    private StorageReference mStorageReference;
    private Uri mSelectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mNameEditText = findViewById(R.id.input_name);
        mEmailEditText = findViewById(R.id.input_email);
        mContactEditText = findViewById(R.id.input_conatct);
        mPasswordEditText = findViewById(R.id.input_password);
        mConfirmPasswordEditText = findViewById(R.id.input_reEnterPassword);
        mSignUpButton = findViewById(R.id.btn_signup);
        TextView mSignInButton = findViewById(R.id.link_login);
        mProgressBar = findViewById(R.id.progressBar);

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigator.navigate(RegisterActivity.this,
                        new Intent(RegisterActivity.this, LoginActivity.class)
                );
                finish();
            }
        });

        mFirebaseAuthentication = FirebaseAuth.getInstance();
        mUserCollection = FirebaseFirestore.getInstance().collection("users");
        mStorageReference = FirebaseStorage.getInstance().getReference();

    }

    public void onPhotoButtonClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, OPEN_DOCUMENT_CODE);
    }

    public void onSignUpButtonClick(View view) {
        final String name = mNameEditText.getEditableText().toString().trim();
        final String email = mEmailEditText.getEditableText().toString().trim();
        final String password = mPasswordEditText.getEditableText().toString().trim();
        final String contact = mContactEditText.getEditableText().toString().trim();
        String confirmPassword = mConfirmPasswordEditText.getEditableText().toString().trim();

        if (validateFields(name, email, password, contact, confirmPassword)) {
            mSignUpButton.setEnabled(false);
            mProgressBar.setVisibility(View.VISIBLE);

            if (mSelectedImage != null) {
                uploadImageFile().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            registerUser(name, email, password, contact, downloadUri.toString());
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error uploading image..",
                                    Toast.LENGTH_SHORT).show();
                            mSignUpButton.setEnabled(true);
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            } else {
                registerUser(name, email, password, contact, "");
            }

        }


    }

    private Task<Uri> uploadImageFile() {
        final StorageReference imgRef = mStorageReference.child("images/users/" + mSelectedImage.getLastPathSegment());

        UploadTask uploadTask = imgRef.putFile(mSelectedImage);
        return uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imgRef.getDownloadUrl();
            }
        });
    }

    private void registerUser(final String name, final String email, final String password,
                              final String contact, final String image) {

        mFirebaseAuthentication.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "User already registered with given email",
                                    Toast.LENGTH_LONG).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                            mSignUpButton.setEnabled(true);
                        } else {
                            saveData(name, email, password, contact, image);
                        }
                    }
                });

    }

    private void saveData(final String name, final String email, final String password,
                          final String contact, final String image) {
        mFirebaseAuthentication.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            String userId = Objects.requireNonNull(task.getResult()).getUser().getUid();

                            // Add a new document with a generated ID
                            User user = new User();
                            user.setId(userId);
                            user.setContact(contact);
                            user.setName(name);
                            user.setEmail(email);
                            user.setPassword(password);
                            user.setRole(Role.USER);
                            user.setImage(image);

                            mUserCollection
                                    .document(userId)
                                    .set(user, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mProgressBar.setVisibility(View.GONE);
                                            mSignUpButton.setEnabled(true);
                                            Navigator.navigate(RegisterActivity.this,
                                                    new Intent(RegisterActivity.this, LoginActivity.class)
                                            );
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document", e);
                                            mFirebaseAuthentication.getCurrentUser().delete()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            Toast.makeText(RegisterActivity.this, "Error registering..",
                                                                    Toast.LENGTH_SHORT).show();
                                                            mProgressBar.setVisibility(View.INVISIBLE);
                                                            mSignUpButton.setEnabled(true);
                                                        }
                                                    });

                                        }
                                    });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Error registering..",
                                    Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                            mSignUpButton.setEnabled(true);
                        }
                    }
                });
    }

    private boolean validateFields(String name, String email, String password, String contact,
                                   String confirmPassword) {
        if (TextUtils.isEmpty(name)) {
            mNameEditText.setError("Name cannot be empty!");
            mNameEditText.requestFocus();
            return false;
        } else {
            mNameEditText.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            mEmailEditText.setError("Email cannot be empty!");
            mEmailEditText.requestFocus();
            return false;
        } else {
            mEmailEditText.setError(null);
        }
        if (TextUtils.isEmpty(contact)) {
            mContactEditText.setError("Contact cannot be empty!");
            mContactEditText.requestFocus();
            return false;
        } else {
            mContactEditText.setError(null);
        }

        if (password.length() < 6) {
            mPasswordEditText.setError("Password too short, enter minimum 6 characters!");
            mPasswordEditText.requestFocus();
            return false;
        } else {
            mPasswordEditText.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            mConfirmPasswordEditText.setError("Password do not match!");
            mConfirmPasswordEditText.requestFocus();
            return false;
        } else {
            mConfirmPasswordEditText.setError(null);
        }

        mNameEditText.onEditorAction(EditorInfo.IME_ACTION_DONE);
        mEmailEditText.onEditorAction(EditorInfo.IME_ACTION_DONE);
        mContactEditText.onEditorAction(EditorInfo.IME_ACTION_DONE);
        mPasswordEditText.onEditorAction(EditorInfo.IME_ACTION_DONE);
        mConfirmPasswordEditText.onEditorAction(EditorInfo.IME_ACTION_DONE);

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == OPEN_DOCUMENT_CODE && resultCode == RESULT_OK) {
            if (resultData != null) {
                // this is the image selected by the user
                mSelectedImage = resultData.getData();
            }
        }
    }
}
