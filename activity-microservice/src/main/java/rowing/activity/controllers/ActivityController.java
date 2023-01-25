package rowing.activity.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.h2.engine.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import rowing.activity.authentication.AuthManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import rowing.activity.domain.entities.Activity;
import rowing.activity.domain.entities.Match;
import rowing.activity.domain.repositories.ActivityRepository;
import rowing.activity.domain.repositories.MatchRepository;
import rowing.activity.services.ActivityService;
import rowing.commons.Certificates;
import rowing.commons.CoxCertificate;
import rowing.commons.NotificationStatus;
import rowing.commons.Position;
import rowing.commons.entities.ActivityDTO;
import rowing.commons.entities.CompetitionDTO;
import rowing.commons.entities.UpdateUserDTO;
import rowing.commons.entities.MatchingDTO;
import rowing.commons.entities.UserDTO;
import rowing.commons.models.UserDTORequestModel;
import rowing.commons.entities.CompetitionDTO;
import rowing.commons.entities.UpdateUserDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Hello World example controller.
 * <p>
 * This controller shows how you can extract information from the JWT token.
 * </p>
 */
@RestController
@RequestMapping("/activity")
public class ActivityController {

    private final transient AuthManager authManager;
    private final transient ActivityRepository activityRepository;
    private final transient MatchRepository matchRepository;

    private final transient ActivityService activityService;

    /**
     * Instantiates a new controller.
     *
     * @param authManager        Spring Security component used to authenticate and authorize the user
     * @param activityRepository the activity repository to be used
     * @param matchRepository    the match repository to be used
     * @param activityService    the activity service to be used
     */
    @Autowired
    public ActivityController(AuthManager authManager, ActivityRepository activityRepository,
                              MatchRepository matchRepository, ActivityService activityService) {
        this.authManager = authManager;
        this.activityRepository = activityRepository;
        this.matchRepository = matchRepository;
        this.activityService = activityService;
    }

    /**
     * Endpoint to create a new activity.
     *
     * @param dto that will contain basic activity information
     * @return response OK if the activity has been created
     */
    @PostMapping("/new")
    public ResponseEntity<String> createActivity(@RequestBody ActivityDTO dto) {
        return ResponseEntity.ok(activityService.createActivity(dto));
    }

    /**
     * Endpoint to retrieve every activity in the repository in a list of ActivityDTO objects.
     *
     * @return response OK if the activities are returned successfully
     */
    @GetMapping("/activityList")
    public ResponseEntity<List<ActivityDTO>> getActivities() {
        return ResponseEntity.ok(activityService.getActivities());
    }

