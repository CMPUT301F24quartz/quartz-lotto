package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter for displaying a list of facilities in a RecyclerView.
 *
 * <p>The FacilitiesAdapter binds facility data, including name, location, and an optional image, to a list item view.
 * Each item includes options to edit or delete the facility, with actions handled via click listeners.
 * This adapter also interfaces with the ManageFacilitiesActivity to confirm deletions.</p>
 */
public class FacilitiesAdapter extends RecyclerView.Adapter<FacilitiesAdapter.FacilityViewHolder> {

    private final Context context;
    private final List<Facility> facilities;

    /**
     * Constructs a new FacilitiesAdapter.
     *
     * @param context the context in which the adapter is used
     * @param facilities a list of Facility objects to display in the RecyclerView
     */
    public FacilitiesAdapter(Context context, List<Facility> facilities) {
        this.context = context;
        this.facilities = facilities;
    }

    /**
     * Creates a new ViewHolder that represents a single list item for a facility.
     *
     * @param parent the parent ViewGroup into which the new view will be added
     * @param viewType the view type of the new View
     * @return a new FacilityViewHolder that holds the view for a single facility item
     */
    @NonNull
    @Override
    public FacilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_facility, parent, false);
        return new FacilityViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     *
     * <p>This method sets the facility name and location in the list item. If an image URL is
     * available, it loads the image using Glide; otherwise, a placeholder is displayed.
     * Also, it sets up click listeners for editing and deleting the facility.</p>
     *
     * @param holder the ViewHolder which should be updated to represent the contents of the item
     *               at the given position in the data set
     * @param position the position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull FacilityViewHolder holder, int position) {
        Facility facility = facilities.get(position);
        holder.nameTextView.setText(facility.getName());
        holder.locationTextView.setText(facility.getLocation());

        if (facility.getImageUrls() != null && !facility.getImageUrls().isEmpty()) {
            String imageUrl = facility.getImageUrls().get(0);
            if (imageUrl != null) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_error_image)
                        .into(holder.facilityImageView);
            } else {
                holder.facilityImageView.setImageResource(R.drawable.ic_placeholder_image);
            }
        } else {
            holder.facilityImageView.setImageResource(R.drawable.ic_placeholder_image);
        }

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddFacilityActivity.class);
            intent.putExtra("facilityId", facility.getId());
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (context instanceof ManageFacilitiesActivity) {
                ((ManageFacilitiesActivity) context).confirmDeleteFacility(facility);
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return the size of the facilities list
     */
    @Override
    public int getItemCount() {
        return facilities.size();
    }

    /**
     * ViewHolder class that represents each facility item in the RecyclerView.
     */
    public static class FacilityViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, locationTextView;
        ImageView facilityImageView;
        ImageButton editButton, deleteButton;

        /**
         * Constructs a FacilityViewHolder and initializes its views.
         *
         * @param itemView the view of the individual item in the RecyclerView
         */
        public FacilityViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.facility_name);
            locationTextView = itemView.findViewById(R.id.facility_location);
            facilityImageView = itemView.findViewById(R.id.facilityImageView);
            editButton = itemView.findViewById(R.id.edit_facility_button);
            deleteButton = itemView.findViewById(R.id.delete_facility_button);
        }
    }
}
