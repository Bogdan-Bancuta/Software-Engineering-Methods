package rowing.notification.domain.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import rowing.commons.NotificationStatus;
import rowing.commons.models.NotificationRequestModel;
import rowing.notification.domain.notification.strategy.EmailStrategy;
import rowing.notification.domain.notification.strategy.KafkaStrategy;
import rowing.notification.domain.notification.strategy.StrategyFactory;
import rowing.notification.domain.notification.strategy.StrategyName;

import javax.naming.ConfigurationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class NotifyUserServiceTest {
    private StrategyFactory strategyFactory;
    private RestTemplate restTemplate;
    private NotifyUserService notifyUserService;
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
    private Map<NotificationStatus, String> notificationStatusToSubject = new HashMap<>() {{
            put(NotificationStatus.CHANGES, "The activity you have signed up for has some changes ");
            put(NotificationStatus.ACTIVITY_FULL, "The activity you signed up for is currently full ");
            put(null, "Your status for the activity is " + "unknown");
            put(NotificationStatus.DEFAULT, "Your status for the activity is ");
        }};
    private NotificationRequestModel requestModel;

    @BeforeEach
    void setup() {
        // mock strategy factory
        strategyFactory = mock(StrategyFactory.class);
        EmailStrategy emailStrategy = mock(EmailStrategy.class);
        //when(emailStrategy).notifyUser(any(Notification.class))
        KafkaStrategy kafkaStrategy = mock(KafkaStrategy.class);
        when(strategyFactory.findStrategy(StrategyName.EMAIL)).thenReturn(emailStrategy);
        when(strategyFactory.findStrategy(StrategyName.KAFKA)).thenReturn(kafkaStrategy);

        // mock restTemplate
        restTemplate = mock(RestTemplate.class);

        // instantiate NotifyUserService
        notifyUserService = new NotifyUserService(notificationStatusToBody, notificationStatusToSubject);
        notifyUserService.setUrl("localhost");
        notifyUserService.setPort("8080");
        notifyUserService.setEmailPath("/getEmail");
        notifyUserService.setToken("valitTokenBruh");
        notifyUserService.setRestTemplate(restTemplate);
        notifyUserService.setStrategyFactory(strategyFactory);

        // mock request model
        requestModel = mock(NotificationRequestModel.class);
        when(requestModel.getUsername()).thenReturn("test");
        when(requestModel.getActivityId()).thenReturn(new UUID(1, 1));
        when(requestModel.getStatus()).thenReturn(NotificationStatus.ACCEPTED);
    }

    @Test
    void exception1() {
        assertThrows(IllegalArgumentException.class, () -> {
            notifyUserService.notifyUser(null);
        });
    }

    @Test
    void exception2() {
        when(requestModel.getUsername()).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> {
            notifyUserService.notifyUser(requestModel);
        });
    }

    @Test
    void exception3() {
        when(requestModel.getActivityId()).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> {
            notifyUserService.notifyUser(requestModel);
        });
    }

    @Test
    void exception4() {
        when(requestModel.getStatus()).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> {
            notifyUserService.notifyUser(requestModel);
        });
    }

    @Test
    void exception5() throws ConfigurationException {
        when(requestModel.getStatus()).thenReturn(NotificationStatus.CHANGES);
        when(requestModel.getDate()).thenReturn(null);
        when(requestModel.getLocation()).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> {
            notifyUserService.notifyUser(requestModel);
        });

        when(requestModel.getLocation()).thenReturn("delft");

        ResponseEntity<String> response = new ResponseEntity<>("test@gmail.com", HttpStatus.OK);
        doReturn(response).when(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                (Class<?>) any(Class.class));

        notifyUserService.notifyUser(requestModel);

        when(requestModel.getLocation()).thenReturn(null);
        when(requestModel.getDate()).thenReturn(new Date());
        notifyUserService.notifyUser(requestModel);
    }

    @Test
    void sendKafkaMessage() throws ConfigurationException {
        doThrow(new RestClientException(NOT_FOUND.toString()))
                .when(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                        (Class<?>) any(Class.class));

        notifyUserService.notifyUser(requestModel);
    }

    @Test
    void otherExceptionGetEmail() {
        doThrow(new RestClientException(HttpStatus.BAD_REQUEST.toString()))
                .when(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                        (Class<?>) any(Class.class));

        assertThrows(RestClientException.class, () -> {
            notifyUserService.notifyUser(requestModel);
        });
    }

    @Test
    void nullUrlException() {
        notifyUserService.setUrl(null);

        assertThrows(ConfigurationException.class, () -> {
            notifyUserService.notifyUser(requestModel);
        }, "uri or token not configured properly");

        notifyUserService.setUrl("some");
        notifyUserService.setPort(null);

        assertThrows(ConfigurationException.class, () -> {
            notifyUserService.notifyUser(requestModel);
        }, "uri or token not configured properly");

        notifyUserService.setPort("some");
        notifyUserService.setEmailPath(null);

        assertThrows(ConfigurationException.class, () -> {
            notifyUserService.notifyUser(requestModel);
        }, "uri or token not configured properly");

        notifyUserService.setEmailPath("some");
        notifyUserService.setToken(null);

        assertThrows(ConfigurationException.class, () -> {
            notifyUserService.notifyUser(requestModel);
        }, "uri or token not configured properly");
    }

    @Test
    void subjectAccepted() {
        NotificationRequestModel accepted = new NotificationRequestModel("alex",
                NotificationStatus.ACCEPTED, new UUID(101L, 2L));
        Notification notificationAccepted = new Notification(accepted, "random@gmail.com");
        String answer = "Your status for the activity is ACCEPTED";
        assertEquals(answer, notifyUserService.retrieveSubject(notificationAccepted));
    }

    @Test
    void subjectRejected() {
        NotificationRequestModel rejected = new NotificationRequestModel("alex",
                NotificationStatus.REJECTED, new UUID(101L, 2L));
        Notification notificationRejected = new Notification(rejected, "random@gmail.com");
        String answer = "Your status for the activity is REJECTED";
        assertEquals(answer, notifyUserService.retrieveSubject(notificationRejected));
    }

    @Test
    void subjectKicked() {
        NotificationRequestModel kicked = new NotificationRequestModel("alex",
                NotificationStatus.KICKED, new UUID(101L, 2L));
        Notification notificationKicked = new Notification(kicked, "random@gmail.com");
        String answer = "Your status for the activity is KICKED";
        assertEquals(answer, notifyUserService.retrieveSubject(notificationKicked));
    }

    @Test
    void subjectDeleted() {
        NotificationRequestModel deleted = new NotificationRequestModel("alex",
                NotificationStatus.DELETED, new UUID(101L, 2L));
        Notification notificationDeleted = new Notification(deleted, "random@gmail.com");
        String answer = "Your status for the activity is DELETED";
        assertEquals(answer, notifyUserService.retrieveSubject(notificationDeleted));
    }

    @Test
    void subjectWithdrawn() {
        NotificationRequestModel withdrawn = new NotificationRequestModel("alex",
                NotificationStatus.WITHDRAWN, new UUID(101L, 2L));
        Notification notificationWithdrawn = new Notification(withdrawn, "random@gmail.com");
        String answer = "Your status for the activity is WITHDRAWN";
        assertEquals(answer, notifyUserService.retrieveSubject(notificationWithdrawn));
    }

    @Test
    void subjectChanges() {
        Date date = new Date();
        NotificationRequestModel changes = new NotificationRequestModel("alex",
                NotificationStatus.CHANGES, new UUID(101L, 2L), "Delft");
        Notification notificationChanges = new Notification(changes, "random@gmail.com");
        String subject = "The activity you have signed up for has some changes ";
        assertEquals(subject, notifyUserService.retrieveSubject(notificationChanges));
    }

    @Test
    void subjectActivityFull() {
        NotificationRequestModel activityFull = new NotificationRequestModel("alex",
                NotificationStatus.ACTIVITY_FULL,
                new UUID(101L, 2L));
        Notification notificationFull = new Notification(activityFull, "random@gmail.com");
        String answer = "The activity you signed up for is currently full ";
        assertEquals(answer, notifyUserService.retrieveSubject(notificationFull));
    }

    @Test
    void subjectNull() {
        Notification notificationNullStatus = new Notification(null, "random@gmail.com");
        String answer = "Your status for the activity is unknown";
        assertEquals(answer, notifyUserService.retrieveSubject(notificationNullStatus));
    }

    @Test
    void retrieveBodyAccepted() {
        String answer = "Congratulations, you were accepted to the activity with id: 00000000-0000-0065-0000-000000000002";
        NotificationRequestModel accepted = new NotificationRequestModel("alex",
                NotificationStatus.ACCEPTED, new UUID(101L, 2L));
        Notification notificationAccepted = new Notification(accepted, "random@gmail.com");
        assertEquals(answer, notifyUserService.retrieveBody(notificationAccepted));
    }

    @Test
    void retrieveBodyRejected() {
        String answer = "Unfortunately, you were rejected to participate in this activity with id: "
                + "00000000-0000-0065-0000-000000000002";
        NotificationRequestModel rejected = new NotificationRequestModel("alex",
                NotificationStatus.REJECTED, new UUID(101L, 2L));
        Notification notificationRejected = new Notification(rejected, "random@gmail.com");
        assertEquals(answer, notifyUserService.retrieveBody(notificationRejected));
    }

    @Test
    void retrieveBodyWithdrawn() {
        NotificationRequestModel withdrawn = new NotificationRequestModel("alex",
                NotificationStatus.WITHDRAWN, new UUID(101L, 2L));
        Notification notificationWithdrawn = new Notification(withdrawn, "random@gmail.com");
        String answer = "You have successfully withdrawn your"
                + " application from the activity with id: 00000000-0000-0065-0000-000000000002";
        assertEquals(answer, notifyUserService.retrieveBody(notificationWithdrawn));
    }

    @Test
    void retrieveBodyKicked() {
        NotificationRequestModel kicked = new NotificationRequestModel("alex",
                NotificationStatus.KICKED, new UUID(101L, 2L));
        Notification notificationKicked = new Notification(kicked, "random@gmail.com");
        String answer = "Unfortunately, you were kicked from this activity with id: " + kicked.getActivityId();
        assertEquals(answer, notifyUserService.retrieveBody(notificationKicked));
    }

    @Test
    void retrieveBodyDeleted() {
        NotificationRequestModel deleted = new NotificationRequestModel("alex",
                NotificationStatus.DELETED,
                new UUID(101L, 2L));
        Notification notificationDeleted = new Notification(deleted, "random@gmail.com");
        String answer = "Unfortunately, the owner deleted the activity with id: " + deleted.getActivityId();
        assertEquals(answer, notifyUserService.retrieveBody(notificationDeleted));
    }

    @Test
    void retrieveBodyUnknownStatus() {
        Notification notificationNullStatus = new Notification(null, "random@gmail.com");
        String answer = "You have a notification regarding your activity with id: null";
        assertEquals(answer, notifyUserService.retrieveBody(notificationNullStatus));
    }

    @Test
    void retrieveBodyLocationChange() {
        NotificationRequestModel changes = new NotificationRequestModel("alex",
                NotificationStatus.CHANGES, new UUID(101L, 2L), "Delft");
        Notification notificationChanges = new Notification(changes, "random@gmail.com");
        String body = notifyUserService.getNotificationStatusToBody().get(NotificationStatus.CHANGES)
                + "00000000-0000-0065-0000-000000000002:\n"
                + "Date: "
                + "The date has not changed since the last update."
                + "\nLocation: Delft";
        assertEquals(body, notifyUserService.retrieveBody(notificationChanges));
    }

    @Test
    void retrieveBodyDateChange() {
        Date date = new Date();
        NotificationRequestModel changes = new NotificationRequestModel("alex",
                NotificationStatus.CHANGES, new UUID(101L, 2L), date);
        Notification notificationChanges = new Notification(changes, "random@gmail.com");
        String body = notifyUserService.getNotificationStatusToBody().get(NotificationStatus.CHANGES)
                + "00000000-0000-0065-0000-000000000002:\n"
                + "Date: " + date + "\nLocation: The location has not changed since the last update.";
        assertEquals(body, notifyUserService.retrieveBody(notificationChanges));
    }


    @Test
    void retrieveBodyActivityFull() {
        NotificationRequestModel activityFull = new NotificationRequestModel("alex",
                NotificationStatus.ACTIVITY_FULL,
                new UUID(101L, 2L));
        Notification notificationFull = new Notification(activityFull, "random@gmail.com");
        String answer = "The activity you signed up for is full at the moment. \n"
                + "Thus, you are currently in the waiting list for the activity with id ";
        assertEquals(answer + "00000000-0000-0065-0000-000000000002",
                notifyUserService.retrieveBody(notificationFull));
    }
}