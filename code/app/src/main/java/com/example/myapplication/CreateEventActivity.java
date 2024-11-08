package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity that allows users to create a new event with details such as name, date, time, description,
 * maximum attendees, and optional poster image. Users can also generate a QR code for the event link.
 *
 * <p>This activity handles user input for event creation, saves the event details to Firebase Firestore,
 * uploads a poster image to Firebase Storage if provided, and generates a QR code that links to the event.
 * QR code generation uses the event ID for referencing the event in the app.</p>
 */
public class CreateEventActivity extends AppCompatActivity {
    private EditText eventNameEditText, dateEditText, timeEditText, descriptionEditText, maxAttendeesEditText, maxWaitlistEditText;
    private CheckBox geolocationCheckBox;
    private ImageView qrCodeImageView;
    private Button uploadPosterButton, saveButton, generateQRButton;
    private Uri posterUri;
    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> posterPickerLauncher;
    private String qrCodeLink; // Class-level variable to hold the QR code link

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event); // Ensure this is the correct layout file

        db = FirebaseFirestore.getInstance();

        // Initialize views
        eventNameEditText = findViewById(R.id.eventNameEditText);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        maxAttendeesEditText = findViewById(R.id.maxAttendeesEditText);
        maxWaitlistEditText = findViewById(R.id.maxWaitlistEditText);
        geolocationCheckBox = findViewById(R.id.geolocationCheckBox);
        saveButton = findViewById(R.id.saveButton);
        generateQRButton = findViewById(R.id.generateQRButton);
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        uploadPosterButton = findViewById(R.id.uploadPosterButton);

        dateEditText.setOnClickListener(v -> showDatePicker());
        timeEditText.setOnClickListener(v -> showTimePicker());

        // Initialize the poster picker launcher
        posterPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        posterUri = result.getData().getData();
                        Toast.makeText(this, "Poster selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        uploadPosterButton.setOnClickListener(v -> openPosterPicker());

        // Set up listeners
        saveButton.setOnClickListener(view -> saveEvent());

        generateQRButton.setOnClickListener(view -> {
            if (qrCodeLink != null && !qrCodeLink.isEmpty()) {
                generateQRCode(qrCodeLink);
            } else {
                Toast.makeText(this, "Please save the event first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Opens the file picker to allow the user to select an image as the event poster.
     *
     * <p>The selected image URI is stored in the {@code posterUri} field for later upload
     * to Firebase Storage.</p>
     */
    private void openPosterPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        posterPickerLauncher.launch(intent);
    }

    /**
     * Displays a date picker dialog to let the user select the event date.
     *
     * <p>Upon selection, the chosen date is formatted and set in the {@code dateEditText} field.</p>
     */
    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    String formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                    dateEditText.setText(formattedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    /**
     * Displays a time picker dialog to let the user select the event time.
     *
     * <p>Upon selection, the chosen time is formatted and set in the {@code timeEditText} field.</p>
     */
    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minuteOfHour) -> {
                    String formattedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                    timeEditText.setText(formattedTime);
                },
                hour, minute, true);
        timePickerDialog.show();
    }

    /**
     * Generates a QR code for the event and displays it in the ImageView.
     *
     * @param qrCodeLink the event link to encode in the QR code
     *
     * <p>The generated QR code contains a link that allows users to access the event
     * directly from a scan.</p>
     */
    private void generateQRCode(String qrCodeLink) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrCodeLink, BarcodeFormat.QR_CODE, 300, 300);
            qrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves the event details to Firestore, including an optional poster upload.
     *
     * <p>The event details, including name, date, time, description, max attendees,
     * and QR code link, are saved to Firestore. If a poster is selected, it is uploaded
     * to Firebase Storage. Upon successful save, the QR code is generated and displayed.</p>
     */
    private void saveEvent() {
        String eventName = eventNameEditText.getText().toString();
        String date = dateEditText.getText().toString();
        String time = timeEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String maxAttendeesStr = maxAttendeesEditText.getText().toString();
        String maxWaitlistStr = maxWaitlistEditText.getText().toString();

        if (eventName.isEmpty() || date.isEmpty() || time.isEmpty() || description.isEmpty() || maxAttendeesStr.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int maxAttendees = Integer.parseInt(maxAttendeesStr);
        Integer maxWaitlist = !maxWaitlistStr.isEmpty() ? Integer.parseInt(maxWaitlistStr) : null;
        boolean geolocationEnabled = geolocationCheckBox.isChecked();

        CollectionReference eventsRef = db.collection("Events");
        String eventId = eventsRef.document().getId(); // Generate event ID

        // Generate QR code link using eventId
        qrCodeLink = "eventapp://event/" + eventId;

        Map<String, Object> event = new HashMap<>();
        event.put("eventName", eventName);
        event.put("description", description);
        event.put("drawDate", date);
        event.put("eventDateTime", time);
        event.put("maxAttendees", maxAttendees);
        event.put("maxOnWaitList", maxWaitlist);
        event.put("geolocationEnabled", geolocationEnabled);
        event.put("qrCodeLink", qrCodeLink); // Save the QR code link
        event.put("eventId", eventId); // Save the eventId for future reference

        eventsRef.document(eventId).set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Event saved successfully.");
                    if (posterUri != null) {
                        uploadPoster(eventId);
                    }
                    Toast.makeText(this, "Event saved successfully", Toast.LENGTH_SHORT).show();
                    // Optionally, generate the QR code immediately after saving
                    generateQRCode(qrCodeLink);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving event", e);
                    Toast.makeText(this, "Error saving event", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Uploads the selected poster image to Firebase Storage and updates the event record
     * in Firestore with the poster URL.
     *
     * @param eventId the ID of the event to which the poster is associated
     *
     * <p>This method uploads the poster to Firebase Storage, then updates the Firestore
     * event record with the download URL of the poster image.</p>
     */
    private void uploadPoster(String eventId) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("posters/" + eventId + ".jpg");
        storageRef.putFile(posterUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    db.collection("Events").document(eventId).update("posterUrl", uri.toString())
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Poster URL updated successfully"))
                            .addOnFailureListener(e -> Log.e("Firestore", "Error updating poster URL", e));
                }))
                .addOnFailureListener(e -> Log.e("Storage", "Error uploading poster", e));
    }
}
