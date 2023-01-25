package rowing.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import rowing.commons.AvailabilityIntervals;
import rowing.commons.Certificates;
import rowing.commons.Gender;
import rowing.commons.entities.UpdateUserDTO;
import rowing.commons.entities.UserDTO;
import rowing.user.authentication.AuthManager;
import rowing.user.authentication.JwtTokenVerifier;
import rowing.user.domain.user.User;
import rowing.user.domain.user.UserRepository;
import rowing.user.models.AvailabilityModel;
import rowing.user.models.TwoAvailabilitiesModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenVerifier mockJwtTokenVerifier;

    @Autowired
    private transient AuthManager mockAuthenticationManager;

    @MockBean
    private transient UserRepository mockUserRepository;

    @Test
    public void testAddAvailability() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(
                Optional.of(new User("bogdan", "bogdan", "bogdan", "bogdan@gmail.com")));
        //when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArguments());
        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ObjectMapper objectMapper = new ObjectMapper().registerModules(new Jdk8Module(), new JavaTimeModule());
        RequestBuilder request = MockMvcRequestBuilders
                    .post("/user/add-availability")
                    .header("Authorization", "Bearer MockedToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            new AvailabilityModel("monday", "10:00", "11:00")))
                    .contentType(MediaType.APPLICATION_JSON);

        ResultActions result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isOk());

        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    public void testAddAvailabilityException() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(
                Optional.of(new User("bogdan", "bogdan", "bogdan", "bogdan@gmail.com")));
        //when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArguments());
        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ObjectMapper objectMapper = new ObjectMapper().registerModules(new Jdk8Module(), new JavaTimeModule());
        RequestBuilder request = MockMvcRequestBuilders
                .post("/user/add-availability")
                .header("Authorization", "Bearer MockedToken")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new AvailabilityModel("monday", "12:00", "11:00")))
                .contentType(MediaType.APPLICATION_JSON);

        ResultActions result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getErrorMessage();
        assertThat(response).isEqualTo("AVAILABILITY IS NOT IN THE CORRECT FORMAT");
    }

    @Test
    public void testRemoveAvailability() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        User u = new User("bogdan", "bogdan", "bogdan", "bogdan@gmail.com");
        u.setAvailability(new ArrayList<AvailabilityIntervals>(
                Arrays.asList(new AvailabilityIntervals("monday", "10:00", "11:00"))));
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.of(u));
        //when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArguments());
        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ObjectMapper objectMapper = new ObjectMapper().registerModules(new Jdk8Module(), new JavaTimeModule());
        RequestBuilder request = MockMvcRequestBuilders
                .post("/user/remove-availability")
                .header("Authorization", "Bearer MockedToken")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new AvailabilityModel("monday", "10:00", "11:00")))
                .contentType(MediaType.APPLICATION_JSON);

        ResultActions result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isOk());

        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    public void testRemoveAvailabilityException1() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        User u = new User("bogdan", "bogdan", "bogdan", "bogdan@gmail.com");
        u.setAvailability(new ArrayList<AvailabilityIntervals>(
                Arrays.asList(new AvailabilityIntervals("tuesday", "10:00", "11:00"))));
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.of(u));
        //when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArguments());
        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ObjectMapper objectMapper = new ObjectMapper().registerModules(new Jdk8Module(), new JavaTimeModule());
        System.out.println(objectMapper.writeValueAsString(u.toDTO()));
        RequestBuilder request = MockMvcRequestBuilders
                .post("/user/remove-availability")
                .header("Authorization", "Bearer MockedToken")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new AvailabilityModel("monday", "10:00", "11:00")))
                .contentType(MediaType.APPLICATION_JSON);

        ResultActions result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isNotFound());

        String response = result.andReturn().getResponse().getErrorMessage();
        assertThat(response).isEqualTo("AVAILABILITY NOT FOUND");
    }

    @Test
    public void testRemoveAvailabilityException2() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        User u = new User("bogdan", "bogdan", "bogdan", "bogdan@gmail.com");
        u.setAvailability(new ArrayList<AvailabilityIntervals>(
                Arrays.asList(new AvailabilityIntervals("monday", "10:00", "11:00"))));
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.of(u));
        //when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArguments());
        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ObjectMapper objectMapper = new ObjectMapper().registerModules(new Jdk8Module(), new JavaTimeModule());
        RequestBuilder request = MockMvcRequestBuilders
                .post("/user/remove-availability")
                .header("Authorization", "Bearer MockedToken")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new AvailabilityModel("monday", "12:00", "11:00")))
                .contentType(MediaType.APPLICATION_JSON);

        ResultActions result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getErrorMessage();
        assertThat(response).isEqualTo("AVAILABILITY IS NOT IN THE CORRECT FORMAT");
    }

    @Test
    public void testEditAvailability() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        User u = new User("bogdan", "bogdan", "bogdan", "bogdan@gmail.com");
        u.setAvailability(new ArrayList<AvailabilityIntervals>(
                Arrays.asList(new AvailabilityIntervals("tuesday", "10:00", "11:00"))));
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.of(u));
        //when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArguments());
        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ObjectMapper objectMapper = new ObjectMapper().registerModules(new Jdk8Module(), new JavaTimeModule());
        RequestBuilder request = MockMvcRequestBuilders
                .post("/user/edit-availability")
                .header("Authorization", "Bearer MockedToken")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TwoAvailabilitiesModel(
                        new AvailabilityModel("tuesday", "10:00", "11:00"),
                        new AvailabilityModel("monday", "12:00", "13:00"))))
                .contentType(MediaType.APPLICATION_JSON);
        ResultActions result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isOk());

        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    public void testEditAvailabilityException1() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        User u = new User("bogdan", "bogdan", "bogdan", "bogdan@gmail.com");
        u.setAvailability(new ArrayList<AvailabilityIntervals>(
                Arrays.asList(new AvailabilityIntervals("tuesday", "10:00", "11:00"))));
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.of(u));
        //when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArguments());
        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ObjectMapper objectMapper = new ObjectMapper().registerModules(new Jdk8Module(), new JavaTimeModule());
        RequestBuilder request = MockMvcRequestBuilders
                .post("/user/edit-availability")
                .header("Authorization", "Bearer MockedToken")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TwoAvailabilitiesModel(
                        new AvailabilityModel("wednesday", "10:00", "11:00"),
                        new AvailabilityModel("monday", "12:00", "13:00"))))
                .contentType(MediaType.APPLICATION_JSON);
        ResultActions result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isNotFound());

        String response = result.andReturn().getResponse().getErrorMessage();
        assertThat(response).isEqualTo("AVAILABILITY NOT FOUND OR CANNOT BE REPLACED");
    }

    @Test
    public void testEditAvailabilityException2() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        User u = new User("bogdan", "bogdan", "bogdan", "bogdan@gmail.com");
        u.setAvailability(new ArrayList<AvailabilityIntervals>(
                Arrays.asList(new AvailabilityIntervals("tuesday", "10:00", "11:00"))));
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.of(u));
        //when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArguments());
        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ObjectMapper objectMapper = new ObjectMapper().registerModules(new Jdk8Module(), new JavaTimeModule());
        RequestBuilder request = MockMvcRequestBuilders
                .post("/user/edit-availability")
                .header("Authorization", "Bearer MockedToken")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TwoAvailabilitiesModel(
                        new AvailabilityModel("tuesday", "10:00", "11:00"),
                        new AvailabilityModel("monday", "15:00", "13:00"))))
                .contentType(MediaType.APPLICATION_JSON);
        ResultActions result = mockMvc.perform(request);

        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getErrorMessage();
        assertThat(response).isEqualTo("AVAILABILITY IS NOT IN THE CORRECT FORMAT");
    }

    @Test
    public void getUser() throws Exception {

        User originalUser = new User("bogdan", "lala", "lala", "bogdan@gmail.com");
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.of(originalUser));

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ResultActions result = mockMvc.perform(get("/user/get-user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        UserDTO userDTO = originalUser.toDTO();
        // Assert
        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo(new ObjectMapper().writeValueAsString(userDTO));
    }


    @Test
    public void patchUserNoChanges() throws Exception {
        User originalUser = new User("bogdan", "lala", "lala", "bogdan@gmail.com");


        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.of(originalUser));

        UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, null, null, null,
                null, null, null, null, null);

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ResultActions result = mockMvc.perform(post("/user/update-user")
                .content(new ObjectMapper().writeValueAsString(updateUserDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        UserDTO userDTO = originalUser.toDTO();
        // Assert
        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo(new ObjectMapper().writeValueAsString(userDTO));
    }

    @Test
    public void patchUserChanges() throws Exception {
        User originalUser = new User("bogdan", "lala", "lala", "bogdan@gmail.com");
        UserDTO userDTO = originalUser.toDTO();

        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.of(originalUser));

        UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, null, "joyce@gmail.com",
                null, null, null, Gender.FEMALE, null, null);
        User shouldBeUpdatedToThisUser = new User("bogdan", "lala", "lala", "joyce@gmail.com");
        shouldBeUpdatedToThisUser.setGender(Gender.FEMALE);
        UserDTO shouldBeUpdatedToThisUserDTO = shouldBeUpdatedToThisUser.toDTO();

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ResultActions result = mockMvc.perform(post("/user/update-user")
                .content(new ObjectMapper().writeValueAsString(updateUserDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo(new ObjectMapper().writeValueAsString(shouldBeUpdatedToThisUserDTO));
    }

    @Test
    public void patchUserChangesException() throws Exception {
        User originalUser = new User("bogdan", "lala", "lala", "bogdan@gmail.com");
        UserDTO userDTO = originalUser.toDTO();

        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.of(originalUser));

        UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, null, "joyce@gmail.com",
                "a", null, null, Gender.FEMALE, null, null);

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ResultActions result = mockMvc.perform(post("/user/update-user")
                .content(new ObjectMapper().writeValueAsString(updateUserDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));


        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getErrorMessage();
        assertThat(response).isEqualTo("Input types incorrect.");
    }

    @Test
    public void getUserEmail() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan"))
                .thenReturn(Optional.of(new User("bogdan", "lala",
                        "lala", "bogdan@gmail.com")));
        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        String json = new JSONObject().put("username", "bogdan").toString();
        ResultActions result = mockMvc.perform(get("/user/get-email-address")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(json));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("bogdan@gmail.com");

    }

    @Test
    public void getUserEmailException() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.empty());
        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        String json = new JSONObject().put("username", "bogdan").toString();
        ResultActions result = mockMvc.perform(get("/user/get-email-address")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(json));

        // Assert
        result.andExpect(status().isNotFound());
        String response = result.andReturn().getResponse().getErrorMessage();

        assertThat(response).isEqualTo("User not found");

    }

    @Test
    public void getUserAvailability() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        User u = new User("activityOwner", "bogdan", "bogdan", "bogdan@gmail.com");
        ArrayList<AvailabilityIntervals> availability = new ArrayList<AvailabilityIntervals>(
                Arrays.asList(new AvailabilityIntervals("tuesday", "10:00", "11:00")));
        u.setAvailability(availability);
        when(mockAuthenticationManager.getUsername()).thenReturn("activity");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("activityOwner");
        when(mockUserRepository.findByUserId("activityOwner")).thenReturn(Optional.of(u));

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        //String json = new JSONObject().put("userId","activityOwner").toString();
        String json = "activityOwner";
        ResultActions result = mockMvc.perform(get("/user/get-availability-user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(json));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        objectMapper.setDateFormat(df);
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);

        assertThat(response).isEqualTo(objectMapper.writeValueAsString(availability));
    }

    @Test
    public void getUserAvailabilityException() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        User u = new User("activityOwner", "bogdan", "bogdan", "bogdan@gmail.com");
        ArrayList<AvailabilityIntervals> availability = new ArrayList<AvailabilityIntervals>(
                Arrays.asList(new AvailabilityIntervals("tuesday", "10:00", "11:00")));
        u.setAvailability(availability);
        when(mockAuthenticationManager.getUsername()).thenReturn("someUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("activityOwner");
        when(mockUserRepository.findByUserId("activityOwner")).thenReturn(Optional.of(u));

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        //String json = new JSONObject().put("userId","activityOwner").toString();
        String json = "activityOwner";
        ResultActions result = mockMvc.perform(get("/user/get-availability-user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(json));

        // Assert
        result.andExpect(status().isForbidden());
    }

    @Test
    public void getUserAvailabilityException2() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("activity");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("activityOwner");
        when(mockUserRepository.findByUserId("activityOwner")).thenReturn(Optional.empty());

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        //String json = new JSONObject().put("userId","activityOwner").toString();
        String json = "activityOwner";
        ResultActions result = mockMvc.perform(get("/user/get-availability-user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(json));

        // Assert
        result.andExpect(status().isNotFound());
    }

    @Test
    public void testAddCertificateToUser() throws Exception {
        User originalUser = new User("bogdan", "lala", "lala", "bogdan@gmail.com");

        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.of(originalUser));

        Certificates.initialize();

        UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, null, "joyce@gmail.com", null,
                null, Arrays.asList("C4"), null, null, null);
        User shouldBeUpdatedToThisUser = new User("bogdan", "lala", "lala", "joyce@gmail.com");
        shouldBeUpdatedToThisUser.setCoxCertificates(Arrays.asList("C4"));
        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ResultActions result = mockMvc.perform(post("/user/update-user")
                .content(new ObjectMapper().writeValueAsString(updateUserDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));


        // Assert
        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo(new ObjectMapper().writeValueAsString(shouldBeUpdatedToThisUser));
    }

    @Test
    public void testAddIncorrectCertificateToUser() throws Exception {
        User originalUser = new User("bogdan", "lala", "lala", "bogdan@gmail.com");

        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.of(originalUser));

        Certificates.initialize();

        UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, null, "joyce@gmail.com",
                null, null, Arrays.asList("C12"), null, null, null);
        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ResultActions result = mockMvc.perform(post("/user/update-user")
                .content(new ObjectMapper().writeValueAsString(updateUserDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));


        // Assert
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void patchUserChangesException2() throws Exception {
        User originalUser = new User("bogdan", "lala", "lala", "bogdan@gmail.com");
        UserDTO userDTO = originalUser.toDTO();

        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.empty());

        UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, null, "joyce@gmail.com",
                null, null, null, Gender.FEMALE, null, null);
        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ResultActions result = mockMvc.perform(post("/user/update-user")
                .content(new ObjectMapper().writeValueAsString(updateUserDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));


        // Assert
        result.andExpect(status().isBadRequest());
        String response = result.andReturn().getResponse().getErrorMessage();

        assertThat(response).isEqualTo("Input types incorrect.");
    }

    @Test
    public void getUserTest() throws Exception {
        User originalUser = new User("bogdan", "lala", "lala", "bogdan@gmail.com");
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");

        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.of(originalUser));

        ResultActions result = mockMvc.perform(get("/user/bogdan/get-user")
                .header("Authorization", "Bearer MockedToken").contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String response = result.andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals(mapper.writeValueAsString(originalUser.toDTO()), response, false);
    }

    @Test
    public void noUserTest() throws Exception {
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");


        ResultActions result = mockMvc.perform(get("/user/bogdan/get-user")
                .header("Authorization", "Bearer MockedToken").contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void patchUserChangesFirstNameNull() throws Exception {
        User originalUser = new User("bogdan", "lala", "lala", "bogdan@gmail.com");
        UserDTO userDTO = originalUser.toDTO();

        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.empty());

        UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, null, "joyce@gmail.com",
                null, "lala", null, Gender.FEMALE, null, null);

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ResultActions result = mockMvc.perform(post("/user/update-user")
                .content(new ObjectMapper().writeValueAsString(updateUserDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));


        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getErrorMessage();
        assertThat(response).isEqualTo("Input types incorrect.");
    }

    @Test
    public void patchUserChangesLastNameNull() throws Exception {
        User originalUser = new User("bogdan", "lala", "lala", "bogdan@gmail.com");
        UserDTO userDTO = originalUser.toDTO();

        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.empty());

        UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, null, "joyce@gmail.com",
                "lala", null, null, Gender.FEMALE, null, null);

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ResultActions result = mockMvc.perform(post("/user/update-user")
                .content(new ObjectMapper().writeValueAsString(updateUserDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));


        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getErrorMessage();
        assertThat(response).isEqualTo("Input types incorrect.");
    }

    @Test
    public void patchUserChangesEmailNull() throws Exception {
        User originalUser = new User("bogdan", "lala", "lala", "bogdan@gmail.com");
        UserDTO userDTO = originalUser.toDTO();

        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("bogdan");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUsernameFromToken(anyString())).thenReturn("bogdan");
        when(mockUserRepository.findByUserId("bogdan")).thenReturn(Optional.empty());

        UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, null, null,
                "lala", "lala", null, Gender.FEMALE, null, null);

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ResultActions result = mockMvc.perform(post("/user/update-user")
                .content(new ObjectMapper().writeValueAsString(updateUserDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));


        // Assert
        result.andExpect(status().isBadRequest());

        String response = result.andReturn().getResponse().getErrorMessage();
        assertThat(response).isEqualTo("Input types incorrect.");
    }
}
