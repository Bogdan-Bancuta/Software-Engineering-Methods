package rowing.notification.domain.notification;

import org.junit.jupiter.api.Test;
import rowing.commons.NotificationStatus;
import rowing.commons.models.NotificationRequestModel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NotificationTest {

    Map<NotificationStatus, String> notificationStatusToBody = new HashMap<>() {{
            put(NotificationStatus.ACCEPTED, "Congratulations, you were accepted to the activity with id: ");
            put(NotificationStatus.DELETED, "Unfortunately, the owner deleted the activity with id: ");
            put(NotificationStatus.REJECTED, "Unfortunately, you were rejected to participate in this activity with id: ");
            put(NotificationStatus.KICKED, "Unfortunately, you were kicked from this activity with id: ");
            put(NotificationStatus.WITHDRAWN,
                    "You have successfully withdrawn your application from the activity with id: ");
            put(NotificationStatus.CHANGES, "There is an information update regarding your activity with id ");
            put(NotificationStatus.ACTIVITY_FULL, "The activity you signed up for is full at the moment. \n"
                    + "Thus, you are currently in the waiting list for the activity with id ");
            put(NotificationStatus.DEFAULT, "You have a notification regarding your activity with id: ");
        }};
    private final Map<NotificationStatus, String> notificationStatusToSubject = new HashMap<>() {{
            put(NotificationStatus.CHANGES, "The activity you have signed up for has some changes ");
            put(NotificationStatus.ACTIVITY_FULL, "The activity you signed up for is currently full ");
            put(null, "Your status for the activity is " + "unknown");
            put(NotificationStatus.DEFAULT, "Your status for the activity is ");
        }};

    @Test
    void constructorNoChanges() {
        NotificationRequestModel accepted = new NotificationRequestModel("alex",
                NotificationStatus.ACCEPTED, new UUID(101L, 2L));
        Notification notificationAccepted = new Notification(accepted, "random@gmail.com");
        assertEquals("The date has not changed since the last update.", notificationAccepted.getNewDate());
        assertEquals("The location has not changed since the last update.", notificationAccepted.getNewLocation());
        assertEquals("00000000-0000-0065-0000-000000000002", notificationAccepted.getActivityId());
        assertEquals(NotificationStatus.ACCEPTED, notificationAccepted.getNotificationStatus());
        assertEquals("random@gmail.com", notificationAccepted.getDestinationEmail());
    }

    @Test
    void constructorWithLocationChanges() {
        NotificationRequestModel accepted = new NotificationRequestModel("alex",
                NotificationStatus.CHANGES, new UUID(101L, 2L), "Delft");
        Notification notificationAccepted = new Notification(accepted, "random@gmail.com");
        assertEquals("The date has not changed since the last update.", notificationAccepted.getNewDate());
        assertEquals("Delft", notificationAccepted.getNewLocation());
        assertEquals("00000000-0000-0065-0000-000000000002", notificationAccepted.getActivityId());
        assertEquals(NotificationStatus.CHANGES, notificationAccepted.getNotificationStatus());
        assertEquals("random@gmail.com", notificationAccepted.getDestinationEmail());
    }

    @Test
    void constructorWithDateChanges() {
        Date date = new Date();
        NotificationRequestModel accepted = new NotificationRequestModel("alex",
                NotificationStatus.CHANGES, new UUID(101L, 2L), date);
        Notification notificationAccepted = new Notification(accepted, "random@gmail.com");
        assertEquals(date.toString(), notificationAccepted.getNewDate());
        assertEquals("The location has not changed since the last update.", notificationAccepted.getNewLocation());
        assertEquals("00000000-0000-0065-0000-000000000002", notificationAccepted.getActivityId());
        assertEquals(NotificationStatus.CHANGES, notificationAccepted.getNotificationStatus());
        assertEquals("random@gmail.com", notificationAccepted.getDestinationEmail());
    }

    @Test
    void constructorLocationChanged() {
        Date date = new Date();
        NotificationRequestModel changes = new NotificationRequestModel("alex",
                NotificationStatus.CHANGES, new UUID(101L, 2L), "Delft");
        Notification notificationChanges = new Notification(changes, "random@gmail.com");
        NotifyUserService notifyUserService = new NotifyUserService(notificationStatusToBody, notificationStatusToSubject);
        String subject = "The activity you have signed up for has some changes ";
        String body = notifyUserService.getNotificationStatusToBody().get(NotificationStatus.CHANGES)
                + "00000000-0000-0065-0000-000000000002:\n"
                + "Date: " + "The date has not changed since the last update."
                + "\nLocation: Delft";
        assertEquals(subject, notifyUserService.retrieveSubject(notificationChanges));
        assertEquals(body, notifyUserService.retrieveBody(notificationChanges));
        assertEquals("The date has not changed since the last update.", notificationChanges.getNewDate());
        assertEquals("Delft", notificationChanges.getNewLocation());
    }

    @Test
    void getDestinationEmail() {
        NotificationRequestModel accepted = new NotificationRequestModel("alex",
                NotificationStatus.ACCEPTED, new UUID(101L, 2L));
        Notification notificationAccepted = new Notification(accepted, "random@gmail.com");
        assertEquals("random@gmail.com", notificationAccepted.getDestinationEmail());
    }

    @Test
    void requestInfoNull() {
        Notification notificationNullStatus = new Notification(null, "random", true);
        assertNull(notificationNullStatus.getActivityId());
    }

    @Test
    void activityIdNull() {
        NotificationRequestModel accepted = new NotificationRequestModel("alex",
                NotificationStatus.ACCEPTED, null);
        Notification notificationAccepted = new Notification(accepted, "random@gmail.com");
        assertNull(notificationAccepted.getActivityId());

        notificationAccepted = new Notification(accepted, "random", true);
        assertNull(notificationAccepted.getActivityId());
    }
}