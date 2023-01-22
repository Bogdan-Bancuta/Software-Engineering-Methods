package rowing.notification.domain.notification.strategy;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import rowing.notification.domain.notification.Notification;
import rowing.notification.domain.notification.NotifyUserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static rowing.notification.domain.notification.strategy.StrategyName.KAFKA;

class KafkaStrategyTest {

    KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
    KafkaStrategy kafkaStrategy = spy(KafkaStrategy.class);

    NotifyUserService notifyUserService;

    @Test
    void getStrategyName() {
        assertNotNull(kafkaStrategy);
        assertEquals(KAFKA, kafkaStrategy.getStrategyName());
    }

    @Test
    void notifyUser() {
        Notification notification = mock(Notification.class);
        notifyUserService = mock(NotifyUserService.class);
        when(notifyUserService.retrieveBody(any(Notification.class))).thenReturn("body");
        when(notifyUserService.retrieveSubject(any(Notification.class))).thenReturn("subj");
        kafkaStrategy.setNotifyUserService(notifyUserService);
        when(notification.getUsername()).thenReturn("test");
        kafkaStrategy.setKafkaTemplate(kafkaTemplate);
        kafkaStrategy.notifyUser(notification);
    }
}