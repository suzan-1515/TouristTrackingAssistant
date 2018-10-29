package com.nepal.tia.admin;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nepal.tia.R;
import com.nepal.tia.model.Role;
import com.nepal.tia.model.TrackingInfo;
import com.nepal.tia.model.User;
import com.nepal.tia.utils.DateUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class AddTrackingActivity extends AppCompatActivity {

    private AppCompatSpinner mTouristSpinner;
    private AppCompatTextView mNameTextView;
    private AppCompatTextView mEmailTextView;
    private AppCompatTextView mContactTextView;
    private TextInputEditText mStartDateTextInputEditText;
    private TextInputEditText mEndDateTextInputEditText;

    private Calendar startDate;
    private Calendar endDate;
    private User mSelectedTourist;
    private ProgressDialog mProgressDialog;
    private CollectionReference mUserCollectionReference;
    private CollectionReference mTrackingCollectionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tracking);

        //Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_add_tracking);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTouristSpinner = findViewById(R.id.tourist_spinner);
        mNameTextView = findViewById(R.id.name);
        mEmailTextView = findViewById(R.id.email);
        mContactTextView = findViewById(R.id.contact);
        mStartDateTextInputEditText = findViewById(R.id.input_start_date);
        mEndDateTextInputEditText = findViewById(R.id.input_end_date);
        MaterialButton mSaveButton = findViewById(R.id.btn_add);

        mTouristSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedTourist = (User) mTouristSpinner.getAdapter().getItem(position);
                fillTouristProfile(mSelectedTourist);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mStartDateTextInputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                startDate = Calendar.getInstance();
                                startDate.set(Calendar.YEAR, year);
                                startDate.set(Calendar.MONTH, monthOfYear);
                                startDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                mStartDateTextInputEditText.setText(
                                        DateUtils.format(startDate));
                            }
                        },
                        now.get(Calendar.YEAR), // Initial year selection
                        now.get(Calendar.MONTH), // Initial month selection
                        now.get(Calendar.DAY_OF_MONTH) // Inital day selection
                );
                dpd.setTitle("Set Start Date");
                dpd.setMinDate(now);
                dpd.setOkColor(ContextCompat.getColor(getApplicationContext(), R.color.iron));
                dpd.setCancelColor(ContextCompat.getColor(getApplicationContext(), R.color.iron));
                dpd.show(getFragmentManager(), "StartDatepickerdialog");
            }
        });

        mEndDateTextInputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                endDate = Calendar.getInstance();
                                endDate.set(Calendar.YEAR, year);
                                endDate.set(Calendar.MONTH, monthOfYear);
                                endDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);


                                mEndDateTextInputEditText.setText(
                                        DateUtils.format(endDate));
                            }
                        },
                        now.get(Calendar.YEAR), // Initial year selection
                        now.get(Calendar.MONTH), // Initial month selection
                        now.get(Calendar.DAY_OF_MONTH) // Inital day selection
                );
                dpd.setTitle("Set End Date");
                dpd.setMinDate(now);
                dpd.setOkColor(ContextCompat.getColor(getApplicationContext(), R.color.iron));
                dpd.setCancelColor(ContextCompat.getColor(getApplicationContext(), R.color.iron));
                dpd.show(getFragmentManager(), "EndDatepickerdialog");
            }
        });

        mProgressDialog = new ProgressDialog(this,
                R.style.AppTheme_Dark_Dialog);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Saving...");

        mUserCollectionReference = FirebaseFirestore.getInstance().collection("users");
        mTrackingCollectionReference = FirebaseFirestore.getInstance().collection("tracking");

        loadTouristData();

    }

    private void fillTouristProfile(User user) {
        if (user != null) {
            mNameTextView.setText(String.format(getString(R.string.value_name_prefix), user.getName()));
            mEmailTextView.setText(String.format(getString(R.string.value_email_prefix), user.getEmail()));
            mContactTextView.setText(String.format(getString(R.string.value_contact_prefix), user.getContact()));
        }
    }

    private void loadTouristData() {
        final List<User> users = new ArrayList<>();

        mUserCollectionReference
                .whereEqualTo("role", Role.USER.toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            for (DocumentSnapshot snapshot : documents) {
                                User user = snapshot.toObject(User.class);
                                users.add(user);
                            }
                            fillTouristSpinner(users);
                        } else {
                            Toast.makeText(AddTrackingActivity.this, "Error fetching tourist data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void fillTouristSpinner(List<User> users) {
        ArrayAdapter<User> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                users);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        mTouristSpinner.setAdapter(dataAdapter);

    }

    public void onSaveClicked(View view) {
        if (mSelectedTourist == null) {
            Toast.makeText(this, "Please select tourist first", Toast.LENGTH_SHORT).show();
            return;
        }

        final String sd = mStartDateTextInputEditText.getEditableText().toString().trim();
        final String ed = mEndDateTextInputEditText.getEditableText().toString().trim();

        if (TextUtils.isEmpty(sd)) {
            mStartDateTextInputEditText.setError("Start date not selected!");
            return;
        } else {
            mStartDateTextInputEditText.setError(null);
        }

        if (TextUtils.isEmpty(ed)) {
            mEndDateTextInputEditText.setError("End date not selected!");
            return;
        } else {
            mEndDateTextInputEditText.setError(null);
        }

        mProgressDialog.show();

        final TrackingInfo trackingInfo = new TrackingInfo();
        trackingInfo.setId(UUID.randomUUID().getMostSignificantBits());
        trackingInfo.setUser(mSelectedTourist);
        trackingInfo.setStartDate(startDate.getTimeInMillis());
        trackingInfo.setEndDate(endDate.getTimeInMillis());

        mTrackingCollectionReference
                .document(mSelectedTourist.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && task.getResult().exists()) {
                                Toast.makeText(AddTrackingActivity.this, "Tourist already added for tracking"
                                        , Toast.LENGTH_SHORT)
                                        .show();
                                mProgressDialog.dismiss();
                            } else {
                                mTrackingCollectionReference
                                        .document(mSelectedTourist.getId())
                                        .set(trackingInfo)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(AddTrackingActivity.this, "Tourist tracking enabled"
                                                            , Toast.LENGTH_SHORT)
                                                            .show();
                                                    finish();
                                                } else {
                                                    Toast.makeText(AddTrackingActivity.this, "Error adding tourist for tracking"
                                                            , Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                                mProgressDialog.dismiss();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(AddTrackingActivity.this, "Error adding tourist for tracking"
                                    , Toast.LENGTH_SHORT)
                                    .show();
                            mProgressDialog.dismiss();
                        }
                    }
                });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
