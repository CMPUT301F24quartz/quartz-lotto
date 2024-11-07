package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class OrgHomePageActivity extends AppCompatActivity {
    private Button createEventButton;
    private OrgEventRepo eventArrayAdapter;
    private ArrayList<Event> eventDataList;
    private ListView eventListView;
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.org_home_page);

        // Initialize Firestore and collection reference
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");

        // Initialize UI components
        createEventButton = findViewById(R.id.create_event_button);
        eventListView = findViewById(R.id.event_list);

        // Initialize ArrayList and Adapter
        eventDataList = new ArrayList<>();
        eventArrayAdapter = new OrgEventRepo(this, eventDataList);

        // Set up ListView with the adapter
        eventListView.setAdapter(eventArrayAdapter);

        // Button to navigate to CreateEventActivity
        createEventButton.setOnClickListener(view -> {
            Intent intent = new Intent(OrgHomePageActivity.this, CreateEventActivity.class);
            startActivity(intent);
        });

        // Set up Firestore real-time listener
        setupFirestoreListener();
    }

    private void setupFirestoreListener() {
        eventsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }

                if (querySnapshots != null) {
                    eventDataList.clear(); // Clear the list to avoid duplicates
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        // Get event details from Firestore document
                        String eventName = doc.getString("eventName");
                        String date = doc.getString("date");
                        String time = doc.getString("time");
                        String description = doc.getString("description");

                        // Check for null values before calling intValue()
                        Long maxAttendeesLong = doc.getLong("maxAttendees");
                        int maxAttendees = (maxAttendeesLong != null) ? maxAttendeesLong.intValue() : 0;  // Default to 0 if null

                        Long maxWaitlistLong = doc.getLong("maxWaitlist");
                        int maxWaitlist = (maxWaitlistLong != null) ? maxWaitlistLong.intValue() : 0;  // Default to 0 if null

                        Boolean geolocationEnabled = doc.getBoolean("geolocationEnabled");
                        if (geolocationEnabled == null) {
                            geolocationEnabled = false; // Default to false if null
                        }

                        // Create an Event object and add to the list
                        Event event = new Event(eventName, date, time, description, maxAttendees, maxWaitlist, geolocationEnabled);
                        eventDataList.add(event);
                    }
                    // Notify the adapter that the data has changed
                    eventArrayAdapter.updateEventDataList(eventDataList);
                }
            }
        });
    }
}