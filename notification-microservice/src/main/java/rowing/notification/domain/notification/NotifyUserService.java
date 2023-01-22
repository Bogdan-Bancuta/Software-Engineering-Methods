package rowing.notification.domain.notification;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import rowing.commons.NotificationStatus;
import rowing.commons.models.NotificationRequestModel;
import rowing.notification.domain.notification.strategy.Strategy;
import rowing.notification.domain.notification.strategy.StrategyFactory;
import rowing.notification.domain.notification.strategy.StrategyName;

import javax.annotation.Resource;
import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.Map;


@Service
public class NotifyUserService {
    @Autowired
    private transient StrategyFactory strategyFactory;

    @Value("${uri.users.url}")
    private transient String url;

    @Value("${uri.users.port}")
    private transient String port;

    @Value("${uri.users.getemailpath}")
    private transient String emailPath;

    @Value("${microserviceJWT}")
    private transient String token;

    private Map<NotificationStatus, String> notificationStatusToBody;

    private transient Map<NotificationStatus, String> notificationStatusToSubject;

    @Autowired
    transient RestTemplate restTemplate;

    /**
     * Constructor used for testing purposes.
     *
     * @param notificationStatusToBody map containing the body for each status
     * @param notificationStatusToSubject map containing subject for each status
     */
    public NotifyUserService(Map<NotificationStatus, String> notificationStatusToBody,
                             Map<NotificationStatus, String> notificationStatusToSubject) {
        this.notificationStatusToBody = notificationStatusToBody;
        this.notificationStatusToSubject = notificationStatusToSubject;
    }

    /**
     * Constructor used for auto initialization of the service.
     *
     */
    public NotifyUserService() {
    }

    /**
     * Setter used to autowire the map containing notification bodies.
     *
     * @param bodyAccepted body for accepted status
     * @param bodyDeleted body for deleted status
     * @param bodyRejected body for rejected status
     * @param bodyKicked body for kicked status
     * @param bodyWithdrawn body for withdrawn status
     * @param bodyChanges body for changes status
     * @param bodyFull body for full status
     * @param bodyDefault body for default status
     */
    @Autowired
    public void setNotificationStatusToBody(@Value("${body.notification.accepted}") String bodyAccepted,
                                            @Value("${body.notification.deleted}") String bodyDeleted,
                                            @Value("${body.notification.rejected}") String bodyRejected,
                                            @Value("${body.notification.kicked}") String bodyKicked,
                                            @Value("${body.notification.withdrawn}") String bodyWithdrawn,
                                            @Value("${body.notification.activityChanges}") String bodyChanges,
                                            @Value("${body.notification.activityFull}") String bodyFull,
                                            @Value("${body.notification.default}") String bodyDefault) {
        this.notificationStatusToBody =  new HashMap<>() {{
                put(NotificationStatus.ACCEPTED, bodyAccepted);
                put(NotificationStatus.DELETED, bodyDeleted);
                put(NotificationStatus.REJECTED, bodyRejected);
                put(NotificationStatus.KICKED, bodyKicked);
                put(NotificationStatus.WITHDRAWN, bodyWithdrawn);
                put(NotificationStatus.CHANGES, bodyChanges);
                put(NotificationStatus.ACTIVITY_FULL, bodyFull);
                put(NotificationStatus.DEFAULT, bodyDefault);
            }};
    }

    /**
     * Setter used to autowire the map containing notification subjects.
     *
     * @param subjectChanges subject for changed status
     * @param subjectFull subject for full status
     * @param subjectGeneral subject for all other statuses
     */
    @Autowired
    public void setNotificationStatusToSubject(@Value("${subject.notification.activityChanges}") String subjectChanges,
                                               @Value("${subject.notification.activityFull}") String subjectFull,
                                               @Value("${subject.notification.general}") String subjectGeneral) {
        this.notificationStatusToSubject = new HashMap<>() {{
                put(NotificationStatus.CHANGES, subjectChanges);
                put(NotificationStatus.ACTIVITY_FULL, subjectFull);
                put(null, subjectGeneral + "unknown");
                put(NotificationStatus.DEFAULT, subjectGeneral);
            }};
    }

