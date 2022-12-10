package rowing.user.domain.user;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import rowing.user.domain.HasEvents;

/**
 * A DDD entity representing an application user in our domain.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User extends HasEvents {
    /**
     * Identifier for the application user.
     */
    @Id
    @Column(name = "userId", nullable = false, unique = true)
    private String userId;

    @Column(name = "rowingPositions", nullable = true, unique = false)
    @Enumerated(EnumType.STRING)
    @ElementCollection
    private Set<Position> rowingPositions;

    @Column(name = "availability", nullable = true, unique = false)
    @Convert(converter = AvailabilityIntervalsAttributeConverter.class)
    private List<Set<AvailabilityIntervals>> availability;

    @Column(name = "email", nullable = false, unique = false)
    private String email;

    @Column(name = "firstName", nullable = false, unique = false)
    private String firstName;

    @Column(name = "lastName", nullable = false, unique = false)
    private String lastName;

    @Column(name = "coxCertificates", nullable = true, unique = false)
    @Enumerated(EnumType.STRING)
    @ElementCollection
    private Set<CoxCertificate> coxCertificates;

    @Column(name = "gender", nullable = false, unique = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "rowingOrganization", nullable = true, unique = false)
    private String rowingOrganization;

    @Column(name = "competitive", nullable = true, unique = false)
    private Boolean competitive;

    /**
     * Creates user with must fill attributes.
     *
     * @param userId - the unique identifier of the user
     * @param firstName - the first name of the user
     * @param lastName - the last name of the user
     * @param email - the email of the user to send notifications to
     */
    public User(String userId, String firstName, String lastName, String email) {
        //TODO validation if necessary
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        //this.recordThat(new UserWasCreatedEvent(username));
    }

    /**
     * Craete attributes for basic profile.
     *
     * @param rowingPositions - positions allowed to fill
     * @param availability - availability schedule
     * @param coxCertificates - certificates
     */
    public void createProfileBasic(Set<Position> rowingPositions, List<Set<AvailabilityIntervals>> availability,
                                   Set<CoxCertificate> coxCertificates) {
        //TODO validation if necessary
        this.rowingPositions = rowingPositions;
        this.availability = availability;
        this.coxCertificates = coxCertificates;
    }

    /**
     * Create attributes for competition for a user.
     *
     * @param gender - gender of user
     * @param rowingOrganization - organization of user
     * @param competitive - if he wants to participate competitively
     */
    public void createProfileCompetitive(Gender gender, String rowingOrganization, boolean competitive) {
        //TODO validation if necessary
        this.gender = gender;
        this.rowingOrganization = rowingOrganization;
        this.competitive = competitive;
    }

    /**
     * Equality is only based on the identifier.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User appUser = (User) o;
        return userId == (appUser.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
