package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Activity that allows users to add or edit facilities in the application.
 *
 * <p>This activity provides a screen where users can add information about a facility,
 * such as the name, location, and an image. The facility details are stored in Firebase
 * Firestore and Firebase Storage. When the user selects an image, it is uploaded to
 * Firebase Storage, and the facility details are saved to Firestore. If the user is editing
 * an existing facility, the facility's current details are loaded from Firestore.</p>
 *
 * <p>Outstanding Issues:</p>
 * <ul>
 *   <li>Ensure the user is authenticated before allowing facility details to be saved.</li>
 *   <li>Handle any additional user permissions for accessing images from external storage.</li>
 * </ul>
 */
public class AddFacilityActivity extends AppCompatActivity {

    private static final String TAG = "AddFacilityActivity";
    private static final String FACILITY_COLLECTION = "Facilities";

    private ImageView facilityImageView;
    private EditText facilityNameField, facilityLocationField;
    private Uri facilityImageUri;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private String userId;
    private String facilityId; // If editing an existing facility

    /**
     * Launcher to handle image selection from external storage.
     */
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    facilityImageUri = result.getData().getData();
                    Glide.with(this)
                            .load(facilityImageUri)
                            .apply(RequestOptions.centerCropTransform())
                            .into(facilityImageView);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_facility);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        facilityImageView = findViewById(R.id.facilityImageView);
        facilityNameField = findViewById(R.id.facility_name);
        facilityLocationField = findViewById(R.id.facility_location);
        Button uploadFacilityImageButton = findViewById(R.id.uploadFacilityImageButton);
        Button saveFacilityButton = findViewById(R.id.saveFacilityButton);

        Intent intent = getIntent();
        if (intent.hasExtra("facilityId")) {
            facilityId = intent.getStringExtra("facilityId");
            loadFacilityDetails(facilityId);
        }

        uploadFacilityImageButton.setOnClickListener(v -> openImagePicker());
        saveFacilityButton.setOnClickListener(v -> saveFacilityDetails());
    }

    /**
     * Opens the image picker to allow the user to select an image for the facility.
     *
     * <p>Launches an intent to open a content selection for images. The selected image URI
     * is then loaded into the facilityImageView using Glide for display.</p>
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
        Toast.makeText(this, "Select an image for the facility", Toast.LENGTH_SHORT).show();
    }

    /**
     * Loads the details of an existing facility from Firestore.
     *
     * @param facilityId the ID of the facility to load
     *
     * <p>Fetches the facility details from Firestore using the given facility ID. If the
     * facility exists, its details (name, location, and image) are displayed in the UI. In
     * case of a failure, an error message is shown to the user.</p>
     */
    private void loadFacilityDetails(String facilityId) {
        db.collection(FACILITY_COLLECTION).document(facilityId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Facility facility = documentSnapshot.toObject(Facility.class);
                        if (facility != null) {
                            facilityNameField.setText(facility.getName());
                            facilityLocationField.setText(facility.getLocation());
                            if (facility.getImageUrls() != null && !facility.getImageUrls().isEmpty()) {
                                Glide.with(this)
                                        .load(facility.getImageUrls().get(0))
                                        .apply(RequestOptions.centerCropTransform())
                                        .placeholder(R.drawable.ic_placeholder_image)
                                        .into(facilityImageView);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load facility details", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading facility details: ", e);
                });
    }

    /**
     * Saves the facility details entered by the user, including optional image upload.
     *
     * <p>If an image URI is present, it is uploaded to Firebase Storage, and the download URL
     * is added to the facility data before saving it to Firestore. If the facilityId is not
     * null, an existing facility is updated; otherwise, a new facility is created in Firestore.</p>
     */
    private void saveFacilityDetails() {
        String name = facilityNameField.getText().toString().trim();
        String location = facilityLocationField.getText().toString().trim();

        if (name.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please enter all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (facilityImageUri != null) {
            String imageName = UUID.randomUUID().toString() + ".jpg";
            StorageReference storageRef = storage.getReference("facility_images/" + userId + "/" + imageName);
            storageRef.putFile(facilityImageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                List<String> imageUrls = new ArrayList<>();
                                imageUrls.add(uri.toString());
                                saveFacilityToFirestore(name, location, imageUrls);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to retrieve image URL", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error retrieving image URL: ", e);
                            }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error uploading image: ", e);
                    });
        } else {
            saveFacilityToFirestore(name, location, null);
        }
    }

    /**
     * Stores the facility data in Firestore, either creating a new facility or updating an
     * existing one.
     *
     * @param name the name of the facility
     * @param location the location of the facility
     * @param imageUrls a list of image URLs associated with the facility (can be null)
     *
     * <p>The method organizes the facility details into a map and attempts to store it
     * in Firestore. If a facility ID is provided, the existing facility document is updated.
     * Otherwise, a new document is created in the "Facilities" collection.</p>
     */
    private void saveFacilityToFirestore(String name, String location, List<String> imageUrls) {
        Map<String, Object> facility = new HashMap<>();
        facility.put("name", name);
        facility.put("location", location);
        facility.put("organizerId", userId);
        facility.put("imageUrls", imageUrls != null ? imageUrls : new ArrayList<String>()); // ensure imageUrls is never null

        if (facilityId != null) {
            db.collection(FACILITY_COLLECTION).document(facilityId)
                    .set(facility, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Facility updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error updating facility", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating facility: ", e);
                    });
        } else {
            db.collection(FACILITY_COLLECTION).add(facility)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Facility added successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error adding facility", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error adding facility: ", e);
                    });
        }
    }
}
