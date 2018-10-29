package com.nepal.tia.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nepal.tia.R;
import com.nepal.tia.admin.adapter.QueriesAdapter;
import com.nepal.tia.model.ServiceRequest;
import com.nepal.tia.model.User;
import com.nepal.tia.utils.Navigator;

import java.util.Objects;

import javax.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminHomeActivity extends AppCompatActivity {

    private static final String TAG = AdminHomeActivity.class.getName();

    private User mCurrentUserData;
    private QueriesAdapter mQueriesAdapter;
    private CollectionReference mRequestReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_admin);

        Bundle extras = getIntent().getExtras();
        mCurrentUserData = (User) Objects.requireNonNull(extras).getSerializable("user");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        //Setup recyclerview and adapter
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mQueriesAdapter = new QueriesAdapter();
        recyclerView.setAdapter(mQueriesAdapter);

        mRequestReference = FirebaseFirestore.getInstance().collection("requests");

        loadQueriesData();
    }

    private void loadQueriesData() {
        mRequestReference
                .limit(5)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        for (DocumentChange dc : snapshot.getDocumentChanges()) {
                            QueryDocumentSnapshot document = dc.getDocument();
                            if (document != null && document.exists()) {
                                Log.d(TAG, "Data loaded: " + document.getData());
                                ServiceRequest serviceRequest = document.toObject(ServiceRequest.class);
                                switch (dc.getType()) {
                                    case ADDED:
                                        mQueriesAdapter.addData(serviceRequest);
                                        break;
                                    case MODIFIED:
                                        mQueriesAdapter.update(serviceRequest);
                                        break;
                                    case REMOVED:
                                        mQueriesAdapter.removeData(serviceRequest);
                                        break;
                                }
                            } else {
                                Log.d(TAG, "Current data: null");
                            }
                        }

                    }
                });
    }

    public void onAddTourist(View view) {
        Log.d(TAG, "onServiceClicked");
        Navigator.navigate(this, new Intent(this, TrackingActivity.class));

    }

    public void onServiceClicked(View view) {
        Log.d(TAG, "onServiceClicked");
        Navigator.navigate(this, new Intent(this, AdminServiceActivity.class));
    }

    public void onViewAllClick(View view) {
        Intent destIntent = new Intent(this, QueriesActivity.class);
        destIntent.putExtra("user", mCurrentUserData);
        Navigator.navigate(this, destIntent);
    }


}
