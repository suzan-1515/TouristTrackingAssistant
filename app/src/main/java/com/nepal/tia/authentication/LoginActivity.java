package com.nepal.tia.authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nepal.tia.R;
import com.nepal.tia.admin.AdminHomeActivity;
import com.nepal.tia.client.ClientHomeActivity;
import com.nepal.tia.model.Role;
import com.nepal.tia.model.User;
import com.nepal.tia.utils.Navigator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputEditText mEmailEditText;
    private TextInputEditText mPasswordEditText;

    private FirebaseAuth mAuth;
    private CollectionReference mUserCollectionRef;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailEditText = findViewById(R.id.input_email);
        mPasswordEditText = findViewById(R.id.input_password);

        mAuth = FirebaseAuth.getInstance();
        mUserCollectionRef = FirebaseFirestore.getInstance().collection("users");

        mProgressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Authenticating...");

    }

    public void signUpButtonClick(View view) {
        // Start the Signup activity
        Navigator.navigate(this,
                new Intent(getApplicationContext(), RegisterActivity.class));
    }

    public void onLoginButtonClick(View view) {
        Log.d(TAG, "Login");

        String email = mEmailEditText.getEditableText().toString().trim();
        String password = mPasswordEditText.getEditableText().toString().trim();


        if (TextUtils.isEmpty(email)) {
            mEmailEditText.setError("Enter email address!");
            mEmailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordEditText.setError("Enter password!");
            mPasswordEditText.requestFocus();
            return;
        }

        mEmailEditText.onEditorAction(EditorInfo.IME_ACTION_DONE);
        mPasswordEditText.onEditorAction(EditorInfo.IME_ACTION_DONE);
        mProgressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            onLoginSuccess(task.getResult().getUser());
                        } else {
                            Log.e(TAG, "Auth error", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });
    }

    private void onLoginSuccess(FirebaseUser firebaseUser) {
        Log.d(TAG, "onLoginSuccess");
        mUserCollectionRef
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        mProgressDialog.dismiss();
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            if (user.getRole() == Role.ADMIN) {
                                Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                                intent.putExtra("user", user);
                                Navigator.navigate(LoginActivity.this,
                                        intent);
                                finish();
                            } else if (user.getRole() == Role.USER) {
                                Intent intent = new Intent(LoginActivity.this, ClientHomeActivity.class);
                                intent.putExtra("user", user);
                                Navigator.navigate(LoginActivity.this,
                                        intent);
                                finish();
                            }
                        } else {
                            Log.d(TAG, "user data not fetched");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error fetching user data", e);
                        mProgressDialog.dismiss();
                    }
                });

    }


}

