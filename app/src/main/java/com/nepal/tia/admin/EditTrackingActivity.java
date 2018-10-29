package com.nepal.tia.admin;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.nepal.tia.R;
import com.nepal.tia.model.TrackingInfo;
import com.nepal.tia.utils.DateUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class EditTrackingActivity extends AppCompatActivity {

    private AppCompatTextView mNameTextView;
    private AppCompatTextView mEmailTextView;
    private AppCompatTextView mContactTextView;
    private TextInputEditText mStartDateTextInputEditText;
    private TextInputEditText mEndDateTextInputEditText;

    private Calendar startDate;
    private Calendar endDate;
    private TrackingInfo mSelectedTrackingInfo;
    private ProgressDialog mProgressDialog;
    private CollectionReference mTrackingCollectionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tracking);

        Bundle extras = getIntent().getExtras();
        mSelectedTrackingInfo = (TrackingInfo) extras.getSerializable("tracking_info");
        if (mSelectedTrackingInfo == null) finish();

        //Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_update_tracking);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNameTextView = findViewById(R.id.name);
        mEmailTextView = findViewById(R.id.email);
        mContactTextView = findViewById(R.id.contact);
        mStartDateTextInputEditText = findViewById(R.id.input_start_date);
        mEndDateTextInputEditText = findViewById(R.id.input_end_date);

        startDate = DateUtils.convert(mSelectedTrackingInfo.getStartDate());
        endDate = DateUtils.convert(mSelectedTrackingInfo.getEndDate());

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
                        startDate.get(Calendar.YEAR), // Initial year selection
                        startDate.get(Calendar.MONTH), // Initial month selection
                        startDate.get(Calendar.DAY_OF_MONTH) // Inital day selection
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
                        endDate.get(Calendar.YEAR), // Initial year selection
                        endDate.get(Calendar.MONTH), // Initial month selection
                        endDate.get(Calendar.DAY_OF_MONTH) // Inital day selection
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
        mProgressDialog.setMessage("Updating...");

        mTrackingCollectionReference = FirebaseFirestore.getInstance().collection("tracking");

        fillTouristProfile();

    }

    private void fillTouristProfile() {
        mNameTextView.setText(String.format(getString(R.string.value_name_prefix), mSelectedTrackingInfo.getUser().getName()));
        mEmailTextView.setText(String.format(getString(R.string.value_email_prefix), mSelectedTrackingInfo.getUser().getEmail()));
        mContactTextView.setText(String.format(getString(R.string.value_contact_prefix), mSelectedTrackingInfo.getUser().getContact()));
        mStartDateTextInputEditText.setText(DateUtils.format(startDate));
        mEndDateTextInputEditText.setText(DateUtils.format(endDate));

    }

    public void onUpdateClicked(View view) {
        mProgressDialog.show();

        mSelectedTrackingInfo.setStartDate(startDate.getTimeInMillis());
        mSelectedTrackingInfo.setEndDate(endDate.getTimeInMillis());

        mTrackingCollectionReference.document(mSelectedTrackingInfo.getUser().getId())
                .set(mSelectedTrackingInfo, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditTrackingActivity.this, "Record updated!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditTrackingActivity.this, "Error updating record!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        mProgressDialog.dismiss();
                        finish();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
