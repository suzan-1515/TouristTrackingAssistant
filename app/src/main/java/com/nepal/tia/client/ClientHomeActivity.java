package com.nepal.tia.client;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.nepal.tia.R;
import com.nepal.tia.client.model.ServiceMenu;
import com.nepal.tia.model.ServiceType;
import com.nepal.tia.model.TrackingInfo;
import com.nepal.tia.model.User;
import com.nepal.tia.utils.DateUtils;
import com.nepal.tia.utils.Navigator;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

public class ClientHomeActivity extends AppCompatActivity {

    private static final String TAG = ClientHomeActivity.class.getName();

    private TextView mTrackingStartDateTextView;
    private TextView mTrackingEndDateTextView;
    private TextView mWelcomeTextView;
    private View mTrackingContainer;

    private User mCurrentUserData;
    private CollectionReference mTrackingReference;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_client);

        Bundle extras = getIntent().getExtras();
        mCurrentUserData = (User) Objects.requireNonNull(extras).getSerializable("user");
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        FloatingActionButton mFloatingActionButton = findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent destIntent = new Intent(v.getContext(), RequestServiceActivity.class);
                destIntent.putExtra("user", mCurrentUserData);
                Navigator.navigate(v.getContext(), destIntent);
            }
        });

        mWelcomeTextView = findViewById(R.id.welcome_message);
        mTrackingContainer = findViewById(R.id.tracking_info);
        mTrackingStartDateTextView = findViewById(R.id.tracking_start_date);
        mTrackingEndDateTextView = findViewById(R.id.tracking_end_date);

        mWelcomeTextView.setText(String.format(
                getString(R.string.title_welcome_user_message), mCurrentUserData.getName()));

        mTrackingReference = FirebaseFirestore.getInstance().collection("tracking");
        sharedPreferences = getSharedPreferences(getClass().getName(), MODE_PRIVATE);

        loadTrackingInfo();
    }

    private void loadTrackingInfo() {
        mTrackingReference
                .document(mCurrentUserData.getId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            TrackingInfo trackingInfo = snapshot.toObject(TrackingInfo.class);
                            updateTrackingInfo(trackingInfo);
                            if (sharedPreferences.getBoolean("notify", true)) {
                                sendNotification(trackingInfo);
                                sharedPreferences.edit().putBoolean("notify", false).apply();
                            }
                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    }
                });
    }

    private void removeTrackingInfo() {
        mTrackingContainer.setVisibility(View.GONE);
    }

    private void updateTrackingInfo(TrackingInfo trackingInfo) {
        mTrackingContainer.setVisibility(View.VISIBLE);
        mTrackingStartDateTextView.setText(
                String.format(getString(R.string.value_tracking_start_prefic),
                        DateUtils.format(trackingInfo.getStartDate()))
        );
        mTrackingEndDateTextView.setText(
                String.format(getString(R.string.value_tracking_end_prefic),
                        DateUtils.format(trackingInfo.getEndDate()))
        );
    }

    public void onHotelClick(View view) {
        Log.d(TAG, "onHotelClick");
        Intent destIntent = new Intent(this, ClientServiceActivity.class);
        destIntent.putExtra("service",
                new ServiceMenu(1, getString(R.string.title_hotel), ServiceType.HOTEL));
        Navigator.navigate(this, destIntent);
    }

    public void onHospitalClick(View view) {
        Log.d(TAG, "onHospitalClick");
        Intent destIntent = new Intent(this, ClientServiceActivity.class);
        destIntent.putExtra("service",
                new ServiceMenu(2, getString(R.string.title_hospital), ServiceType.HOSPITAL));
        Navigator.navigate(this, destIntent);
    }

    public void onRescueClick(View view) {
        Log.d(TAG, "onRescueClick");
        Intent destIntent = new Intent(this, ClientServiceActivity.class);
        destIntent.putExtra("service",
                new ServiceMenu(3, getString(R.string.title_rescue), ServiceType.RESCUE));
        Navigator.navigate(this, destIntent);
    }

    public void onPoliceStationClick(View view) {
        Log.d(TAG, "onPoliceStationClick");
        Intent destIntent = new Intent(this, ClientServiceActivity.class);
        destIntent.putExtra("service",
                new ServiceMenu(4, getString(R.string.title_police), ServiceType.POLICE_STATION));
        Navigator.navigate(this, destIntent);
    }

    public void onDestinationClick(View view) {
        Log.d(TAG, "onDestinationClick");
        Intent destIntent = new Intent(this, ClientServiceActivity.class);
        destIntent.putExtra("service",
                new ServiceMenu(5, getString(R.string.title_destination), ServiceType.DESTINATION));
        Navigator.navigate(this, destIntent);
    }

    public void onGuideClick(View view) {
        Log.d(TAG, "onGuideClick");
        Intent destIntent = new Intent(this, ClientServiceActivity.class);
        destIntent.putExtra("service",
                new ServiceMenu(6, getString(R.string.title_guide), ServiceType.GUIDE));
        Navigator.navigate(this, destIntent);
    }

    private void sendNotification(TrackingInfo trackingInfo) {
        Log.d(TAG, "Tracking Notify " + trackingInfo.getStartDate());

        final String notification_title = "Tracking Started!";
        final String notification_msg = "Your tracking has been started from " +
                DateUtils.format(trackingInfo.getStartDate()) + " to " +
                DateUtils.format(trackingInfo.getEndDate());

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "channel_id_01";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent intent = new Intent();
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(notification_title)
                .setContentText(notification_msg)
                .setContentInfo("Urgent");
        int mNotificationId = (int) System.currentTimeMillis();
        notificationManager.notify(mNotificationId, notificationBuilder.build());

    }

}
