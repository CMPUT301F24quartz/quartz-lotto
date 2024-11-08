package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Attendee;
import com.example.myapplication.AttendingActivity;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of attendees in the AttendingActivity.
 *
 * <p>This adapter is responsible for creating and binding the views for each attendee item
 * in the RecyclerView. It allows conditional display of buttons and status fields based on
 * whether the attendee is on the waiting list or has been chosen.</p>
 *
 * <p>Parameters:</p>
 * <ul>
 *     <li>{@code attendeeList} - List of attendees to be displayed in the RecyclerView.</li>
 *     <li>{@code isWaiting} - Boolean indicating if the attendee is on a waiting list, which controls the visibility of the cancel button.</li>
 *     <li>{@code notChosen} - Boolean indicating if the attendee's status is not chosen, which controls the visibility of the status field.</li>
 * </ul>
 */
public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.ViewHolder> {

    private List<Attendee> attendeeList;
    private boolean isWaiting;
    private boolean notChosen;

    /**
     * Constructor for AttendeeAdapter.
     *
     * @param attendeeList List of attendees to be displayed.
     * @param isWaiting Boolean indicating if attendees are on the waiting list, to show the cancel button.
     * @param notChosen Boolean indicating if attendees have not been chosen, to hide the status field.
     */
    public AttendeeAdapter(List<Attendee> attendeeList, boolean isWaiting, boolean notChosen) {
        this.attendeeList = attendeeList != null ? attendeeList : new ArrayList<>();
        this.isWaiting = isWaiting;
        this.notChosen = notChosen;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendee, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the data for a specific attendee to the ViewHolder.
     *
     * <p>Sets the attendee name and status in the holder's views. If the attendee is on the
     * waiting list, a cancel button is shown and can trigger a move to cancelled state in the
     * AttendingActivity. If the attendee is not chosen, the status field is hidden.</p>
     *
     * @param holder ViewHolder to bind data to.
     * @param position Position of the attendee in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attendee attendee = attendeeList.get(position);
        holder.name.setText(attendee.getName());
        holder.status.setText(attendee.getStatus());

        if (isWaiting) {
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setOnClickListener(v -> {
                if (holder.itemView.getContext() instanceof AttendingActivity) {
                    ((AttendingActivity) holder.itemView.getContext()).moveToCancelled(position);
                    notifyDataSetChanged();
                }
            });
        } else {
            holder.cancelButton.setVisibility(View.GONE);
        }

        if (notChosen) {
            holder.status.setVisibility(View.GONE);
        } else {
            holder.status.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return attendeeList.size();
    }

    /**
     * ViewHolder class for holding views of each attendee item.
     *
     * <p>This inner class holds references to the attendee's name, status, and cancel button
     * for each item in the RecyclerView.</p>
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, status;
        Button cancelButton;

        /**
         * Constructs a ViewHolder and initializes its views.
         *
         * @param itemView The view of the attendee item in the RecyclerView.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.attendeeName);
            status = itemView.findViewById(R.id.attendeeStatus);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }
}
