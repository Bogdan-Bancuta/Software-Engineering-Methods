package rowing.activity.domain;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import rowing.activity.authentication.AuthManager;
import rowing.activity.domain.entities.Activity;
import rowing.activity.domain.entities.Competition;
import rowing.activity.domain.entities.Match;
import rowing.activity.domain.repositories.ActivityRepository;
import rowing.activity.domain.repositories.MatchRepository;
import rowing.activity.domain.utils.Builder;
import rowing.activity.services.ActivityService;
import rowing.commons.AvailabilityIntervals;
import rowing.commons.Gender;
import rowing.commons.Position;
import rowing.commons.entities.MatchingDTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class ActivityServiceTest {

    @MockBean
    private transient ActivityRepository mockActivityRepository = mock(ActivityRepository.class);
    @MockBean
    private transient MatchRepository mockMatchRepository = mock(MatchRepository.class);
    @Autowired
    private transient AuthManager mockAuthenticationManager;

    private ActivityService activityService;

    Activity amateurTraining;
    Activity amateurCompetition;
    Date amateurTrainingDate;
    Date amateurCompetitionDate;
    MatchingDTO match;
    UUID trainingId;
    UUID competitionId;
    List<AvailabilityIntervals> availability;

    /**
     * Function that inits the basic activity.
     *
     * @throws ParseException exception for wrong format
     */
    @BeforeEach
    public void init() throws ParseException {

        String dateString = "26-09-3043 14:05:05";
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        amateurTrainingDate = formatter.parse(dateString);
        trainingId = UUID.randomUUID();

        dateString = "27-09-3043 16:05:05"; // Creating new competition details
        formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        amateurCompetitionDate = formatter.parse(dateString);
        competitionId = UUID.randomUUID();

        Builder trainingBuilder = new TrainingBuilder();
        Director director = new Director();

        List<Position> positionList = new ArrayList<>();
        positionList.add(Position.COACH);
        positionList.add(Position.COX);
        List<String> applicantList = new ArrayList<>();

        director.constructTraining((TrainingBuilder) trainingBuilder, UUID.randomUUID(),
                "Admin", "Amateur Training", "Training",
                amateurTrainingDate, "Aula", positionList, applicantList, "C4");
        amateurTraining = trainingBuilder.build();

        Builder competitionBuilder = new CompetitionBuilder();
        director.constructCompetition((CompetitionBuilder) competitionBuilder, UUID.randomUUID(),
                "Admin", "Amateur Competition", "Competition",
                amateurCompetitionDate, "Aula", Gender.MALE, "TUDelft", positionList, applicantList, "C4");
        amateurCompetition = competitionBuilder.build();

        availability = new ArrayList<AvailabilityIntervals>();
        availability.add(new AvailabilityIntervals("wednesday", "14:05", "14:06"));
        availability.add(new AvailabilityIntervals("thursday", "16:05", "16:06"));
        match = new MatchingDTO(UUID.randomUUID(), null,
                "Admin", Position.COX, Gender.MALE, true, "TUDelft",
                availability, null);
    }

    @Test
    void activityTypeMatch() {
        assertThat(amateurTraining.getType().equals("Training"));
        assertThat(amateurCompetition.getType().equals("Competition"));
    }

    @Test
    public void availabilityCheckTestBoundaryLeftTrue() {
        assertTrue(ActivityService.checkAvailability(amateurTraining, match.getAvailability()));
    }

    @Test
    public void availabilityCheckTestFalse() {
        List<AvailabilityIntervals> newAvailability = new ArrayList<>();
        newAvailability.add(new AvailabilityIntervals("thursday", "16:03", "16:04"));
        match.setAvailability(newAvailability);
        assertTrue(!ActivityService.checkAvailability(amateurTraining, match.getAvailability()));
    }

    @Test
    public void availabilityCheckTestBoundaryRightFalse() { // Interval is inclusive only for start
        List<AvailabilityIntervals> newAvailability = new ArrayList<>();
        newAvailability.add(new AvailabilityIntervals("thursday", "16:04", "16:05"));
        match.setAvailability(newAvailability);
        assertTrue(!ActivityService.checkAvailability(amateurTraining, match.getAvailability()));
    }

    @Test
    public void availabilityCheckTestDifferentDay() { // Interval is inclusive only for start
        List<AvailabilityIntervals> newAvailability = new ArrayList<>();
        newAvailability.add(new AvailabilityIntervals("monday", "16:04", "16:10"));
        match.setAvailability(newAvailability);
        assertTrue(!ActivityService.checkAvailability(amateurTraining, match.getAvailability()));
    }

    @Test
    public void activityExpired() throws Exception {

        String dateString2 = "26-09-1881";
        SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");
        Date date = formatter2.parse(dateString2);

        Competition activity = new Competition();
        UUID id = UUID.randomUUID();
        activity.setId(id);
        activity.setOwner("Admin");
        activity.setName("Test Activity2");
        activity.setType("Competition");
        activity.setStart(date);
        activity.setGender(Gender.MALE);

        List<Position> positionList2 = new ArrayList<>();
        positionList2.add(Position.PORT);
        positionList2.add(Position.COX);
        activity.setPositions(positionList2);


        List<String> applicantList1 = new ArrayList<>();
        activity.setApplicants(applicantList1);

        List<Activity> activityList = new ArrayList<>();

        activityList.add(activity);

        List<Match> matches = new ArrayList<>();
        Match<MatchingDTO> match1 = new Match<>(new MatchingDTO(UUID.randomUUID(), id,
                "Admin", Position.COX, Gender.MALE, true, "TUDelft",
                availability, null));

        matches.add(match1);

        when(mockActivityRepository.findAll()).thenReturn(activityList);
        when(mockMatchRepository.findAllByActivityId(id)).thenReturn(matches);
        when(mockMatchRepository.existsByActivityId(id)).thenReturn(true);

        activityService = new ActivityService(mockActivityRepository, mockAuthenticationManager, mockMatchRepository);
        activityService.getActivities();

        verify(mockActivityRepository).delete(activity);
        verify(mockMatchRepository).deleteAll(mockMatchRepository.findAllByActivityId(id));
    }

    @Test
    public void checkStartFalse() throws ParseException {
        String dateString = "26-09-1043 14:05:05";
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        Date passedDate = formatter.parse(dateString);

        assertThrows(IllegalArgumentException.class, () -> {
            ActivityService.checkNewStart(passedDate);
        });
    }

    @Test
    public void checkStartTrue() throws ParseException {
        String dateString = "26-09-3043 14:05:05";
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        Date futureDate = formatter.parse(dateString);

        assertTrue(ActivityService.checkNewStart(futureDate));
    }

    @Test
    public void checkStartCurrentFalse() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        assertThrows(IllegalArgumentException.class, () -> {
            ActivityService.checkNewStart(currentDate);
        });
    }
}