    /**
     * Enpoint to remove activity based on the activity id.
     *
     * @param activityId - the id of the activity to be removed
     * @return activityDTO - the activity that has been deleted
     */
    @GetMapping("/{activityId}/delete")
    public ResponseEntity<ActivityDTO> deleteActivity(@PathVariable("activityId") UUID activityId) {
        ActivityDTO activityDTO;
        try {
            activityDTO = activityService.deleteActivity(activityId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity was not found", e);
        }
        return ResponseEntity.ok(activityDTO);
    }

    /** Endpoint to update the activity.
     *
     * @param activityId - the id of the activity to be updated
     * @param dto - the activityDTO that contains the new information
     * @return activityDTO - the activity that has been updated
     */
    @PatchMapping("/{activityId}/update-activity")
    public ResponseEntity<String> updateUser(@PathVariable("activityId") UUID activityId, @RequestBody ActivityDTO dto)
        throws JsonProcessingException {
        Optional<Activity> optionalActivity = activityRepository.findActivityById(activityId);
        if (!optionalActivity.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activity does not exist !");
        }
        Activity activity = optionalActivity.get();
        if (!authManager.getUsername().equals(activity.getOwner())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the owner of the activity can edit an activity !");
        }

        String result = "Activity has not been updated";
        try {
            result = activityService.updateActivity(activityId, dto);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Activity does not exist !")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activity does not exist !");
            }
            if (e.getMessage().equals(("Activity start time is in the past !"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Activity start time is in the past !");
            }
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint that lets the user connect to the activity using the id.
     *
     * @param match dto containing information regarding the signup process
     * @return response if the sign-up was successful or not
     */
    @PostMapping("/sign/{activityId}")
    public ResponseEntity<String> signUpActivity(@RequestBody MatchingDTO match, @PathVariable UUID activityId) {
        String response = "";
        try {
            response = activityService.signUp(match);
        } catch (IllegalArgumentException | JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
            //throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity was not found", e);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint that lets the user disconnect from the activity using the id.
     *
     * @param activityId - the activity to disconnect from
     * @return response if the sign-off was successful or not
     */
    @PostMapping("/signOff/{activityId}")
    public ResponseEntity<String> signOffActivity(@PathVariable UUID activityId) {
        String response = "";
        try {
            response = activityService.signOff(activityId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for accepting a specific user to an activity with the chosen position.
     *
     * @param activityId the id of the activity
     * @param model the UserDTORequestModel keeping the information about the selected user and position
     * @return a ResponseEntity of string to notify what happened
     * @throws JsonProcessingException if there is a problem occurs when converting
     *         the NotificationRequestModel object to Json
     */
    @PostMapping("/{activityId}/accept")
    public ResponseEntity<String> acceptUser(@PathVariable("activityId") UUID activityId,
                                             @RequestBody UserDTORequestModel model) throws JsonProcessingException {
        Optional<Activity> optionalActivity = activityRepository.findActivityById(activityId);
        if (!optionalActivity.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activity does not exist!");
        }
        Activity activity = optionalActivity.get();
        if (!authManager.getUsername().equals(activity.getOwner())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the owner of the activity can accept users");
        }
        if (matchRepository.existsByActivityId(activityId)) {
            List<Match> matches = matchRepository.findAllByActivityId(activityId);
            for (Match match : matches) {
                if (match.getUserId().equals(model.getUserId())) {
                    return ResponseEntity.badRequest().body("This user is already participating in the activity");
                }
            }
        }
        if (!model.getRowingPositions().contains(model.getPositionSelected())) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("The user didn't apply for this position");
        }
        if (!activity.getApplicants().contains(model.getUserId())) {
            return ResponseEntity.badRequest().body("This user didn't apply for this activity");
        }
        if (!activity.getPositions().contains(model.getPositionSelected())) {
            return ResponseEntity.badRequest().body("This position is already full");
        }
        if (model.getPositionSelected().equals(Position.COX)) {
            List<String> certificates = model.getCoxCertificates();
            List<CoxCertificate> coxCertificates = new ArrayList<>();
            for (String s : certificates) {
                if (Certificates.existByName(s)) {
                    coxCertificates.add(Certificates.getCertificate(s));
                }
            }
            boolean exists = false;
            for (CoxCertificate certificate : coxCertificates) {
                if (certificate.getName().equals(activity.getBoatType())) {
                    exists = true;
                    break;
                }
                if (certificate.getSupersedes() != null) {
                    for (String supersede : certificate.getSupersedes()) {
                        if (supersede.equals(activity.getBoatType())) {
                            exists = true;
                            break;
                        }
                    }
                }
            }
            if (!exists) {
                return ResponseEntity.badRequest().body("The user don't have a certificate for this boat type!");
            }
        }

        return ResponseEntity.ok(activityService.acceptUser(activity, model));
    }

    /**
     * Endpoint for rejecting a specific user to an activity.
     *
     * @param activityId the id of the activity
     * @param model the UserDTO keeping the information about the selected user
     * @return a ResponseEntity of string to notify what happened
     * @throws JsonProcessingException if there is a problem occurs when converting
     *         the NotificationRequestModel object to Json
     */
    @PostMapping("/{activityId}/reject")
    public ResponseEntity<String> rejectUser(@PathVariable("activityId") UUID activityId,
                                             @RequestBody UserDTO model) throws JsonProcessingException {

        Optional<Activity> optionalActivity = activityRepository.findActivityById(activityId);
        if (!optionalActivity.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activity does not exist!");
        }
        Activity activity = optionalActivity.get();
        if (!authManager.getUsername().equals(activity.getOwner())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the owner of the activity can reject users");
        }
        if (matchRepository.existsByActivityId(activityId)) {
            List<Match> matches = matchRepository.findAllByActivityId(activityId);
            for (Match match : matches) {
                if (match.getUserId().equals(model.getUserId())) {
                    return ResponseEntity.badRequest().body("This user is already participating in the activity");
                }
            }
        }
        if (!activity.getApplicants().contains(model.getUserId())) {
            return ResponseEntity.badRequest().body("This user didn't apply for this activity");
        }

        return ResponseEntity.ok(activityService.rejectUser(activity, model));
    }


    /**
     * Endpoint that kicks an user from signUp and participation for a certain activity.
     *
     * @param activityId id of the activity they owner wants to kick the user from
     * @param userId id of the user that gets kicked
     * @return response if user has been kicked or not
     */
    @PostMapping("/{activityId}/kick")
    public ResponseEntity<String> kickUser(@PathVariable("activityId") UUID activityId,
                                             @RequestBody String userId)  {
        String response;
        Optional<Activity> activityOpt = activityRepository.findActivityById(activityId);
        if (activityOpt.isPresent()) {
            Activity activity = activityOpt.get();
            if (!authManager.getUsername().equals(activity.getOwner())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the owner of the activity can kick users");
            }
            try {
                response = activityService.kickUser(activity, userId);
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                response = e.getMessage();
                return ResponseEntity.badRequest().body(response);
            }
        }
        return ResponseEntity.badRequest().body("ActivityId is not correct !");
    }

    /**
     * Endpoint to return the userDTO object for the corresponding username.
     *
     * @param userId of the user
     * @return a response entity with the userDTO object inside
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String userId) {
        try {
            return ResponseEntity.ok(activityService.getUser(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint to return all participants of an activity.
     *
     * @param activityId of the activity
     * @return a response entity with the list of participants inside
     */
    @GetMapping("/{activityId}/participants")
    public ResponseEntity<List<UserDTO>> getParticipants(@PathVariable UUID activityId) {
        Optional<Activity> activityOpt = activityRepository.findActivityById(activityId);
        if (activityOpt.isPresent()) {
            Activity activity = activityRepository.findActivityById(activityId).get();;
            if (!authManager.getUsername().equals(activity.getOwner())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok(activityService.getParticipants(activityId));
        }
        return ResponseEntity.badRequest().build();
    }
}