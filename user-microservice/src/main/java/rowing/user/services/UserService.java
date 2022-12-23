package rowing.user.services;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rowing.commons.entities.UpdateUserDTO;
import rowing.commons.entities.UserDTO;
import rowing.user.domain.user.User;
import rowing.user.domain.user.UserRepository;
import rowing.user.domain.user.utils.UserNotFoundException;

import java.util.Optional;

@Service
@NoArgsConstructor
public class UserService {
    @Autowired
    private UserRepository userRepository;

    /**
     * Initializes the user repository.
     *
     * @param userRepository - the jpa repository
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns a user based on its id.
     *
     * @param userId - the id of the user we are interested in.
     * @return user - the user object with the specified userId
     */
    public User findUserById(String userId)
            throws UserNotFoundException {
        Optional<User> optionalUser = userRepository.findByUserId(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return user;
        }
        throw new UserNotFoundException("userId");
    }

    /**
     * Returns a user based on its id.
     *
     * @param userId - the id of the user we are interested in.
     * @return user - the userDTO of the user object with the specified userId.
     */
    public UserDTO getUser(String userId)
            throws UserNotFoundException {
        User user = findUserById(userId);
        return user.toDTO();
    }

    /**
     * Updates the user with the specified userId.
     *
     * @param userId - the id of the user we are interested in.
     * @param updateUserDTO - the userDTO with the information which needs to be updated.
     * @return user - the userDTO of the object with the specified userId.
     */
    public UserDTO updateUser(String userId, UpdateUserDTO updateUserDTO)
            throws IllegalArgumentException {
        User user;
        try {
            user = findUserById(userId);
        } catch (UserNotFoundException e) {
            user = new User(userId);
        }

        Optional.ofNullable(updateUserDTO.getRowingPositions()).ifPresent(user::setRowingPositions);
        Optional.ofNullable(updateUserDTO.getAvailability()).ifPresent(user::setAvailability);
        Optional.ofNullable(updateUserDTO.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(updateUserDTO.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(updateUserDTO.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(updateUserDTO.getCoxCertificates()).ifPresent(user::setCoxCertificates);
        Optional.ofNullable(updateUserDTO.getGender()).ifPresent(user::setGender);
        Optional.ofNullable(updateUserDTO.getRowingOrganization()).ifPresent(user::setRowingOrganization);
        Optional.ofNullable(updateUserDTO.getCompetitive()).ifPresent(user::setCompetitive);

        UserDTO updatedUserDTO = user.toDTO();
        userRepository.save(user);

        return updatedUserDTO;
    }

    /**
     * Mehod for retrieving the user from the database with their username.
     *
     * @param userId of the user to get from the database
     * @return the userDTO object of the requested user
     */
    public UserDTO getUserSelected(String userId) {
        return userRepository.findByUserId(userId).get().toDTO();
    }
}
