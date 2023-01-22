package rowing.notification.domain.notification;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import rowing.commons.NotificationStatus;

import java.util.HashMap;
import java.util.Map;

@Data
@Service
public class EmailService {
    @Autowired
    private transient JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String emailAddress;

    @Autowired
    NotifyUserService notifyUserService;

    /**
     * Sends a notification as an email to the given address.
     *
     * @param notification notification to send
     */
    public void sendEmail(Notification notification) {
        if (emailAddress == null
                || notification == null || notification.getDestinationEmail() == null
                || notifyUserService.retrieveBody(notification) == null
                || notifyUserService.retrieveSubject(notification) == null
                || !notification.getDestinationEmail().contains("@")) {
            throw new IllegalArgumentException();
        }
        SimpleMailMessage newMessage = new SimpleMailMessage();
        newMessage.setFrom(emailAddress);
        newMessage.setTo(notification.getDestinationEmail());
        newMessage.setText(notifyUserService.retrieveBody(notification));
        newMessage.setSubject(notifyUserService.retrieveSubject(notification));
        mailSender.send(newMessage);
        System.out.println("The email was sent successfully.");
    }
}
