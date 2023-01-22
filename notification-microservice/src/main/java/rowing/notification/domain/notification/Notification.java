package rowing.notification.domain.notification;

import rowing.commons.NotificationStatus;
import rowing.commons.models.NotificationRequestModel;

public class Notification {
    private transient String activityId;
    private transient NotificationStatus notificationStatus;
    private transient String destinationEmail;
    private transient String newLocation;
    private transient String newDate;
    private transient String username;
    private transient boolean useKafka = false;

    /**
     * Constructor for the notification object that is sent through email.
     *
     * @param requestInfo - info containing the status, email and activity id
     * @param email - the email of the user to be notified
     */
    public Notification(NotificationRequestModel requestInfo, String email) {
        if (isRequestInfoValid(requestInfo)) {
            this.notificationStatus = requestInfo.getStatus();
            this.activityId = getActivityId(requestInfo);
            this.newDate = getDate(requestInfo);
            this.newLocation = getLocation(requestInfo);
            this.destinationEmail = email;
        }
    }

    private boolean isRequestInfoValid(NotificationRequestModel requestInfo) {
        return requestInfo != null && requestInfo.getActivityId() != null;
    }

    private String getActivityId(NotificationRequestModel requestInfo) {
        return requestInfo.getActivityId().toString();
    }

    /**
     * Getter for the activity id field.
     *
     * @return the activity id
     */
    public String getActivityId() {
        return activityId;
    }

    private String getDate(NotificationRequestModel requestInfo) {
        return requestInfo.getDate() != null ? requestInfo.getDate().toString() :
                "The date has not changed since the last update.";
    }

    private String getLocation(NotificationRequestModel requestInfo) {
        return requestInfo.getLocation() != null ? requestInfo.getLocation() :
                "The location has not changed since the last update.";
    }


    /**
     * Constructor for the notification object that is sent through kafka.
     *
     * @param requestInfo - info containing the status, email and activity id
     * @param username - the username of the user to be notified
     * @param useKafka - boolean that represents if kafka is to be used
     */
    public Notification(NotificationRequestModel requestInfo, String username, boolean useKafka) {
        if (isRequestInfoValid(requestInfo)) {
            this.notificationStatus = requestInfo.getStatus();
            this.activityId = getActivityId(requestInfo);
            this.newDate = getDate(requestInfo);
            this.newLocation = getLocation(requestInfo);
            this.username = username;
        }
        this.useKafka = useKafka;
    }

    /**
     * Getter for the destination email.
     *
     * @return destination email as a string
     */
    public String getDestinationEmail() {
        return this.destinationEmail;
    }

    /**
     * Getter for notification status.
     *
     * @return the notification status
     */
    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    /**
     * Getter for the new location.
     *
     * @return the new location
     */
    public String getNewLocation() {
        return newLocation;
    }

    /**
     * Getter for the new date.
     *
     * @return the new date
     */
    public String getNewDate() {
        return newDate;
    }

    /**
     * Getter for the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }
}
