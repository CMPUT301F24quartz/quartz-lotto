package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that allows users to manage their facilities, including viewing, adding, and deleting facilities.
 *
 * <p>This activity uses Firebase Firestore to store and retrieve facility data, and Firebase Storage
 * for managing facility images. It also provides anonymous sign-in functionality and loads the user's
 * facilities on sign-in. Facilities can be added through an Add Facility screen, and they can be deleted
 * after a confirmation prompt.</p>
/*

public class ManageFacilitiesActivity extends AppCompatActivity {

    private static final String TAG = "ManageFacilitiesActivity";
    private static final String FACILITY_COLLECTION = "Facilities";

    private RecyclerView facilitiesRecyclerView;
    private FacilitiesAdapter facilitiesAdapter;
    private List<Facility> facilitiesList = new ArrayList<>();
    private Button addFacilityButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private String userId;

    /**
     * Initializes the activity, sets up Firebase Firestore and authentication, and prepares the facilities list.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the most recent data.
     */

    public void setAuth(FirebaseAuth auth) {
        this.auth = auth;
    }

    public void setFirestore(FirebaseFirestore db) {
        this.db = db;
    }

    public void setStorage(FirebaseStorage storage) {
        this.storage = storage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_facilities);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        if (storage == null) {
            storage = FirebaseStorage.getInstance();
        }
        
        performAnonymousSignIn();

        facilitiesRecyclerView = findViewById(R.id.facilities_recycler_view);
        addFacilityButton = findViewById(R.id.add_facility_button);

        facilitiesAdapter = new FacilitiesAdapter(this, facilitiesList);
        facilitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        facilitiesRecyclerView.setAdapter(facilitiesAdapter);

        addFacilityButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManageFacilitiesActivity.this, AddFacilityActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Performs anonymous sign-in if the user is not already signed in.
     *
     * <p>If the user is already signed in, their ID is retrieved, and facilities are loaded for that user.
     * If anonymous sign-in is required and successful, the facilities are loaded after sign-in.
     * If sign-in fails, a message is displayed, and the activity finishes.</p>
     */
    private void performAnonymousSignIn() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            auth.signInAnonymously()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                userId = user.getUid();
                                Log.d(TAG, "Anonymous sign-in successful. User ID: " + userId);
                                loadFacilities();
                            }
                        } else {
                            Log.w(TAG, "Anonymous sign-in failed.", task.getException());
                            Toast.makeText(ManageFacilitiesActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            userId = currentUser.getUid();
            Log.d(TAG, "User already signed in. User ID: " + userId);
            loadFacilities();
        }
    }

    /**
     * Reloads the facilities list when the activity resumes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (userId != null) {
            loadFacilities();
        }
    }

    /**
     * Loads the list of facilities for the signed-in user from Firestore.
     *
     * <p>This method clears the current list, queries Firestore for facilities matching the
     * user's ID, and updates the adapter with the retrieved data.</p>
     */

    void loadFacilities() {
        if (userId == null) {
            Log.e(TAG, "User ID is null. Cannot load facilities.");
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference facilitiesRef = db.collection(FACILITY_COLLECTION);
        facilitiesRef.whereEqualTo("organizerId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        facilitiesList.clear();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Facility facility = document.toObject(Facility.class);
                                facility.setId(document.getId());
                                facilitiesList.add(facility);
                            }
                            facilitiesAdapter.notifyDataSetChanged();
                            Log.d(TAG, "Loaded " + facilitiesList.size() + " facilities.");
                        }
                    } else {
                        Toast.makeText(ManageFacilitiesActivity.this, "Error getting facilities.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error getting facilities: ", task.getException());
                    }
                });
    }

    /**
     * Deletes a facility from Firestore and optionally its associated image from Firebase Storage.
     *
     * <p>Once the facility is deleted, it is also removed from the local facilities list and
     * the RecyclerView adapter is updated. In case of a failure, an error message is displayed.</p>
     *
     * @param facility the Facility object to delete
     */
    public void deleteFacility(Facility facility) {
        if (facility == null) {
            Toast.makeText(this, "Error: Facility not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String facilityId = facility.getId();
        String imageUrl = (facility.getImageUrls() != null && !facility.getImageUrls().isEmpty()) ? facility.getImageUrls().get(0) : null;

        db.collection(FACILITY_COLLECTION).document(facilityId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Facility deleted successfully", Toast.LENGTH_SHORT).show();

                    if (imageUrl != null) {
                        deleteImageFromStorage(imageUrl);
                    }

                    facilitiesList.remove(facility);
                    facilitiesAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting facility", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error deleting facility: ", e);
                });
    }

    /**
     * Deletes an image from Firebase Storage based on its URL.
     *
     * @param imageUrl the URL of the image to delete from Storage
     */
    private void deleteImageFromStorage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Log.w(TAG, "Image URL is null or empty. Skipping deletion from Storage.");
            return;
        }

        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        imageRef.delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Image deleted from Storage successfully."))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting image from Storage", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error deleting image from Storage: ", e);
                });
    }

    /**
     * Displays a confirmation dialog before deleting a facility.
     *
     * @param facility the Facility object to confirm deletion for
     */
    public void confirmDeleteFacility(Facility facility) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Facility")
                .setMessage("Are you sure you want to delete this facility and its image?")
                .setPositiveButton("Yes", (dialog, which) -> deleteFacility(facility))
                .setNegativeButton("No", null)
                .show();
    }
}
