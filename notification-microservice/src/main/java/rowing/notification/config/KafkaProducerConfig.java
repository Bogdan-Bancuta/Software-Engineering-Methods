package rowing.notification.config;

import lombok.Data;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
public class KafkaProducerConfig {

    @Value("${useAuthentication}")
    private boolean useAuthentication;

    private Map<String, String> kafkaToValues;

    /**
     * Initializes the configuration values for kafka in the map.
     *
     * @param bootstrapServers - url for servers
     * @param kafkaJaasConfig - configuration
     * @param kafkaSaslMechanism - mechanism
     * @param kafkaSecurityProtocol - security protocol
     * @param useAuthentication - if it uses authentication
     */
    @Autowired
    public void setKafkaToValues(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
                               @Value("${spring.kafka.properties.sasl.jaas-config}") String kafkaJaasConfig,
                               @Value("${spring.kafka.properties.sasl-mechanism}") String kafkaSaslMechanism,
                               @Value("${spring.kafka.properties.security-protocol}") String kafkaSecurityProtocol,
                                 @Value("${useAuthentication}") boolean useAuthentication) {
        this.kafkaToValues = new HashMap<>();
        kafkaToValues.put("servers", bootstrapServers);
        kafkaToValues.put("config", kafkaJaasConfig);
        kafkaToValues.put("mechanism", kafkaSaslMechanism);
        kafkaToValues.put("protocol", kafkaSecurityProtocol);
        this.useAuthentication = useAuthentication;
    }

    /**
     * Configures the kafka producer with values from application.properties

     * @return properties of the kafka producer
     */
    public Map<String, Object> producerConfig() {
        HashMap<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaToValues.get("servers"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        if (useAuthentication) {
            props.put("sasl.mechanism", kafkaToValues.get("mechanism"));
            props.put("sasl.jaas.config", kafkaToValues.get("config"));
            props.put("security.protocol", kafkaToValues.get("protocol"));
        }
        return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

}
