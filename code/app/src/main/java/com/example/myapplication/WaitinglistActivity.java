package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Waitlist only currently shows one member at a time. Need to fix this bug for halfway.
 */
public class WaitinglistActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAttendees;
    private AttendeeAdapter attendeesAdapter;
    private List<Attendee> attendees = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitinglist);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        eventId = getIntent().getStringExtra("event_id");

        recyclerViewAttendees = findViewById(R.id.recyclerViewAttendees);
        recyclerViewAttendees.setLayoutManager(new LinearLayoutManager(this));

        attendeesAdapter = new AttendeeAdapter(attendees, false, true);
        recyclerViewAttendees.setAdapter(attendeesAdapter);

        if (eventId != null) {
            loadEventWaitlist(eventId);
        }

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        Button tabAttendees = findViewById(R.id.tabAttendees);
        tabAttendees.setOnClickListener(view -> {
            Intent intent = new Intent(this, AttendingActivity.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });
    }

//    private void loadNotChosenAttendees() {
//        CollectionReference usersRef = db.collection("Users");
//
//        Query query = usersRef.whereEqualTo("status", "not chosen");
//
//        query.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                attendees.clear();
//
//                for (QueryDocumentSnapshot document : task.getResult()) {
//                    String userID = document.getId();
//                    String name = document.getString("name");
//                    String status = document.getString("status");
//
//                    Attendee attendee = new Attendee(userID, name, status);
//                    attendees.add(attendee);
//                }
//
//                attendeesAdapter.notifyDataSetChanged();
//            }
//        });
//    }

    public void loadEventWaitlist(String eventId) {
        DocumentReference eventRef = db.collection("Events").document(eventId);

        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Retrieve the waitlist_id
                    String waitListId = document.getString("waitlist_id");

                    if (waitListId != null) {
                        // Fetch the waitlist document from the Waitlists collection
                        DocumentReference waitRef = db.collection("Waitlists").document(waitListId);

                        waitRef.get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                DocumentSnapshot document_wait = task1.getResult();
                                if (document_wait != null && document_wait.exists()) {
                                    List<Attendee> selectedEntries = new ArrayList<>();

                                    // Iterate through each field in the waitlist document
                                    for (String key : document_wait.getData().keySet()) {
                                        // Skip non-user fields
                                        if (key.startsWith("user_")) {
                                            // Each user field is an array with [username, status]
                                            List<Object> userData = (List<Object>) document_wait.get(key);
                                            if (userData != null && userData.size() > 1) {
                                                String username = (String) userData.get(0);
                                                String status = (String) userData.get(1);

                                                // Only add attendees with "not chosen" status
                                                if ("not chosen".equals(status)) {
                                                    Attendee attendee = new Attendee("", username, status);
                                                    selectedEntries.add(attendee);
                                                }
                                            }
                                        }
                                    }

                                    // Update the adapter's data with the selected entries
                                    attendeesAdapter = new AttendeeAdapter(selectedEntries, true, false);
                                    recyclerViewAttendees.setAdapter(attendeesAdapter);
                                    attendeesAdapter.notifyDataSetChanged();
                                } else {
                                    Log.d("WaitinglistActivity", "Waitlist document does not exist.");
                                }
                            } else {
                                Log.e("WaitinglistActivity", "Error getting waitlist document: ", task1.getException());
                            }
                        });
                    } else {
                        Log.d("WaitinglistActivity", "No waitlist_id found in event.");
                    }
                } else {
                    Log.d("WaitinglistActivity", "Event document does not exist.");
                }
            } else {
                Log.e("WaitinglistActivity", "Error getting event document: ", task.getException());
            }
        });
    }
    /**
     * Loads attendees based on the selected entries.
     * @param selectedEntries
     */
    private void loadAttendees(List<Object> selectedEntries) {
        for (Object userId : selectedEntries) {
            // fetch user data based on userId
            fetchUserData(userId.toString());
        }
    }

    /**
     * Fetches user data based on the provided user ID.
     * @param userId
     */
    private void fetchUserData(String userId) {
        db.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot userDoc = task.getResult();
                String name = userDoc.getString("name");
                Attendee attendee = new Attendee(userId, name, "waiting");
                attendees.add(attendee);
                attendeesAdapter.notifyDataSetChanged();
            }
        });
    }
}