    /**
     * This method calls the endpoint inside the users microservice to receive the user's email address.
     *  If the email address is found then it continues with the EMAIL strategy, else with the KAFKA strategy.

     * @param request - request that was received
     */
    public void notifyUser(NotificationRequestModel request) throws ConfigurationException {
        if (!validateRequest(request)) {
            throw new IllegalArgumentException();
        }
        if (!validateConfiguration()) {
            throw new ConfigurationException("uri or token not configured properly");
        }
        HttpEntity requestHttp = buildRequest(request);

        //sending the request
        String uri = url + ":" + port + emailPath;
        try {
            // send request and get response
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, requestHttp, String.class);
            executeNotification(StrategyName.EMAIL, request, response.getBody(), false);
        } catch (RestClientException e) {
            handleError(e, request);
        }
    }

    private void handleError(RestClientException e, NotificationRequestModel request) {
        if (e.getMessage().contains("404")) {
            System.out.println(e.getMessage());
            executeNotification(StrategyName.KAFKA, request, request.getUsername(), true);
        } else {
            throw e;
        }
    }

    private HttpEntity buildRequest(NotificationRequestModel request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        String body = "{\"username\":\"" + request.getUsername() + "\"}";
        return new HttpEntity(body, headers);
    }

    private boolean validateRequest(NotificationRequestModel request) {
        if (request == null || request.getUsername() == null
                || request.getActivityId() == null || request.getStatus() == null) {
            return false;
        }
        if (request.getStatus().equals(NotificationStatus.CHANGES)
                && (request.getDate() == null && request.getLocation() == null)) {
            return false;
        }
        return true;
    }


    private boolean validateConfiguration() {
        return url != null
                && port != null && emailPath != null
                && token != null;
    }

    private void executeNotification(StrategyName strategyName,
                                     NotificationRequestModel request,
                                     String response,
                                     boolean useKafka) {
        Strategy strategy = strategyFactory.findStrategy(strategyName);
        Notification notification;
        if (strategyName == StrategyName.EMAIL) {
            notification = new Notification(request, response);
        } else {
            notification = new Notification(request, response, useKafka);
        }
        strategy.notifyUser(notification);
    }


    /**
     * Getter for the map containing notification bodies.
     *
     * @return the map containing notification bodies.
     */
    public Map<NotificationStatus, String> getNotificationStatusToBody() {
        return notificationStatusToBody;
    }

    /**
     * Setter for url.
     *
     * @param url the url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Setter for the port.
     *
     * @param port the port
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Setter for the email path.
     *
     * @param emailPath the email path
     */
    public void setEmailPath(String emailPath) {
        this.emailPath = emailPath;
    }

    /**
     * Setter for the strategy factory.
     *
     * @param strategyFactory the strategy factory
     */
    public void setStrategyFactory(StrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    /**
     * Setter for the token.
     *
     * @param token the token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Setter for the rest template.
     *
     * @param restTemplate the rest template
     */
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Generate body text for the email based on the status.
     *
     * @return the body of an email in a string format
     */
    public String retrieveBody(Notification notification) {
        if (notification.getNotificationStatus() == null) {
            return notificationStatusToBody.get(NotificationStatus.DEFAULT) + notification.getActivityId();
        }

        String body = notificationStatusToBody.getOrDefault(notification.getNotificationStatus(),
                notificationStatusToBody.get(NotificationStatus.DEFAULT));
        if (notification.getNotificationStatus() == NotificationStatus.CHANGES) {
            body += notification.getActivityId() + ":\n"
                    + "Date: " + notification.getNewDate()
                    + "\nLocation: " + notification.getNewLocation();
            return body;
        }
        return body + notification.getActivityId();
    }

    /**
     * Retrieves the subject of an email based on the status of user in
     * this notification.
     *
     * @return String representing an email's subject
     */
    public String retrieveSubject(Notification notification) {
        return notificationStatusToSubject.getOrDefault(notification.getNotificationStatus(),
                notificationStatusToSubject.get(NotificationStatus.DEFAULT)
                        + notification.getNotificationStatus());
    }
}
