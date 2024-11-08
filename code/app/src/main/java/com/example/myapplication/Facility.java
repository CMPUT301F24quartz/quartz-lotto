package com.example.myapplication;

import java.util.List;

/**
 * Represents a facility with details such as name, location, image URLs, and an organizer ID.
 *
 * <p>The Facility class provides properties to store information about a facility,
 * including its ID, name, location, associated images, and the ID of the organizer.
 * It includes getter and setter methods for accessing and modifying these properties.</p>
 */
public class Facility {
    private String id;
    private String name;
    private String location;
    private List<String> imageUrls;
    private String organizerId;

    /**
     * Default constructor required for Firestore and other data serialization.
     */
    public Facility() {}

    /**
     * Constructs a Facility with specified details.
     *
     * @param name the name of the facility
     * @param location the location of the facility
     * @param imageUrls a list of URLs for images associated with the facility
     * @param organizerId the ID of the organizer who created or manages the facility
     */
    public Facility(String name, String location, List<String> imageUrls, String organizerId) {
        this.name = name;
        this.location = location;
        this.imageUrls = imageUrls;
        this.organizerId = organizerId;
    }

    /**
     * Returns the unique ID of the facility.
     *
     * @return the facility ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the facility.
     *
     * @param id the facility ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the facility.
     *
     * @return the facility name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the facility.
     *
     * @param name the facility name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the location of the facility.
     *
     * @return the facility location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the facility.
     *
     * @param location the facility location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns a list of image URLs associated with the facility.
     *
     * @return a list of image URLs
     */
    public List<String> getImageUrls() {
        return imageUrls;
    }

    /**
     * Sets the list of image URLs associated with the facility.
     *
     * @param imageUrls a list of image URLs
     */
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    /**
     * Returns the ID of the organizer who created or manages the facility.
     *
     * @return the organizer ID
     */
    public String getOrganizerId() {
        return organizerId;
    }

    /**
     * Sets the ID of the organizer who created or manages the facility.
     *
     * @param organizerId the organizer ID
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }
}
