package rowing.notification.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;
import rowing.commons.NotificationStatus;
import rowing.commons.entities.utils.JsonUtil;
import rowing.commons.models.NotificationRequestModel;
import rowing.notification.authentication.AuthManager;
import rowing.notification.authentication.JwtTokenVerifier;

import java.util.Date;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
@AutoConfigureMockMvc
public class IntegrationWithMocksTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenVerifier mockJwtTokenVerifier;

    @Autowired
    private transient AuthManager mockAuthenticationManager;

    @Autowired
    RestTemplate restTemplate;

    @Value("${uri.users.url}")
    String uri;

    private MockRestServiceServer mockServer;

    @Value("${bearerToken}")
    String token;

    @BeforeEach
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void testSendNotificationEmail() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        NotificationRequestModel requestModel = new NotificationRequestModel("alex",
                NotificationStatus.ACCEPTED, new UUID(1, 1));

        //mocks the Users getEmailAddress endpoint
        mockServer.expect(requestTo(uri + ":8084/get-email-address"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(content().json("{\"username\": \"alex\"}"))
                .andRespond(withSuccess("halitgulamov@gmail.com", MediaType.TEXT_PLAIN));

        // calls REST API internally
        ResultActions result = mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        // verify if the request was made
        mockServer.verify();

        // Assert
        result.andExpect(status().isOk());
    }

    @Test
    public void testSendNotificationChanges() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        NotificationRequestModel requestModel = new NotificationRequestModel("alex",
                NotificationStatus.CHANGES, new UUID(1, 1), "Delft");

        //mocks the Users getEmailAddress endpoint
        mockServer.expect(requestTo(uri + ":8084/get-email-address"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(content().json("{\"username\": \"alex\"}"))
                .andRespond(withSuccess("halitgulamov@gmail.com", MediaType.TEXT_PLAIN));

        // calls REST API internally
        ResultActions result = mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        // verify if the request was made
        mockServer.verify();

        // Assert
        result.andExpect(status().isOk());
    }

    @Test
    public void testSendNotificationFull() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        NotificationRequestModel requestModel = new NotificationRequestModel("alex",
                NotificationStatus.ACTIVITY_FULL, new UUID(1, 1));

        //mocks the Users getEmailAddress endpoint
        mockServer.expect(requestTo(uri + ":8084/get-email-address"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(content().json("{\"username\": \"alex\"}"))
                .andRespond(withSuccess("halitgulamov@gmail.com", MediaType.TEXT_PLAIN));

        // calls REST API internally
        ResultActions result = mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        // verify if the request was made
        mockServer.verify();

        // Assert
        result.andExpect(status().isOk());
    }

    @Test
    public void testSendNotificationKafka() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        NotificationRequestModel requestModel = new NotificationRequestModel("alex",
                NotificationStatus.ACCEPTED, new UUID(1, 1));

        //mocks the Users getEmailAddress endpoint
        mockServer.expect(requestTo(uri + ":8084/get-email-address"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(content().json("{\"username\": \"alex\"}"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // calls REST API internally
        ResultActions result = mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        // verify if the request was made
        mockServer.verify();

        // Assert
        result.andExpect(status().isOk());
    }

    @Test
    public void testUnauthorizedNotification() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        NotificationRequestModel requestModel = new NotificationRequestModel("alex",
                NotificationStatus.ACCEPTED, new UUID(1, 1));

        // calls REST API internally
        ResultActions result = mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(requestModel)));

        // Assert
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void testUnauthorizedUsers() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        NotificationRequestModel requestModel = new NotificationRequestModel("alex",
                NotificationStatus.ACCEPTED, new UUID(1, 1));

        //mocks the Users getEmailAddress endpoint
        mockServer.expect(requestTo(uri + ":8084/get-email-address"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(content().json("{\"username\": \"alex\"}"))
                .andRespond(withUnauthorizedRequest());

        // calls REST API internally
        ResultActions result = mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        // verify if the request was made
        mockServer.verify();

        // Assert
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testBadRequestUsers() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        NotificationRequestModel requestModel = new NotificationRequestModel("alex",
                NotificationStatus.ACCEPTED, new UUID(1, 1));

        //mocks the Users getEmailAddress endpoint
        mockServer.expect(requestTo(uri + ":8084/get-email-address"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(content().json("{\"username\": \"alex\"}"))
                .andRespond(withBadRequest());

        // calls REST API internally
        ResultActions result = mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        // verify if the request was made
        mockServer.verify();

        // Assert
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testBadRequestNotification() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        NotificationRequestModel requestModel = new NotificationRequestModel();

        // calls REST API internally
        ResultActions result = mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        // Assert
        result.andExpect(status().isBadRequest());

        requestModel = new NotificationRequestModel("test", NotificationStatus.CHANGES, new UUID(1, 1));
        // calls REST API internally
        result = mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        // Assert
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testSendNotificationLocationOK() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        NotificationRequestModel requestModel = new NotificationRequestModel("alex",
                NotificationStatus.CHANGES, new UUID(1, 1), "delft");

        //mocks the Users getEmailAddress endpoint
        mockServer.expect(requestTo(uri + ":8084/get-email-address"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(content().json("{\"username\": \"alex\"}"))
                .andRespond(withSuccess("aojica65@gmail.com", MediaType.TEXT_PLAIN));

        // calls REST API internally
        ResultActions result = mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        // verify if the request was made
        mockServer.verify();

        // Assert
        result.andExpect(status().isOk());
    }

    @Test
    public void testSendNotificationDateOK() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        NotificationRequestModel requestModel = new NotificationRequestModel("alex",
                NotificationStatus.CHANGES, new UUID(1, 1), new Date());

        //mocks the Users getEmailAddress endpoint
        mockServer.expect(requestTo(uri + ":8084/get-email-address"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(content().json("{\"username\": \"alex\"}"))
                .andRespond(withSuccess("aojica65@gmail.com", MediaType.TEXT_PLAIN));

        // calls REST API internally
        ResultActions result = mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        // verify if the request was made
        mockServer.verify();

        // Assert
        result.andExpect(status().isOk());
    }

    @Test
    public void testSendNotificationDateKafka() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        NotificationRequestModel requestModel = new NotificationRequestModel("alex",
                NotificationStatus.CHANGES, new UUID(1, 1), new Date());

        //mocks the Users getEmailAddress endpoint
        mockServer.expect(requestTo(uri + ":8084/get-email-address"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(content().json("{\"username\": \"alex\"}"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // calls REST API internally
        ResultActions result = mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        // verify if the request was made
        mockServer.verify();

        // Assert
        result.andExpect(status().isOk());
    }

    @Test
    public void testSendNotificationLocationKafka() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getUsername()).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        NotificationRequestModel requestModel = new NotificationRequestModel("alex",
                NotificationStatus.CHANGES, new UUID(1, 1), "delft");

        //mocks the Users getEmailAddress endpoint
        mockServer.expect(requestTo(uri + ":8084/get-email-address"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(content().json("{\"username\": \"alex\"}"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // calls REST API internally
        ResultActions result = mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        // verify if the request was made
        mockServer.verify();

        // Assert
        result.andExpect(status().isOk());
    }

}
