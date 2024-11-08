package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for displaying a list of events in a RecyclerView.
 *
 * <p>The EventAdapter displays event names in a simple list item format. When an event
 * is clicked, it opens the {@link WaitinglistActivity} with the selected event's ID passed as an extra.
 * </p>
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private final List<String> eventNames;
    private final List<String> eventIds;
    private final Context context;

    /**
     * Constructs a new EventAdapter.
     *
     * @param context the context in which the adapter is used
     * @param eventNames a list of event names to display in the RecyclerView
     * @param eventIds a list of event IDs corresponding to each event name
     */
    public EventAdapter(Context context, List<String> eventNames, List<String> eventIds) {
        this.context = context;
        this.eventNames = eventNames;
        this.eventIds = eventIds;
    }

    /**
     * Creates a new ViewHolder that represents a single list item for an event.
     *
     * @param parent the parent ViewGroup into which the new view will be added
     * @param viewType the view type of the new View
     * @return a new EventViewHolder that holds the view for a single event item
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     *
     * <p>Sets the event name text for the list item and attaches an OnClickListener
     * to handle navigation to the {@link WaitinglistActivity} with the selected event's ID.</p>
     *
     * @param holder the ViewHolder which should be updated to represent the contents of the item
     *               at the given position in the data set
     * @param position the position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.eventNameTextView.setText(eventNames.get(position));

        holder.itemView.setOnClickListener(v -> {
            String selectedEventId = eventIds.get(position);
            Intent intent = new Intent(context, WaitinglistActivity.class);
            intent.putExtra("event_id", selectedEventId);
            context.startActivity(intent);
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return the size of the event names list
     */
    @Override
    public int getItemCount() {
        return eventNames.size();
    }

    /**
     * ViewHolder class that represents each event item in the RecyclerView.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView;

        /**
         * Constructs an EventViewHolder.
         *
         * @param itemView the view of the individual item in the RecyclerView
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}
