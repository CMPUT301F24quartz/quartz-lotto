package com.example.myapplication;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity to display detailed information about a specific event.
 *
 * <p>This activity retrieves event details from Firestore using an event ID and displays
 * the event's name, date, time, description, and poster image. If the event ID is not found or
 * an error occurs, a message is displayed to the user.</p>
 */
public class EventDetailsActivity extends AppCompatActivity {

    private TextView eventNameTextView, dateTextView, timeTextView, descriptionTextView;
    private ImageView posterImageView;
    private FirebaseFirestore db;
    private String eventId;

    /**
     * Initializes the activity, retrieves the event ID from the intent, and loads the event details.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the most recent data.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        db = FirebaseFirestore.getInstance();

        eventNameTextView = findViewById(R.id.eventNameTextView);
        dateTextView = findViewById(R.id.dateTextView);
        timeTextView = findViewById(R.id.timeTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        posterImageView = findViewById(R.id.posterImageView);

        eventId = getIntent().getStringExtra("eventId");

        if (eventId != null) {
            loadEventDetails(eventId);
        } else {
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Loads the event details from Firestore based on the provided event ID.
     *
     * <p>This method fetches the event's name, date, time, description, and poster URL from
     * Firestore and displays them in the UI. If the poster URL is available, it is loaded
     * into the ImageView using Glide.</p>
     *
     * @param eventId the ID of the event to load
     */
    private void loadEventDetails(String eventId) {
        DocumentReference eventRef = db.collection("Events").document(eventId);
        eventRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String eventName = documentSnapshot.getString("eventName");
                        String date = documentSnapshot.getString("drawDate");
                        String time = documentSnapshot.getString("eventDateTime");
                        String description = documentSnapshot.getString("description");
                        String posterUrl = documentSnapshot.getString("posterUrl");

                        eventNameTextView.setText(eventName);
                        dateTextView.setText("Date: " + date);
                        timeTextView.setText("Time: " + time);
                        descriptionTextView.setText(description);

                        if (posterUrl != null && !posterUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(posterUrl)
                                    .into(posterImageView);
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading event details", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
