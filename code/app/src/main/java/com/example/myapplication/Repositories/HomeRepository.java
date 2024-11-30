package com.example.myapplication.Repositories;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.BaseActivity;
import com.example.myapplication.Models.Event;
import com.example.myapplication.Views.HomeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class HomeRepository extends BaseActivity {
    private final FirebaseFirestore db;
    private final String deviceId;


    public HomeRepository() {
        this.deviceId = retrieveDeviceId();
        db = FirebaseFirestore.getInstance();
    }

    // Method to fetch events where the device is on the waitlist with "waiting" status
    public void fetchWaitingDevices(HomeView homeView) {
        String deviceId = retrieveDeviceId(); // Get device ID dynamically
        List<Event> deviceWaitlistEvents = new ArrayList<>();

        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            event.setEventId(document.getId());

                            List<Map<String, Object>> waitlist = (List<Map<String, Object>>) document.get("waitlist");
                            if (waitlist != null) {
                                for (Map<String, Object> entry : waitlist) {
                                    String entryDeviceId = (String) entry.get("deviceId");
                                    String status = (String) entry.get("status");

                                    if (deviceId.equals(entryDeviceId) && "waiting".equalsIgnoreCase(status)) {
                                        deviceWaitlistEvents.add(event);
                                        break;
                                    }
                                }
                            }
                        }

                        homeView.updateWaitlistEvents(deviceWaitlistEvents);
                    } else {
                        Log.e("FirestoreError", "Error fetching events", task.getException());
                    }
                });
    }
}
