package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main activity that serves as the entry point for the application, providing navigation
 * between different fragments and activities.
 *
 * <p>The MainActivity includes a BottomNavigationView for switching between fragments like
 * HomeFragment and QRScannerFragment. It also starts the OrganizerProfileActivity directly
 * when the profile menu item is selected. This activity initializes the notification channel
 * and loads the HomeFragment by default.</p>
 */
public class MainActivity extends AppCompatActivity {

    private EditText eventNameEditText, dateEditText, timeEditText, descriptionEditText, maxAttendeesEditText, maxWaitlistEditText;
    private CheckBox geolocationCheckBox;
    private Button saveButton, generateQRButton;

    private BottomNavigationView bottomNavigationView;

    /**
     * Initializes the activity and sets up the navigation between fragments and activities.
     *
     * <p>This method sets the main layout, creates the notification channel, loads the HomeFragment by
     * default, and sets up listeners for the bottom navigation menu.</p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the most recent data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        NotificationUtils.createNotificationChannel(this); // Creating channel for notifications

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Load HomeFragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_camera:
                    selectedFragment = new QRScannerFragment(); // Navigate to QRScannerFragment
                    break;
                case R.id.nav_profile:
                    // Start OrgProfileActivity instead of navigating to a Fragment
                    Intent intent = new Intent(this, OrganizerProfileActivity.class);
                    startActivity(intent);
                    return true;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack(null)
                        .commit();
            }
            return true;
        });
    }
}
