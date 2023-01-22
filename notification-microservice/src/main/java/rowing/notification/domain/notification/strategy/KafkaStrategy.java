package rowing.notification.domain.notification.strategy;

import lombok.Data;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import rowing.notification.domain.notification.Notification;
import rowing.notification.domain.notification.NotifyUserService;

@Data
@Component
public class KafkaStrategy implements Strategy {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    NotifyUserService notifyUserService;

    @Value("${topicName}")
    String topicName;

    @Override
    public StrategyName getStrategyName() {
        return StrategyName.KAFKA;
    }

    @Override
    public void notifyUser(Notification notification) {
        String message = notifyUserService.retrieveSubject(notification)
                + "\n" + notifyUserService.retrieveBody(notification);
        JSONObject json = new JSONObject();
        json.put(notification.getUsername(), message);
        kafkaTemplate.send(topicName, json.toString());
        System.out.println(message);
    }
}
