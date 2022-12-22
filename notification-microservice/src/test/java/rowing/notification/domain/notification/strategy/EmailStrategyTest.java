package rowing.notification.domain.notification.strategy;

import org.junit.jupiter.api.Test;
import rowing.notification.domain.notification.EmailService;
import rowing.notification.domain.notification.Notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static rowing.notification.domain.notification.strategy.StrategyName.EMAIL;

class EmailStrategyTest {

    EmailStrategy emailStrategy = new EmailStrategy();

    @Test
    void getStrategyName() {
        assertNotNull(emailStrategy);
        assertEquals(EMAIL, emailStrategy.getStrategyName());
    }

    @Test
    void notifyUser() {
        EmailService emailService = mock(EmailService.class);
        Notification notification = mock(Notification.class);
        emailStrategy.setEmailService(emailService);
        emailStrategy.notifyUser(notification);
    }
}