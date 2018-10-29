package com.nepal.tia.admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nepal.tia.R;
import com.nepal.tia.model.ServiceInfo;
import com.nepal.tia.model.ServiceType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.PatternsCompat;

public class AddServiceActivity extends AppCompatActivity {

    private static final int OPEN_DOCUMENT_CODE = 2;
    private static final String TAG = AddServiceActivity.class.getName();

    private TextInputEditText mTitleTextInputEditText;
    private TextInputEditText mAddressTextInputEditText;
    private TextInputEditText mContactTextInputEditText;
    private TextInputEditText mEmailTextInputEditText;
    private TextInputEditText mWebsiteTextInputEditText;
    private AppCompatSpinner mTypeSpinner;

    private FirebaseFirestore mFirebaseFirestore;
    private StorageReference mStorageReference;
    private ProgressDialog mProgressDialog;

    private Uri mSelectedImage;
    private ServiceInfo mServiceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            mServiceInfo = (ServiceInfo) extras.getSerializable("service_info");

        //Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(mServiceInfo == null ? R.string.title_add_service : R.string.title_update_service);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTitleTextInputEditText = findViewById(R.id.input_title);
        mAddressTextInputEditText = findViewById(R.id.input_address);
        mContactTextInputEditText = findViewById(R.id.input_contact);
        mEmailTextInputEditText = findViewById(R.id.input_email);
        mWebsiteTextInputEditText = findViewById(R.id.input_website);
        mTypeSpinner = findViewById(R.id.type);
        AppCompatImageButton mImagePicker = findViewById(R.id.image_picker);
        MaterialButton mSaveButton = findViewById(R.id.btn_add);
        if (mServiceInfo != null)
            mSaveButton.setText(R.string.title_update_service);

        mImagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, OPEN_DOCUMENT_CODE);
            }
        });

        initServiceTypeSpinner();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        mProgressDialog = new ProgressDialog(this,
                R.style.AppTheme_Dark_Dialog);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Please wait...");

        if (mServiceInfo != null) {
            fillUpdateData();
        }

    }

    private void fillUpdateData() {
        mTitleTextInputEditText.setText(mServiceInfo.getTitle());
        mAddressTextInputEditText.setText(mServiceInfo.getAddress());
        mEmailTextInputEditText.setText(mServiceInfo.getEmail());
        mWebsiteTextInputEditText.setText(mServiceInfo.getWebsite());
        mContactTextInputEditText.setText(mServiceInfo.getContact());
        mTypeSpinner.setSelection(ServiceType.toIndex(mServiceInfo.getType()));
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
        mTypeSpinner.setAdapter(dataAdapter);
    }

    public void onSaveClicked(View view) {
        final String title = mTitleTextInputEditText.getEditableText().toString().trim();
        final String address = mAddressTextInputEditText.getEditableText().toString().trim();
        final String contact = mContactTextInputEditText.getEditableText().toString().trim();
        final String email = mEmailTextInputEditText.getEditableText().toString().trim();
        final String website = mWebsiteTextInputEditText.getEditableText().toString().trim();

        if (validateFields(title, address, contact, email, website)) {
            mProgressDialog.show();
            if (mSelectedImage != null) {
                uploadImageFile().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            if (mServiceInfo == null)
                                saveData(title, address, contact, email, website, downloadUri.toString());
                            else
                                updateData(title, address, contact, email, website, downloadUri.toString());
                        } else {
                            Toast.makeText(AddServiceActivity.this, "Error uploading image..",
                                    Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });
            } else {
                if (mServiceInfo == null)
                    saveData(title, address, contact, email, website, "");
                else
                    updateData(title, address, contact, email, website, "");

            }
        }
    }

    private Task<Uri> uploadImageFile() {
        final StorageReference imgRef = mStorageReference.child("images/" + mSelectedImage.getLastPathSegment());

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

    private boolean validateFields(String title, String address, String contact, String email, String website) {

        if (TextUtils.isEmpty(title)) {
            mTitleTextInputEditText.setError("Title cannot be empty!");
            mTitleTextInputEditText.requestFocus();
            return false;
        } else {
            mTitleTextInputEditText.setError(null);
        }

        if (TextUtils.isEmpty(address)) {
            mAddressTextInputEditText.setError("Address cannot be empty!");
            mAddressTextInputEditText.requestFocus();
            return false;
        } else {
            mAddressTextInputEditText.setError(null);
        }

        if (!TextUtils.isEmpty(email)) {
            if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
                mEmailTextInputEditText.setError("Invalid email address!");
                mEmailTextInputEditText.requestFocus();
                return false;
            } else {
                mEmailTextInputEditText.setError(null);
            }
        }

        if (!TextUtils.isEmpty(website)) {
            if (!PatternsCompat.AUTOLINK_WEB_URL.matcher(website).matches()) {
                mWebsiteTextInputEditText.setError("Invalid website address");
                mWebsiteTextInputEditText.requestFocus();
                return false;
            } else {
                mWebsiteTextInputEditText.setError(null);
            }
        }

        return true;
    }

    private void saveData(String title, String address, String contact,
                          String email, String website, String image) {
        int serviceIndex = mTypeSpinner.getSelectedItemPosition();
        ServiceType selectedService = ServiceType.fromIndex(serviceIndex);

        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setId(UUID.randomUUID().getMostSignificantBits());
        serviceInfo.setTitle(title);
        serviceInfo.setAddress(address);
        serviceInfo.setContact(contact);
        serviceInfo.setEmail(email);
        serviceInfo.setWebsite(website);
        serviceInfo.setImage(image);
        serviceInfo.setType(selectedService);

        mFirebaseFirestore.collection("services")
                .document(String.valueOf(serviceInfo.getId()))
                .set(serviceInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddServiceActivity.this, "Service data saved!",
                                    Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                            finish();
                        } else {
                            Toast.makeText(AddServiceActivity.this, "Error saving data!",
                                    Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });
    }

    private void updateData(String title, String address, String contact, String email,
                            String website, String image) {
        int serviceIndex = mTypeSpinner.getSelectedItemPosition();
        ServiceType selectedService = ServiceType.fromIndex(serviceIndex);

        mServiceInfo.setTitle(title);
        mServiceInfo.setAddress(address);
        mServiceInfo.setContact(contact);
        mServiceInfo.setEmail(email);
        mServiceInfo.setWebsite(website);
        mServiceInfo.setImage(TextUtils.isEmpty(image) ? mServiceInfo.getImage() : image);
        mServiceInfo.setType(selectedService);

        mFirebaseFirestore.collection("services")
                .document(String.valueOf(mServiceInfo.getId()))
                .set(mServiceInfo, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddServiceActivity.this, "Service updated!",
                                    Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                            finish();
                        } else {
                            Toast.makeText(AddServiceActivity.this, "Error updating service!",
                                    Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });

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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
