package rowing.activity.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rowing.activity.authentication.AuthManager;
import rowing.activity.domain.CompetitionBuilder;
import rowing.activity.domain.Director;
import rowing.activity.domain.TrainingBuilder;
import rowing.activity.domain.entities.Activity;
import rowing.activity.domain.entities.Competition;
import rowing.activity.domain.entities.Match;
import rowing.activity.domain.entities.Training;
import rowing.activity.domain.repositories.ActivityRepository;
import rowing.activity.domain.repositories.MatchRepository;
import rowing.activity.domain.utils.Builder;
import rowing.commons.AvailabilityIntervals;
import rowing.commons.NotificationStatus;
import rowing.commons.entities.ActivityDTO;
import rowing.commons.entities.CompetitionDTO;
import rowing.commons.entities.MatchingDTO;
import rowing.commons.entities.UserDTO;
import rowing.commons.entities.utils.JsonUtil;
import rowing.commons.models.NotificationRequestModel;
import rowing.commons.models.UserDTORequestModel;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class ActivityService {
    private final transient ActivityRepository activityRepository;
    private final transient AuthManager authManager;
    private final transient MatchRepository matchRepository;
    @Autowired
    private transient RestTemplate restTemplate;

    @Value("${microserviceJWT}")
    private transient String token;

    @Value("${portNotification}")
    String portNotification;

    @Value("${urlNotification}")
    String urlNotification;

    @Value("${pathNotify}")
    String pathNotify;

    @Value("${portUsers}")
    String portUsers;

    @Value("${pathUserController}")
    String pathUserController;

    @Value("${pathUserAvailability}")
    String pathUserAvailability;

    /**
     * Constructor for the ActivityService class.
     *
     * @param activityRepository that will be used to keep info about activities
     *
     * @param authManager that will be used
     *
     * @param matchRepository that will be used to match users and activities
     */
    @Autowired
    public ActivityService(ActivityRepository activityRepository, AuthManager authManager,
                           MatchRepository matchRepository) {
        this.activityRepository = activityRepository;
        this.authManager = authManager;
        this.matchRepository = matchRepository;
    }

    /**
     * Gets example by id.
     *
     * @return the example found in the database with the given id
     */
    public String hellWorld() {
        return "Hello " + authManager.getUsername();
    }

    /**
     * Method to create a new activity and add it to the repository.
     *
     * @param dto that will contain basic activity information
     *
     * @return the string will be returned if the activity is added successfully
     */
    public String createActivity(ActivityDTO dto) {
        Builder builder;
        Director director;
        if (dto.getType().equals("Training")) {
            builder = new TrainingBuilder();
            director = new Director();
            director.constructTrainingDTO((TrainingBuilder) builder, dto);
            Training activity = (Training) builder.build();
            activityRepository.save(activity);
            return "Activity " + activity.getId() + " was created successfully !";
        } else {
            builder = new CompetitionBuilder();
            director = new Director();
            director.constructCompetitionDTO((CompetitionBuilder) builder, (CompetitionDTO) dto);
            Competition activity = (Competition) builder.build();
            activityRepository.save(activity);
            return "Activity " + activity.getId() + "created successfully !";
        }
    }

    /**
     * Method to retrieve every activity in the repository in a list of ActivityDTO objects.
     *
     * @return list of all activities stored in the database
     */
    public List<ActivityDTO> getActivities() {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        List<Activity> activities = activityRepository.findAll();
        List<ActivityDTO> activityDTOs = new ArrayList<>();

        List<UUID> uuids = new ArrayList<>();
        for (Activity activity : activities) {

            if (activity.getStart().after(currentDate)) {
                activityDTOs.add(activity.toDto());
            } else {
                uuids.add(activity.getId());
                activityRepository.delete(activity);
            }
        }
        for (UUID id : uuids) {
            if (matchRepository.existsByActivityId(id)) {
                matchRepository.deleteAll(matchRepository.findAllByActivityId(id));
            }
        }
        return activityDTOs;
    }

    /**
     * Deletes the activity with the specified id from the database.
     *
     * @param activityId - the UUID corresponding to the activity
     * @return activityDto - the activityDto corresponding to the deleted activity
     * @throws IllegalArgumentException - if the activity is not found in the database
     */
    public ActivityDTO deleteActivity(UUID activityId) throws IllegalArgumentException {
        Optional<Activity> activity = activityRepository.findActivityById(activityId);
        if (activity.isPresent()) {
            ActivityDTO activityDto = activity.get().toDto();
            activityRepository.delete(activity.get());
            return activityDto;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Function that checks wether a schedule is available for an activity.
     *
     * @param activity that needs to fit in the availability
     * @param availability list of intervals that could fit our activity start time
     * @return true or false
     */
    public static boolean checkAvailability(Activity activity, List<AvailabilityIntervals> availability) {
        Calendar cal = Calendar.getInstance();  // Checking availability
        cal.setTime(activity.getStart());
        var day = cal.get(Calendar.DAY_OF_WEEK);
        var time = LocalTime.ofInstant(cal.getTime().toInstant(), ZoneId.systemDefault());
        boolean available = false;

        for (AvailabilityIntervals interval : availability) {
            if (interval.getDay().getValue() == day
                    && interval.getStartInterval().isBefore(time) && interval.getEndInterval().isAfter(time)) {
                available = true;
            }
        }
        return available;
    }

    /**
     * Returns the activity with the specified id from the database.
     *
     * @param match dto that contains information about the singUp / match process
     * @return String - the response corresponding to the signUp result
     * @throws IllegalArgumentException - if the activity is not found in the database or user is not compatible
     */
    public String signUp(MatchingDTO match) throws IllegalArgumentException, JsonProcessingException {
        Optional<Activity> activity = activityRepository.findActivityById(match.getActivityId());
        if (activity.isPresent()) {
            Activity activityPresent = activity.get(); // Checking if a user is already signed up for this
            List<String> signUps = activityPresent.getApplicants();
            for (String userId : signUps) {
                if (userId.equals(match.getUserId())) {
                    throw new IllegalArgumentException("User already signed up for this activity !\n");
                }
            }

            if (!checkAvailability(activityPresent, match.getAvailability())) {
                throw new IllegalArgumentException("User is not available for this activity !");
            }
            if (activityPresent instanceof Competition) {   // Checking competition requirements
                Competition competition = (Competition) activityPresent;
                if (!match.getCompetitive()) {
                    throw new IllegalArgumentException("User is not competitive!");
                }
                if (competition.getGender() != null
                        && (competition.getGender() != match.getGender())) {
                    throw new IllegalArgumentException("User does not fit gender requirements !");
                }
                if (competition.getOrganisation() != null
                        && (!competition.getOrganisation().equals(match.getOrganisation()))) {
                    throw new IllegalArgumentException("User is not part of the organisation !");
                }
            }

            activityPresent.addApplicant(match.getUserId()); // If all is fine we add the applicant
            activityPresent = activityRepository.save(activityPresent);

            if (activityPresent.getPositions().size() < 1) {

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(token);

                NotificationRequestModel request = new NotificationRequestModel(match.getUserId(),
                        NotificationStatus.ACTIVITY_FULL, activityPresent.getId());

                String body = JsonUtil.serialize(request);
                HttpEntity requestEntity = new HttpEntity(body, headers);
                ResponseEntity responseEntity = restTemplate.exchange(
                        urlNotification + ":" + portNotification + pathNotify,
                        HttpMethod.POST, requestEntity, String.class);
                return "User " + match.getUserId() + " signed up for activity : " + match.getActivityId().toString()
                        + " but since activity was full the user is currently in the waitlist.";
            }

            return "User " + match.getUserId() + " signed up for activity : " + match.getActivityId().toString();
        }
        throw new IllegalArgumentException("Activity does not exist !");
    }

    /**
     * Accepts the user to the activity, saves the match to the matching repo, and sends a notification to the user.
     *
     * @param activity that the owner wants to accept the user for
     * @param model the UserDTORequestModel keeping the information about the selected user and position
     * @return a String that notifies that the user is accepted successfully.
     * @throws JsonProcessingException if there is a problem occurs when converting
     *         the NotificationRequestModel object to Json
     */
    public String acceptUser(Activity activity, UserDTORequestModel model) throws JsonProcessingException {
        activity.getPositions().remove(model.getPositionSelected());
        Match<MatchingDTO> match = new Match<>(new MatchingDTO(UUID.randomUUID(), activity.getId(),
                model.getUserId(), model.getPositionSelected(), model.getGender(),
                model.getCompetitive(), model.getRowingOrganization(),
                model.getAvailability(), NotificationStatus.ACCEPTED));

        match = matchRepository.save(match);
        activity = activityRepository.save(activity);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        NotificationRequestModel request = new NotificationRequestModel(model.getUserId(),
                NotificationStatus.ACCEPTED, activity.getId());

        String body = JsonUtil.serialize(request);
        HttpEntity requestEntity = new HttpEntity(body, headers);
        ResponseEntity responseEntity = restTemplate.exchange(
                urlNotification + ":" + portNotification + pathNotify,
                HttpMethod.POST, requestEntity, String.class);
        String response = "User "  + model.getUserId() + " is accepted successfully to the activity with id "
                + activity.getId();

        if (activity.getPositions().size() >= 1) {
            return response;
        }

        List<String> applicants = activity.getApplicants();
        for (String user : applicants) {
            if (!matchRepository.existsByActivityIdAndUserId(match.getActivityId(), user)) {
                request = new NotificationRequestModel(user,
                        NotificationStatus.ACTIVITY_FULL, activity.getId());
                body = JsonUtil.serialize(request);
                requestEntity = new HttpEntity(body, headers);
                responseEntity = restTemplate.exchange(
                        urlNotification + ":" + portNotification + pathNotify,
                        HttpMethod.POST, requestEntity, String.class);
                response += "\nUser " + user + " is currently in the waitlist since the activity was full.";
            }
        }
        return response;

    }

    /**
     * Signs off a user from an activity.
     *
     * @param activityId - activity to be signed off from
     * @return String - information about the user, activity and status of the operation.
     * @throws IllegalArgumentException - if the activity is not found, or the user is not signed up for this activity
     */
    public String signOff(UUID activityId) throws IllegalArgumentException {
        String username = authManager.getUsername();
        Optional<Activity> optionalActivity = activityRepository.findActivityById(activityId);
        if (!optionalActivity.isPresent()) {
            throw new IllegalArgumentException("Activity not found!");
        }
        Activity activity = optionalActivity.get();
        if (!activity.getApplicants().contains(username)) {
            throw new IllegalArgumentException("User has not signed-up for this activity");
        }
        Optional<Match> optionalMatch = matchRepository.findByActivityIdAndUserId(activityId, username);
        if (optionalMatch.isPresent()) {
            Match match = optionalMatch.get();
            activity.getPositions().add(match.getPosition());
            matchRepository.delete(match);
        }
        activity.getApplicants().remove(username);
        activityRepository.save(activity);
        return "User " + username + " has signed off from the activity : " + activityId;
    }

    /**
     * Rejects the user applied to the activity, and sends a notification to the user.
     *
     * @param activity that the owner wants to reject the user for
     * @param model the UserDTO keeping the information about the selected user
     * @return a String that notifies that the user is rejected successfully.
     * @throws JsonProcessingException if there is a problem occurs when converting
     *         the NotificationRequestModel object to Json
     */
    public String rejectUser(Activity activity, UserDTO model) throws JsonProcessingException {
        activity.getApplicants().remove(model.getUserId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        NotificationRequestModel request = new NotificationRequestModel(model.getUserId(),
                NotificationStatus.REJECTED, activity.getId());

        String body = JsonUtil.serialize(request);
        HttpEntity requestEntity = new HttpEntity(body, headers);
        ResponseEntity responseEntity = restTemplate.exchange(
                urlNotification + ":" + portNotification + pathNotify,
                HttpMethod.POST, requestEntity, String.class);

        return "User " + model.getUserId() + " is rejected successfully";
    }

    /**
     * Function that kicks an user from the repository.
     *
     * @param activity activity information
     * @param userId id of the user that needs to be kicked
     * @return message containing if the user has been kicked or not
     * @throws IllegalArgumentException if the input is invalid
     */
    public String kickUser(Activity activity, String userId) throws IllegalArgumentException {
        boolean signedUp = false;
        System.out.println(userId + "\n");
        for (int i = 0; i < activity.getApplicants().size(); i++) {
            System.out.println(activity.getApplicants().get(i).toString());
            if (activity.getApplicants().get(i).equals(userId)) {
                List<String> list = activity.getApplicants();
                list.remove(i);
                activity.setApplicants(list);
                signedUp = true;
            }
        }
        if (!signedUp) {
            throw new IllegalArgumentException("User " + userId + " was not signed up for this activity !");
        }
        activityRepository.save(activity);
        Optional<Match> match = matchRepository.findByActivityIdAndUserId(activity.getId(), userId);
        if (match.isPresent()) {
            matchRepository.delete(match.get());
            return "User " + userId + " is no longer participating !";
        }
        return "User " + userId + " kicked successfully !";
    }

    /**
     * Returns the userDTO object for the corresponding username.
     *
     * @param userId of the user
     * @return the userDTO object of the user
     */
    public UserDTO getUser(String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<String> requestEntity = new HttpEntity<>(userId, headers);
        ResponseEntity<UserDTO> response = restTemplate
                .exchange(urlNotification + ":" + portUsers + "/user/" + userId + "/get-user",
                        HttpMethod.GET, requestEntity, UserDTO.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        throw new IllegalArgumentException();
    }

    /**
     * Method to return all participants of an activity.
     *
     * @param activityId of the activity
     * @return list of users
     */
    public List<UserDTO> getParticipants(UUID activityId) {
        List<String> ids = new ArrayList<>();
        List<UserDTO> users = new ArrayList<>();

        if (!matchRepository.existsByActivityId(activityId)) {
            return users;
        }

        List<Match> matches = matchRepository.findAllByActivityId(activityId);
        matches.stream().filter(match -> match.getDto().getStatus() == NotificationStatus.ACCEPTED)
                .forEach(match -> users.add(getUser(match.getUserId())));

        return users;
    }

    /**
     * Method to update an activity in the repository.
     * Fields that can be updated are: start time, location.
     *
     * @param activityId - the UUID corresponding to the activity that will be updated
     * @param updateActivityDto - the activityDto containing the information which the activity will be updated with
     * @return activityDto - the activityDto corresponding to the updated activity
     * @throws IllegalArgumentException - if the activity is not found in the database
     * @throws JsonProcessingException - if there is a problem occurs when converting
     */
    public String updateActivity(UUID activityId, ActivityDTO updateActivityDto)
            throws IllegalArgumentException, JsonProcessingException {
        Optional<Activity> optionalActivity = activityRepository.findActivityById(activityId);
        if (optionalActivity.isEmpty()) {
            throw new IllegalArgumentException("Activity does not exist !");
        }
        Activity activity = optionalActivity.get();
        Optional<Date> optionalStart = Optional.ofNullable(updateActivityDto.getStart());
        Optional<String> optionalLocation = Optional.ofNullable(updateActivityDto.getLocation());

        NotificationRequestModel requestModel =
                createUpdateNotificationRequestModel(activity, optionalStart, optionalLocation);

        // Send notification to all participants
        String uriNotification = urlNotification + ":" + portNotification + pathNotify;
        HttpHeaders headersNotification = new HttpHeaders();
        headersNotification.setContentType(MediaType.APPLICATION_JSON);
        headersNotification.setBearerAuth(token);

        for (String username : getParticipantIDs(activityId)) {
            requestModel.setUsername(username);
            String bodyNotification = JsonUtil.serialize(requestModel);
            HttpEntity requestNotification = new HttpEntity(bodyNotification, headersNotification);
            ResponseEntity<String> responseNotification = restTemplate.exchange(
                    uriNotification,
                    HttpMethod.POST, requestNotification, String.class);

            //Check if any participants are not available for the new date and remove them from the activity if they are not
            if (optionalStart.isPresent()) {
                removeUnavailableUser(activity, username);
            }
        }

        activityRepository.save(activity);
        return "Activity" + activityId + "has been updated successfully";
    }

    private void removeUnavailableUser(Activity activity, String username) {
        //building the request for the user availability
        String uriUser = urlNotification + ":" + portUsers + pathUserController + pathUserAvailability;
        HttpHeaders headersUser = new HttpHeaders();
        headersUser.setContentType(MediaType.APPLICATION_JSON);
        headersUser.setBearerAuth(token);
        HttpEntity requestHttpUser = new HttpEntity(username, headersUser);

        //sending the request
        ResponseEntity<List<AvailabilityIntervals>> response =
                restTemplate.exchange(uriUser, HttpMethod.GET, requestHttpUser,
                        new ParameterizedTypeReference<List<AvailabilityIntervals>>() {
                        }, username);

        //checking the response
        if (response.getStatusCode() == HttpStatus.OK) {
            List<AvailabilityIntervals> availability = response.getBody();
            if (!checkAvailability(activity, availability)) {
                kickUser(activity, username);
            }
        }
    }

    private static NotificationRequestModel createUpdateNotificationRequestModel(Activity activity,
                                     Optional<Date> optionalStart, Optional<String> optionalLocation) {
        NotificationRequestModel requestModel = new NotificationRequestModel(null,
                NotificationStatus.CHANGES,
                activity.getId());

        if (optionalStart.isPresent()) {
            Date newStart = optionalStart.get();
            checkNewStart(newStart);  // Checking if the new start is in the future
            activity.setStart(newStart);
            requestModel.setDate(newStart);
        }

        if (optionalLocation.isPresent()) {
            String newLocation = optionalLocation.get();
            activity.setLocation(newLocation);
            requestModel.setLocation(newLocation);
        }
        return requestModel;
    }

    /**
     * Gets the list of accepted participants of an activity.
     *
     * @param activityId - the UUID corresponding to the activity
     * @return the list of accepted participants
     */
    public List<String> getParticipantIDs(UUID activityId) {
        List<String> participants = new ArrayList<>();

        if (matchRepository.existsByActivityId(activityId)) {
            List<Match> matches = matchRepository.findAllByActivityId(activityId);
            for (Match match : matches) {
                if (match.getDto().getStatus() == NotificationStatus.ACCEPTED) {
                    participants.add(match.getUserId());
                }
            }
        }
        return participants;
    }

    /**
     * Method to check if the new start date is valid.
     *
     * @param newStart - the new start date
     * @throws IllegalArgumentException - if the new start date is invalid
     */
    public static boolean checkNewStart(Date newStart)
            throws IllegalArgumentException {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        if (currentDate.after(newStart)) {
            throw new IllegalArgumentException("Activity start time is in the past !");
        }
        return true;
    }

}
