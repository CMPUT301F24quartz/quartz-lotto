package com.example.myapplication.Views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import com.example.myapplication.AttendeeAdapter;
import com.example.myapplication.AttendeesAdapter;
import com.example.myapplication.Controllers.EntrantListController;
import com.example.myapplication.Models.Attendee;
import com.example.myapplication.R;
import com.example.myapplication.Repositories.EntrantListRepository;

import java.util.ArrayList;

/**
 * Fragment displaying the waitlist for an event.
 */
public class WaitingListView extends Fragment {
    private static final String ARG_EVENT_ID = "eventId";
    private String eventId;
    private RecyclerView waitingView;
    private AttendeesAdapter adapter;
    private EntrantListController controller;
    private EntrantListRepository.FetchEntrantListCallback callback;

    public WaitingListView() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param eventId The unique ID of the event.
     * @return A new instance of fragment WaitlistFragment.
     */
    public static WaitingListView newInstance(String eventId) {
        WaitingListView fragment = new WaitingListView();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve eventId from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        } else {
            Toast.makeText(getContext(), "Event ID missing.", Toast.LENGTH_SHORT).show();
            // Optionally, navigate back or show an error
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waitlist, container, false);
        waitingView = view.findViewById(R.id.recyclerViewAttendees);
        waitingView.setLayoutManager(new LinearLayoutManager(getContext()));
        controller = new EntrantListController();

        controller.fetchEntrantList(eventId, "waiting", new EntrantListRepository.FetchEntrantListCallback() {
            @Override
            public void onFetchEntrantListSuccess(ArrayList<Attendee> entrantList) {

                // Handle the fetched entrant list (e.g., update UI or pass to adapter)
                adapter = new AttendeesAdapter(entrantList, getContext(), "organizer", ARG_EVENT_ID);
                waitingView.setAdapter(adapter);
                Log.d("EntrantListController", "Fetched Entrant List: " + entrantList);
            }

            @Override
            public void onFetchEntrantListFailure(Exception e) {
                // Handle the error
                Log.e("EntrantListController", "Failed to fetch entrant list", e);
            }
        });


        return view;
    }
}
