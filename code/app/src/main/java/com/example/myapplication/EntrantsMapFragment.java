// EntrantsMapFragment.java
package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.firestore.*;

import java.util.List;
import java.util.Map;

/**
 * Fragment that displays a map with markers for each entrant's location for a specific event.
 *
 * <p>This fragment utilizes Google Maps to show the locations of event entrants retrieved
 * from Firestore. Entrants are represented with markers on the map, and the map camera
 * optionally focuses on the first entrant's location. The map's lifecycle methods are
 * properly handled to integrate with the Fragment lifecycle.</p>
 */
public class EntrantsMapFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String TAG = "EntrantsMapFragment";

    /**
     * Inflates the fragment layout containing the map view and initializes the map.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container The parent view that this fragment's UI should be attached to, if any.
     * @param savedInstanceState The fragment's previously saved state, if any.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrants_map, container, false);

        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return view;
    }

    /**
     * Called when the map is ready to be used. Sets up initial map settings and fetches entrants' locations.
     *
     * @param map The GoogleMap object that is ready to be used.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Optional: Customize the map
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Fetch entrants' locations from Firestore
        fetchEntrantsLocations();
    }

    /**
     * Retrieves the locations of entrants from Firestore and adds markers to the map for each location.
     *
     * <p>Fetches the locations of entrants for a specified event ID and places a marker at each entrant's
     * location on the map. The camera is moved to the first entrant's location if available.</p>
     */
    private void fetchEntrantsLocations() {
        String eventId = "EVENT12345"; // Replace with dynamic event ID if applicable

        db.collection("entrants")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot doc : documents) {
                        Map<String, Object> location = (Map<String, Object>) doc.get("location");
                        if (location != null) {
                            double latitude = (double) location.get("latitude");
                            double longitude = (double) location.get("longitude");

                            LatLng entrantLatLng = new LatLng(latitude, longitude);
                            googleMap.addMarker(new MarkerOptions()
                                    .position(entrantLatLng)
                                    .title("Entrant ID: " + doc.getString("entrantId")));
                        }
                    }

                    // Optionally move the camera to the first entrant's location
                    if (!documents.isEmpty()) {
                        Map<String, Object> firstLocation = (Map<String, Object>) documents.get(0).get("location");
                        if (firstLocation != null) {
                            double lat = (double) firstLocation.get("latitude");
                            double lon = (double) firstLocation.get("longitude");
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 10));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching entrants' locations", e);
                });
    }

    // MapView lifecycle methods

    /**
     * Called when the Fragment is visible to the user and actively running.
     * Resumes the MapView to keep the map visible and interactive.
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * Called when the Fragment becomes visible to the user.
     * Starts the MapView lifecycle, making the map ready to display.
     */
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    /**
     * Called when the Fragment is no longer started.
     * Stops the MapView lifecycle to preserve resources.
     */
    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    /**
     * Called when the Fragment is paused.
     * Pauses the MapView lifecycle to pause map activity.
     */
    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    /**
     * Called to release memory when the system is running low on memory.
     * Passes the low-memory state to the MapView.
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     * Called when the Fragment is destroyed.
     * Cleans up the MapView resources.
     */
    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    /**
     * Called to save the fragment state before a configuration change.
     * Saves the MapView's state to retain map configuration.
     *
     * @param outState Bundle in which to save the state of the Fragment.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
