package com.example.myapplication.Views;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.EventAdapter;
import com.example.myapplication.Models.Event;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class HomeView extends AppCompatActivity {
    // Aditi
    private ListView selectedEventsListView;
    private ListView waitlistEventsListView;

    private HomePageController selectedEventsAdapter;
    private HomePageController waitlistEventsAdapter;

    private List<Event> selectedEvents;
    private List<Event> waitlistEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for this activity
        setContentView(R.layout.activity_home_page);


        selectedEventsListView = findViewById(R.id.selected_events_list);
        waitlistEventsListView = findViewById(R.id.entrant_waitlist);

        selectedEvents = new ArrayList<>();


        //SILLY SAMPLE DATA
        selectedEvents.add(new EntrantSelectedEvent(1, "svt night 1", "2024-11-09"));
        selectedEvents.add(new EntrantSelectedEvent(2, "svt night 2", "2024-11-10"));

        selectedEventsAdapter = new EventAdapter(this, selectedEvents, true);


        selectedEventsListView.setAdapter(selectedEventsAdapter);

    }

    // Confirm an event from the waitlist to the selected list
    public void confirmEvent(EntrantSelectedEvent event) {
        selectedEvents.add(event);
        selectedEventsAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Event Confirmed", Toast.LENGTH_SHORT).show();
    }

    // Delete an event from either list
    public void deleteEvent(EntrantSelectedEvent event, boolean isSelectedList) {
        if (isSelectedList) {
            selectedEvents.remove(event);
            selectedEventsAdapter.notifyDataSetChanged();
        }

        Toast.makeText(this, "Event Deleted", Toast.LENGTH_SHORT).show();
    }
}