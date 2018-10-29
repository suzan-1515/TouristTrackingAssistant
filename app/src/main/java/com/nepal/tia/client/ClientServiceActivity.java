package com.nepal.tia.client;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nepal.tia.R;
import com.nepal.tia.client.adapter.ClientServiceAdapter;
import com.nepal.tia.client.model.ServiceMenu;
import com.nepal.tia.model.ServiceInfo;

import javax.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ClientServiceActivity extends AppCompatActivity {

    private static final String TAG = ClientServiceActivity.class.getName();
    private ClientServiceAdapter clientServiceAdapter;
    private ServiceMenu mServiceMenu;
    private CollectionReference mServiceReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_client);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mServiceMenu = (ServiceMenu) extras.getSerializable("service");
        }

        //Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(mServiceMenu.getTitle());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Setup recyclerview and adapter
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        clientServiceAdapter = new ClientServiceAdapter();
        recyclerView.setAdapter(clientServiceAdapter);

        //Initialize firebase firestore
        mServiceReference = FirebaseFirestore.getInstance().collection("services");

        loadData();

    }

    private void loadData() {
        Log.d(TAG, "loading data from firestore");
        mServiceReference
                .whereEqualTo("type", mServiceMenu.getType().toString())
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
                                ServiceInfo serviceInfo = document.toObject(ServiceInfo.class);
                                switch (dc.getType()) {
                                    case ADDED:
                                        clientServiceAdapter.addData(serviceInfo);
                                        break;
                                    case MODIFIED:
                                        clientServiceAdapter.update(serviceInfo);
                                        break;
                                    case REMOVED:
                                        clientServiceAdapter.removeData(serviceInfo);
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

}
