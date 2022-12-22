package rowing.activity.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rowing.activity.authentication.AuthManager;
import rowing.activity.domain.utils.Builder;
import rowing.activity.domain.CompetitionBuilder;
import rowing.activity.domain.Director;
import rowing.activity.domain.TrainingBuilder;
import rowing.activity.domain.entities.Activity;
import rowing.activity.domain.entities.Competition;
import rowing.activity.domain.entities.Match;
import rowing.activity.domain.entities.Training;
import rowing.activity.domain.repositories.ActivityRepository;
import rowing.activity.domain.repositories.MatchRepository;
import rowing.commons.AvailabilityIntervals;
import rowing.commons.NotificationStatus;
import rowing.commons.entities.ActivityDTO;
import rowing.commons.entities.CompetitionDTO;
import rowing.commons.entities.MatchingDTO;
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

    /**
     * Constructor for the ActivityService class.
     *
     * @param activityRepository that will be used to keep info about activities
     * @param authManager        that will be used
     * @param matchRepository    that will be used to match users and activities
     */
    @Autowired
    public ActivityService(ActivityRepository activityRepository, AuthManager authManager, MatchRepository matchRepository) {
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
     * @param activity     that needs to fit in the availability
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
            }

            return "User " + match.getUserId() + " signed up for activity : " + match.getActivityId().toString();
        }
        throw new IllegalArgumentException("Activity does not exist !");
    }

    /**
     * Accepts the user to the activity, saves the match to the matching repo, and sends a notification to the user.
     *
     * @param activity that the owner wants to accept the user for
     * @param model    the UserDTORequestModel keeping the information about the selected user and position
     * @return a String that notifies that the user is created successfully.
     * @throws JsonProcessingException if there is a problem occurs when converting
     *                                 the NotificationRequestModel object to Json
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

        if (activity.getPositions().size() < 1) {
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
                }
            }
        }

        return "User " + model.getUserId() + " is accepted successfully";
    }

    public String kickUser(Activity activity, String userId) throws IllegalArgumentException {
        boolean signedUp = false;
        for (int i = 0; i < activity.getApplicants().size(); i++ ) {
            if (activity.getApplicants().get(i).equals(userId)) {
                List<String> list = activity.getApplicants();
                list.remove(i);
                activity.setApplicants(list);
                signedUp = true;
            }
        }
        if (!signedUp)
            throw new IllegalArgumentException("User " + userId + " was not signed up for this activity !");
        activityRepository.save(activity);
        Optional<Match> match = matchRepository.findByActivityIdAndUserId(activity.getId(), userId);
        if (match.isPresent()) {
            matchRepository.delete(match.get());
        }
        return "User " + userId + " kicked successfully !";
    }
}
