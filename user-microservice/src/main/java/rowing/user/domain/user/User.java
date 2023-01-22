package rowing.user.domain.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.dynamic.loading.InjectionClassLoader;
import org.hibernate.annotations.GeneratorType;
import rowing.commons.*;
import rowing.commons.entities.UpdateUserDTO;
import rowing.commons.entities.UserDTO;
import rowing.user.domain.HasEvents;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String userId;

    @Column(name = "rowingPositions")
    @Enumerated(EnumType.STRING)
    @ElementCollection
    private List<Position> rowingPositions;

    @Column(name = "availability")
    @Convert(converter = AvailabilityIntervalsAttributeConverter.class)
    private List<AvailabilityIntervals> availability = new ArrayList<>();

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "firstName", nullable = false)
    private String firstName;

    @Column(name = "lastName", nullable = false)
    private String lastName;

    @Column(name = "coxCertificates")
    @ElementCollection
    private List<String> coxCertificates;

    @Column(name = "gender", nullable = true, unique = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "rowingOrganization")
    private String rowingOrganization;

    @Column(name = "competitive")
    private Boolean competitive;

    /**
     * Creates a user.
     *
     * @param userId - the unique identifier of the user
     */
    public User(String userId) {
        this.userId = userId;
        //this.recordThat(new UserWasCreatedEvent(username));
    }

    /**
     * Creates user with must fill attributes.
     *
     * @param userId - the unique identifier of the user
     * @param firstName - the first name of the user
     * @param lastName - the last name of the user
     * @param email - the email of the user to send notifications to
     */
    public User(String userId, String firstName, String lastName, String email) {
        this.userId = userId;
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        //this.recordThat(new UserWasCreatedEvent(username));
    }

    /**
     * Setter with data validation for availability.
     *
     * @param availability - availability of the user.
     * @throws IllegalArgumentException - if validation fails
     */
    public void setAvailability(List<AvailabilityIntervals> availability) throws IllegalArgumentException {
        if (availability == null) {
            return;
        }
        this.availability = new ArrayList<>();
        for (AvailabilityIntervals a : availability) {
            this.availability.add(new AvailabilityIntervals(a.getDay().toString(),
                    a.getStartInterval().toString(), a.getEndInterval().toString()));
        }
    }

    /**
     * Setter with data validation for email.
     *
     * @param email - email of the user.
     * @throws IllegalArgumentException - if validation fails
     */
    public void setEmail(String email) throws IllegalArgumentException {
        if (email == null || email.length() <= 6 || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Email has not valid format");
        }
        this.email = email;
    }

    /**
     * Setter with data validation for first name.
     *
     * @param firstName - first name of the user.
     * @throws IllegalArgumentException - if validation fails
     */
    public void setFirstName(String firstName) throws IllegalArgumentException {
        if (firstName == null || firstName.length() <= 1 || !firstName.matches("^[a-zA-Z]*$")) {
            throw new IllegalArgumentException("First Name should be longer "
                    + "than 1 character and should contain only letters");
        }
        this.firstName = firstName;
    }

    /**
     * Setter with data validation for last name.
     *
     * @param lastName - last name of the user.
     * @throws IllegalArgumentException - if validation fails
     */
    public void setLastName(String lastName) throws IllegalArgumentException {
        if (lastName == null || lastName.length() <= 1 || !lastName.matches("^[a-zA-Z]*$")) {
            throw new IllegalArgumentException("Last Name should be longer "
                    + "than 1 character and should contain only letters");
        }
        this.lastName = lastName;
    }

    /**
     * Setter with data validation for cox certificates.
     *
     * @param coxCertificates - cox certificates of the user.
     * @throws IllegalArgumentException - if validation fails
     */
    public void setCoxCertificates(List<String> coxCertificates) {
        if (coxCertificates == null) {
            return;
        }
        for (String name : coxCertificates) {
            if (Certificates.existByName(name) == false) {
                throw new IllegalArgumentException("Certificates are not recognized");
            }
        }
        this.coxCertificates = coxCertificates;
    }

    /**
     * Setter with data validation for rowing organisation.
     *
     * @param rowingOrganization - rowing organisation of the user.
     * @throws IllegalArgumentException - if validation fails
     */
    public void setRowingOrganization(String rowingOrganization) {
        if (rowingOrganization == null) {
            return;
        }
        int limit = 2;
        if (rowingOrganization.length() <= limit) {
            throw new IllegalArgumentException("Rowing organization must be at least 3 characters long");
        }
        this.rowingOrganization = rowingOrganization;
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
        return userId.equals(appUser.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    /**
     * Convert user to UsetDTO.
     *
     * @return - userDTO
     */
    public UserDTO toDTO() {
        return new UserDTO(userId,
                rowingPositions,
                availability,
                email,
                firstName,
                lastName,
                coxCertificates,
                gender,
                rowingOrganization,
                competitive);
    }

    /**
     * Sets user's parameters based on UpdateUserDTO object.
     *
     * @param updateUserDTO - updateUserDTO objects parameters are extracted from.
     */
    public void setParams(UpdateUserDTO updateUserDTO) {
        Optional.ofNullable(updateUserDTO.getRowingPositions()).ifPresent(this::setRowingPositions);
        Optional.ofNullable(updateUserDTO.getAvailability()).ifPresent(this::setAvailability);
        Optional.ofNullable(updateUserDTO.getEmail()).ifPresent(this::setEmail);
        Optional.ofNullable(updateUserDTO.getFirstName()).ifPresent(this::setFirstName);
        Optional.ofNullable(updateUserDTO.getLastName()).ifPresent(this::setLastName);
        Optional.ofNullable(updateUserDTO.getCoxCertificates()).ifPresent(this::setCoxCertificates);
        Optional.ofNullable(updateUserDTO.getGender()).ifPresent(this::setGender);
        Optional.ofNullable(updateUserDTO.getRowingOrganization()).ifPresent(this::setRowingOrganization);
        Optional.ofNullable(updateUserDTO.getCompetitive()).ifPresent(this::setCompetitive);
    }
}
