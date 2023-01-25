package rowing.activity.domain.entities;

import org.junit.jupiter.api.Test;
import rowing.commons.entities.MatchingDTO;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MatchTest {

    @Test
    public void nullIDTest() {
        MatchingDTO dto = new MatchingDTO(null, UUID.randomUUID(), "efe",
                null, null, null, null, null, null);
        Match match = new Match(dto);

        assertNotNull(match.getId());
    }
}
