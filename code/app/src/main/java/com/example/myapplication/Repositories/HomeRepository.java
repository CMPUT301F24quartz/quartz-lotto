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
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class HomeRepository extends BaseActivity {
    private final FirebaseFirestore db;
    private final String deviceId;



    public HomeRepository() {
        this.deviceId = "TA9jSDvfKDX6wP3ueVO5nTwrf4D3";
        db = FirebaseFirestore.getInstance();
    }

    // Method to fetch events where the device is on the waitlist with "waiting" status
    public void fetchWaitingDevices(HomeView homeView) {
        String deviceId = "TA9jSDvfKDX6wP3ueVO5nTwrf4D3"; // Sample device ID
        List<Event> deviceWaitlistEvents = new ArrayList<>(); // List to store Event objects for display

        // Query all events from the "events" collection
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Loop through all events
                        for (QueryDocumentSnapshot eventDoc : task.getResult()) {
                            Event event = eventDoc.toObject(Event.class);
                            event.setEventId(eventDoc.getId()); // Ensure the event has an ID

                            // Query the waitlist subcollection for each event
                            db.collection("events")
                                    .document(event.getEventId())
                                    .collection("waitlist")
                                    .get()
                                    .addOnCompleteListener(waitlistTask -> {
                                        if (waitlistTask.isSuccessful()) {
                                            // Loop through the waitlist documents
                                            for (QueryDocumentSnapshot waitlistDoc : waitlistTask.getResult()) {
                                                String entryDeviceId = waitlistDoc.getString("deviceId");
                                                String status = waitlistDoc.getString("status");

                                                // Check if this device is on the waitlist with a "waiting" status
                                                if (deviceId.equals(entryDeviceId) && "waiting".equals(status)) {
                                                    // Set event name and date directly in the event object
                                                    event.setEventName(eventDoc.getString("eventName")); // Assuming Event class has setEventName() method
                                                    event.setEventDateTime(eventDoc.getString("eventDateTime")); // Assuming Event class has setEventDateTime() method

                                                    // Add the event to the list of waiting events
                                                    deviceWaitlistEvents.add(event);
                                                    break; // Only add the event once for this device
                                                }
                                            }

                                            // Once all events and waitlists have been processed, update the view
                                            homeView.updateWaitlistEvents(deviceWaitlistEvents);
                                        } else {
                                            Log.e("FirestoreError", "Error fetching waitlist", waitlistTask.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.e("FirestoreError", "Error fetching events", task.getException());
                    }
                });
    }


}
