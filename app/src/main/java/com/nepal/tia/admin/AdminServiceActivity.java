package com.nepal.tia.admin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nepal.tia.R;
import com.nepal.tia.admin.adapter.AdminServiceAdapter;
import com.nepal.tia.event.OnItemActionListener;
import com.nepal.tia.model.ServiceInfo;
import com.nepal.tia.utils.Navigator;
import com.nepal.tia.utils.ToastUtils;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminServiceActivity extends AppCompatActivity
        implements OnItemActionListener<ServiceInfo> {

    private static final String TAG = AdminServiceActivity.class.getName();
    private AdminServiceAdapter adminServiceAdapter;
    private CollectionReference mServiceReference;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_admin);

        //Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_services);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton mFloatingActionButton = findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigator.navigate(v.getContext(), new Intent(v.getContext(), AddServiceActivity.class));
            }
        });

        //Setup recyclerview and adapter
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adminServiceAdapter = new AdminServiceAdapter(this);
        recyclerView.setAdapter(adminServiceAdapter);

        //Initialize firebase firestore
        mServiceReference = FirebaseFirestore.getInstance().collection("services");

        mProgressDialog = new ProgressDialog(this,
                R.style.AppTheme_Dark_Dialog);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Processing...");

        loadData();
    }

    private void loadData() {
        Log.d(TAG, "loadData");

        mServiceReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                for (DocumentChange dc : snapshot.getDocumentChanges()) {
                    QueryDocumentSnapshot document = dc.getDocument();
                    if (document != null && document.exists()) {
                        ServiceInfo serviceInfo = document.toObject(ServiceInfo.class);
                        switch (dc.getType()) {
                            case ADDED:
                                adminServiceAdapter.addData(serviceInfo);
                                break;
                            case MODIFIED:
                                adminServiceAdapter.update(serviceInfo);
                                break;
                            case REMOVED:
                                adminServiceAdapter.removeData(serviceInfo);
                                break;
                        }
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                }
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onItemEditAction(View v, int position, ServiceInfo serviceInfo) {
        Intent destIntent = new Intent(this, AddServiceActivity.class);
        destIntent.putExtra("service_info", serviceInfo);
        Navigator.navigate(this, destIntent);
    }

    @Override
    public void onItemDeleteAction(View v, int position, final ServiceInfo serviceInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete?");
        builder.setMessage("Are you sure, you want to delete this service record?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
                mProgressDialog.show();
                mServiceReference.document(String.valueOf(serviceInfo.getId()))
                        .delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    ToastUtils.show(AdminServiceActivity.this, "Record deleted!");
                                } else {
                                    ToastUtils.show(AdminServiceActivity.this, "Error deleting record!");
                                }
                                mProgressDialog.dismiss();
                            }
                        });
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
            }

        });

        builder.show();
    }
}
