package rowing.notification.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import rowing.commons.NotificationStatus;
import rowing.commons.models.NotificationRequestModel;
import rowing.notification.authentication.AuthManager;
import rowing.notification.domain.notification.NotifyUserService;

import javax.naming.ConfigurationException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class DefaultControllerTest {
    NotifyUserService notifyUserService;
    NotificationRequestModel badRequestModel;
    NotificationRequestModel goodRequestModel;
    DefaultController defaultController;
    AuthManager authManager;

    @BeforeEach
    public void prep() throws ConfigurationException {
        badRequestModel = new NotificationRequestModel();
        goodRequestModel = new NotificationRequestModel("someusr", NotificationStatus.ACCEPTED, new UUID(1, 1));
        notifyUserService = mock(NotifyUserService.class);
        doThrow(IllegalArgumentException.class).when(notifyUserService).notifyUser(badRequestModel);
        authManager = mock(AuthManager.class);
        defaultController = new DefaultController(authManager);
        defaultController.setNotifyUserService(notifyUserService);
    }

    @Test
    void notifyUserException() {
        assertThrows(ResponseStatusException.class, () -> {
            defaultController.notifyUser(badRequestModel);
        });
    }

    @Test
    void notifyUser() {

        assertEquals(ResponseEntity.ok().build(), defaultController.notifyUser(goodRequestModel));
    }
}