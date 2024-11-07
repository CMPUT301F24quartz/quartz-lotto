package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateEventActivity extends AppCompatActivity {
    private EditText eventNameEditText, dateEditText, timeEditText, descriptionEditText, maxAttendeesEditText, maxWaitlistEditText;
    private CheckBox geolocationCheckBox;
    private Button saveButton;
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");

        // Initialize UI elements
        eventNameEditText = findViewById(R.id.eventNameEditText);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        maxAttendeesEditText = findViewById(R.id.maxAttendeesEditText);
        maxWaitlistEditText = findViewById(R.id.maxWaitlistEditText);
        geolocationCheckBox = findViewById(R.id.geolocationCheckBox);
        saveButton = findViewById(R.id.saveButton);

        // Save button action
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve input values
                String eventName = eventNameEditText.getText().toString().trim();
                String date = dateEditText.getText().toString().trim();
                String time = timeEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();
                String maxAttendees = maxAttendeesEditText.getText().toString().trim();
                String maxWaitlist = maxWaitlistEditText.getText().toString().trim();
                boolean geolocationEnabled = geolocationCheckBox.isChecked();

                // Validate inputs (optional, but recommended)
                if (eventName.isEmpty() || maxAttendees.isEmpty()) {
                    Toast.makeText(CreateEventActivity.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert maxWaitlist to integer, defaulting to 0 if it's empty
                int maxWaitlistInt = maxWaitlist.isEmpty() ? 0 : Integer.parseInt(maxWaitlist);

                // Create event object
                Event newEvent = new Event(eventName, date, time, description, Integer.parseInt(maxAttendees),
                        maxWaitlistInt, geolocationEnabled);

                // Add event to Firestore
                eventsRef.add(newEvent)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(CreateEventActivity.this, "Event saved successfully", Toast.LENGTH_SHORT).show();

                            // Clear input fields after saving
                            eventNameEditText.setText("");
                            dateEditText.setText("");
                            timeEditText.setText("");
                            descriptionEditText.setText("");
                            maxAttendeesEditText.setText("");
                            maxWaitlistEditText.setText("");  // Keep maxWaitlist field empty
                            geolocationCheckBox.setChecked(false);
                        })
                        .addOnFailureListener(e -> Toast.makeText(CreateEventActivity.this, "Error saving event", Toast.LENGTH_SHORT).show());

//                // Passing event data back to HomePageActivity (optional)
//                Intent intent = new Intent();
//                intent.putExtra("eventName", eventName);
//                setResult(RESULT_OK, intent);
//                finish();
            }
        });
    }
}
