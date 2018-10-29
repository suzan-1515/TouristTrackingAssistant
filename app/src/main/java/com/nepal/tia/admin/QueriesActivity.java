package com.nepal.tia.admin;

import android.os.Bundle;

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

import javax.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class QueriesActivity extends AppCompatActivity {

    private CollectionReference mRequestReference;
    private QueriesAdapter mQueriesAdapter;
    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queries);

        Bundle extras = getIntent().getExtras();
        if (extras == null) finish();

        mCurrentUser = (User) extras.getSerializable("user");

        //Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_queries);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        for (DocumentChange dc : snapshot.getDocumentChanges()) {
                            QueryDocumentSnapshot document = dc.getDocument();
                            if (document != null && document.exists()) {
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
}
