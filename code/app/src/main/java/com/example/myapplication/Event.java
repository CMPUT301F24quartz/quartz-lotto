package com.example.myapplication;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.Context;

/**
 * Represents an event with details such as name, date, time, description, and attendance limits.
 *
 * <p>The Event class provides properties and methods to manage the event details, including
 * the maximum number of attendees, a waiting list, QR code link, and notification system.
 * Methods allow adding/removing users from the waiting list, sampling attendees, and notifying selected users.</p>
 */
public class Event {
    private String eventName;
    private String date;
    private String time;
    private String description;
    private int maxAttendees;
    private Integer maxWaitlist; // Nullable
    private boolean geolocationEnabled;
    private String qrCodeLink;
    private WaitingList waitingList;

    /**
     * Constructs a new Event with specified details.
     *
     * @param eventName the name of the event
     * @param date the date of the event
     * @param time the time of the event
     * @param description the description of the event
     * @param maxAttendees the maximum number of attendees allowed for the event
     * @param maxWaitlist the maximum number of people allowed on the waitlist, nullable
     * @param geolocationEnabled whether geolocation is enabled for this event
     * @param qrCodeLink the link for the QR code associated with this event
     */
    public Event(String eventName, String date, String time, String description, int maxAttendees, Integer maxWaitlist, boolean geolocationEnabled, String qrCodeLink) {
        this.eventName = eventName;
        this.date = date;
        this.time = time;
        this.description = description;
        this.maxAttendees = maxAttendees;
        this.maxWaitlist = maxWaitlist;
        this.geolocationEnabled = geolocationEnabled;
        this.qrCodeLink = qrCodeLink;
        this.waitingList = new WaitingList();
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(int maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public Integer getMaxWaitlist() {
        return maxWaitlist;
    }

    public void setMaxWaitlist(Integer maxWaitlist) {
        this.maxWaitlist = maxWaitlist;
    }

    public boolean isGeolocationEnabled() {
        return geolocationEnabled;
    }

    public void setGeolocationEnabled(boolean geolocationEnabled) {
        this.geolocationEnabled = geolocationEnabled;
    }

    public String getQrCodeLink() {
        return qrCodeLink;
    }

    public void setQrCodeLink(String qrCodeLink) {
        this.qrCodeLink = qrCodeLink;
    }

    /**
     * Adds a user to the waiting list for the event, if there is capacity.
     *
     * @param user the user to add to the waiting list
     * @param waitingCapacity the maximum capacity of the waiting list
     * @return true if the user was successfully added, false otherwise
     */
    public boolean addWaitingUser(UserProfile user, int waitingCapacity) {
        return waitingList.addWaiter(user, waitingCapacity);
    }

    /**
     * Removes a user from the waiting list.
     *
     * @param user the user to remove from the waiting list
     * @return true if the user was successfully removed, false otherwise
     */
    public boolean removeWaitingUser(UserProfile user) {
        return waitingList.removeWaiter(user);
    }

    /**
     * Selects a specified number of users from the waiting list.
     *
     * @param selectedCapacity the number of users to select from the waiting list
     * @return a list of selected UserProfile objects
     */
    public List<UserProfile> sampleAttendees(int selectedCapacity) {
        return waitingList.sampleAttendees(selectedCapacity);
    }

    /**
     * Sends notifications to sampled attendees from the waiting list.
     *
     * @param sampledAttendees a list of sampled attendees represented as maps of user attributes
     * @param context the context from which to send the notifications
     * @param notificationService the service used to send notifications
     */
    public void notifySampledAttendees(List<Map<String, Object>> sampledAttendees, Context context, NotificationService notificationService) {
        for (Map<String, Object> user : sampledAttendees) {
            String title = "Congratulations!";
            String description = "You have been selected from the waiting list";
            NotificationService.sendNotification(user, context, title, description);
        }
    }
}
