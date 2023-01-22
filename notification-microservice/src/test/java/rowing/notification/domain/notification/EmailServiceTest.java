package rowing.notification.domain.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    Notification notification;
    EmailService emailService;
    JavaMailSender mailSender;

    NotifyUserService notifyUserService;

    @BeforeEach
    void setup() {
        emailService = new EmailService();
        mailSender = mock(JavaMailSender.class);
        emailService.setMailSender(mailSender);
        emailService.setEmailAddress("me@gmail.com");


        // mocking
        notification = mock(Notification.class);
        when(notification.getDestinationEmail()).thenReturn("someEmail@gmail.com");

        notifyUserService = mock(NotifyUserService.class);
        when(notifyUserService.retrieveBody(any(Notification.class))).thenReturn("body");
        when(notifyUserService.retrieveSubject(any(Notification.class))).thenReturn("subj");
        emailService.setNotifyUserService(notifyUserService);
    }

    @Test
    void sendEmail() {
        emailService.sendEmail(notification);
    }

    @Test
    void exception1() {
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendEmail(null);
        });
    }

    @Test
    void exception2() {
        emailService.setEmailAddress(null);
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendEmail(notification);
        });
    }

    @Test
    void exception3() {
        when(notification.getDestinationEmail()).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendEmail(null);
        });

        when(notification.getDestinationEmail()).thenReturn("someEmailgmail.com");
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendEmail(null);
        });
    }

    @Test
    void exception4() {
        when(notifyUserService.retrieveBody(any(Notification.class))).thenReturn(null);
        emailService.setNotifyUserService(notifyUserService);
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendEmail(notification);
        });
    }

    @Test
    void exception5() {
        when(notifyUserService.retrieveSubject(any(Notification.class))).thenReturn(null);
        emailService.setNotifyUserService(notifyUserService);
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendEmail(notification);
        });
    }
}