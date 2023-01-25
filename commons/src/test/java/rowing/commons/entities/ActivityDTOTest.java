package rowing.commons.entities;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import rowing.commons.Position;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityDTOTest {
    Date date = new Date();
    ActivityDTO activityDTO = new ActivityDTO(
            new UUID(1, 1), "test", "name",
            "type", date, "here",
            Arrays.asList(new Position[]{Position.COX, Position.COACH}),
            Arrays.asList(new String[] {"applicant1"}), "someBoatType");

    @BeforeAll
    void setup(){

    }

    @Test
    void getId() {
        assertEquals(new UUID(1, 1), activityDTO.getId());
    }

    @Test
    void getOwner() {
        assertEquals("test",  activityDTO.getOwner());
    }

    @Test
    void getType() {
        assertEquals("type", activityDTO.getType());
    }

    @Test
    void getName() {
        assertEquals("name", activityDTO.getName());
    }

    @Test
    void getBoatType() {
        assertEquals("someBoatType", activityDTO.getBoatType());
    }

    @Test
    void getStart() {
        assertEquals(date, activityDTO.getStart());
    }

    @Test
    void getLocation() {
        assertEquals("here", activityDTO.getLocation());
    }

    @Test
    void getPositions() {
        assertEquals(Arrays.asList(new Position[]{Position.COX, Position.COACH}),
                activityDTO.getPositions());
    }

    @Test
    void getApplicants() {
        assertEquals(Arrays.asList(new String[] {"applicant1"}),
                activityDTO.getApplicants());
    }

    @Test
    void setId() {
        activityDTO.setId(new UUID(2, 2));
        assertEquals(new UUID(2, 2), activityDTO.getId());
    }

    @Test
    void setOwner() {
        activityDTO.setOwner("owner");
        assertEquals("owner", activityDTO.getOwner());
        activityDTO.setOwner("test");
    }

    @Test
    void setName() {
        activityDTO.setName("nameTest");
        assertEquals("nameTest", activityDTO.getName());
    }

    @Test
    void setType() {
        activityDTO.setType("type2");
        assertEquals("type2", activityDTO.getType());
    }

    @Test
    void setStart() {
        Date testDate = new Date();
        activityDTO.setStart(testDate);
        assertEquals(testDate, activityDTO.getStart());
        activityDTO.setStart(date);
    }

    @Test
    void setLocation() {
        activityDTO.setLocation("there");
        assertEquals("there", activityDTO.getLocation());
        activityDTO.setLocation("here");
    }

    @Test
    void setPositions() {
        activityDTO.setPositions(Arrays.asList(new Position[]{Position.COX}));
        assertEquals(Arrays.asList(new Position[]{Position.COX}), activityDTO.getPositions());
        activityDTO.setPositions(Arrays.asList(new Position[]{Position.COX, Position.COACH}));
    }

    @Test
    void setApplicants() {
        activityDTO.setApplicants(Arrays.asList(new String[]{"testApplicant"}));
        assertEquals(Arrays.asList(new String[]{"testApplicant"}), activityDTO.getApplicants());
    }

    @Test
    void setBoatType() {
        activityDTO.setBoatType("typeBoat");
        assertEquals("typeBoat", activityDTO.getBoatType());
        activityDTO.setBoatType("someBoatType");
    }

    @Test
    void testEquals() {
        ActivityDTO activityDTO2 = new ActivityDTO(activityDTO);
        assertTrue(activityDTO.equals(activityDTO2));
    }

    @Test
    void canEqual() {
        ActivityDTO activityDTO2 = new ActivityDTO(activityDTO);
        assertTrue(activityDTO.canEqual(activityDTO2));
    }

    @Test
    void testHashCode() {
        ActivityDTO activityDTO2 = new ActivityDTO(activityDTO);
        assertEquals(activityDTO.hashCode(), activityDTO2.hashCode());
    }
}