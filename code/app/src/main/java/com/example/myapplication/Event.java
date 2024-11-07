package com.example.myapplication;

public class Event {
    private String eventName;
    private String date;
    private String time;
    private String description;
    private int maxAttendees;
    private int maxWaitlist;
    private boolean geolocationEnabled;

    // Constructor
    public Event(String eventName, String date, String time, String description, int maxAttendees, int maxWaitlist, boolean geolocationEnabled) {
        this.eventName = eventName;
        this.date = date;
        this.time = time;
        this.description = description;
        this.maxAttendees = maxAttendees;
        this.maxWaitlist = maxWaitlist;
        this.geolocationEnabled = geolocationEnabled;
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

    public int getMaxWaitlist() {
        return maxWaitlist;
    }

    public void setMaxWaitlist(int maxWaitlist) {
        this.maxWaitlist = maxWaitlist;
    }

    public boolean isGeolocationEnabled() {
        return geolocationEnabled;
    }

    public void setGeolocationEnabled(boolean geolocationEnabled) {
        this.geolocationEnabled = geolocationEnabled;
    }
}

//    public String getQrCodeLink() {
//        return qrCodeLink;
//    }
//
//    public void setQrCodeLink(String qrCodeLink) {
//        this.qrCodeLink = qrCodeLink;
//    }
//

//    public boolean addWaitingUser(UserProfile user, int waitingCapacity) {
//        return waitingList.addWaiter(user, waitingCapacity);
//    }
//    /*
//    Removes a user from the waiting list
//     */
//    public boolean removeWaitingUser(UserProfile user) {
//        return waitingList.removeWaiter(user);
//    }
//    /*
//    Samples a specified number of users from the waiting list
//     */
//    public List<UserProfile> sampleAttendees(int selectedCapacity) {
//        return waitingList.sampleAttendees(selectedCapacity);
//    }
//
//    public void notifySampledAttendees(List<UserProfile> sampledAttendees, Context context, NotificationService notificationService) {
//        for (UserProfile user: sampledAttendees) {
//            String title = "Congratulations!";
//            String description = "You have been selected from the waiting list";
//            NotificationService.sendNotification(user, context, title, description);
//
//        }
//    }


