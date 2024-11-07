package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.myapplication.Event;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OrgEventRepo extends ArrayAdapter<Event> {
    private Context context;
    private ArrayList<Event> eventDataList;

    public OrgEventRepo(Context context, ArrayList<Event> eventDataList) {
        super(context, 0, eventDataList); // Pass data list to the ArrayAdapter
        this.context = context;
        this.eventDataList = eventDataList;
    }

    public View getView (int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
        }

        // Get the event object from the list
        Event event = eventDataList.get(position);

        // Find the TextViews in the item layout
        TextView eventNameTextView = convertView.findViewById(R.id.event_name_text);
        TextView dateTextView = convertView.findViewById(R.id.date_text);

        // Set the text of the TextViews using the getters from the Event object
        eventNameTextView.setText(event.getEventName());
        dateTextView.setText(event.getDate());

        return convertView;
    }

    // Update the list when new data is available
    public void updateEventDataList(ArrayList<Event> eventDataList) {
        this.eventDataList.clear();
        this.eventDataList.addAll(eventDataList);
        notifyDataSetChanged();
    }
}
