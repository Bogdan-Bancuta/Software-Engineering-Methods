package rowing.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class KafkaTopicConfigTest {

    @Test
    void exampleTopic() {
        KafkaTopicConfig topicConfig = new KafkaTopicConfig();
        assertNotNull(topicConfig);

        ReflectionTestUtils.setField(topicConfig, "topicName",
                "notification");

        NewTopic newTopic = topicConfig.exampleTopic();
        assertNotNull(newTopic);
        assertEquals("notification", newTopic.name());
    }
}