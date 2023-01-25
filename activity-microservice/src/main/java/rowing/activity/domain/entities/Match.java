package rowing.activity.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import rowing.activity.domain.utils.BaseEntity;
import rowing.commons.Gender;
import rowing.commons.NotificationStatus;
import rowing.commons.Position;
import rowing.commons.entities.MatchingDTO;

import javax.persistence.*;
import java.time.Duration;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "matching")
public class Match<T extends MatchingDTO> extends BaseEntity<T> {
    @Id
    private UUID id;

    @Column(name = "activityId", nullable = false)
    private UUID activityId;

    @Column(name = "userId", nullable = false, unique = true)
    private String userId;

    @Column(name = "position", nullable = false)
    private Position position;

    /**
     * Constructor from a dto.
     *
     * @param dto that gives the information for this entity
     */
    public Match(MatchingDTO dto) {
        ModelMapper mapper = new ModelMapper();
        mapper.addConverter(
                context -> Duration.ofMillis(context.getSource()),
                Integer.class, Duration.class);
        mapper.getConfiguration().setSkipNullEnabled(true);
        if (dto.getId() == null) {
            // This id will change once the matching entity is saved, but it must be non-null
            this.id = UUID.randomUUID();
        } else {
            this.id = dto.getId();
        }

        this.activityId = dto.getActivityId();
        this.userId = dto.getUserId();
        this.position = dto.getPosition();
    }

    /**
     * Returns the DTO of this match.
     *
     * @return the DTO of this match
     */
    public T getDto() {
        return (T) new MatchingDTO(this.toDto());
    }

    /**
     * Convertor to match dto.
     *
     * @return the dto from this match entity
     */
    private MatchingDTO toDto() {
        return new MatchingDTO(
                this.id,
                this.activityId,
                this.userId,
                this.position,
                Gender.OTHERS,
                false,
                "",
                null,
                NotificationStatus.ACCEPTED
        );
    }
}
