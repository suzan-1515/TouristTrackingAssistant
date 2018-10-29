package com.nepal.tia.client;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nepal.tia.R;
import com.nepal.tia.model.ServiceRequest;
import com.nepal.tia.model.ServiceType;
import com.nepal.tia.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

public class RequestServiceActivity extends AppCompatActivity {

    private AppCompatTextView mNameTextView;
    private AppCompatTextView mEmailTextView;
    private AppCompatTextView mContactTextView;
    private TextInputEditText mTitleTextInputEditText;
    private TextInputEditText mDescriptionTextInputEditText;
    private Spinner mServiceTypeSpinner;

    private CollectionReference mRequestReference;
    private CollectionReference mNotifyReference;
    private ProgressDialog mProgressDialog;
    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_service);

        Bundle extras = getIntent().getExtras();
        if (extras == null) finish();

        mCurrentUser = (User) extras.getSerializable("user");

        //Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_request_service);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNameTextView = findViewById(R.id.name);
        mEmailTextView = findViewById(R.id.email);
        mContactTextView = findViewById(R.id.contact);
        mTitleTextInputEditText = findViewById(R.id.input_title);
        mDescriptionTextInputEditText = findViewById(R.id.input_description);
        mServiceTypeSpinner = findViewById(R.id.tourist_spinner);


        mProgressDialog = new ProgressDialog(this,
                R.style.AppTheme_Dark_Dialog);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Saving...");

        mRequestReference = FirebaseFirestore.getInstance().collection("requests");
        mNotifyReference = FirebaseFirestore.getInstance().collection("notify");

        fillTouristProfile(mCurrentUser);
        initServiceTypeSpinner();

    }

    private void fillTouristProfile(User user) {
        if (user != null) {
            mNameTextView.setText(String.format(getString(R.string.value_name_prefix), user.getName()));
            mEmailTextView.setText(String.format(getString(R.string.value_email_prefix), user.getEmail()));
            mContactTextView.setText(String.format(getString(R.string.value_contact_prefix), user.getContact()));
        }
    }

    private void initServiceTypeSpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("Hotel");
        categories.add("Hospital");
        categories.add("Destination");
        categories.add("Rescue");
        categories.add("Police Station");
        categories.add("Guide/Porter");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        mServiceTypeSpinner.setAdapter(dataAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onSendClicked(View view) {
        final String title = mTitleTextInputEditText.getEditableText().toString().trim();
        final String description = mDescriptionTextInputEditText.getEditableText().toString().trim();
        ServiceType serviceType = ServiceType.fromIndex(mServiceTypeSpinner.getSelectedItemPosition());
        if (validateData(title, description)) {

            mProgressDialog.show();
            final ServiceRequest serviceRequest = new ServiceRequest();
            serviceRequest.setId(UUID.randomUUID().getMostSignificantBits());
            serviceRequest.setUser(mCurrentUser);
            serviceRequest.setTitle(title);
            serviceRequest.setDescription(description);
            serviceRequest.setServiceType(serviceType);
            serviceRequest.setTimestamp(Calendar.getInstance().getTimeInMillis());

            mRequestReference
                    .document(String.valueOf(serviceRequest.getId()))
                    .set(serviceRequest)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mNotifyReference
                                        .document(String.valueOf(serviceRequest.getId()))
                                        .set(serviceRequest)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(RequestServiceActivity.this, "Request sent!",
                                                        Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        });

                            } else {
                                Toast.makeText(RequestServiceActivity.this, "Error sending request!",
                                        Toast.LENGTH_SHORT).show();
                            }
                            mProgressDialog.dismiss();
                        }
                    });
        }
    }

    private boolean validateData(String title, String description) {
        if (TextUtils.isEmpty(title)) {
            mTitleTextInputEditText.setError("Title cannot be empty!");
            mTitleTextInputEditText.requestFocus();
            return false;
        } else {
            mTitleTextInputEditText.setError(null);
        }

        if (TextUtils.isEmpty(description)) {
            mDescriptionTextInputEditText.setError("Description cannot be empty!");
            mDescriptionTextInputEditText.requestFocus();
            return false;
        } else {
            mDescriptionTextInputEditText.setError(null);
        }

        return true;
    }
}
