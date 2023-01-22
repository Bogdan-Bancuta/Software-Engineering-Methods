package rowing.notification.domain.notification.strategy;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StrategyFactoryTest {

    @Test
    void findStrategy() {
        Set<Strategy> strategySet = new HashSet<>();
        EmailStrategy emailStrategy = new EmailStrategy();
        strategySet.add(emailStrategy);
        KafkaStrategy kafkaStrategy = new KafkaStrategy();
        strategySet.add(kafkaStrategy);

        StrategyFactory strategyFactory = new StrategyFactory(strategySet);

        assertEquals(emailStrategy, strategyFactory.findStrategy(StrategyName.EMAIL));
        assertEquals(kafkaStrategy, strategyFactory.findStrategy(StrategyName.KAFKA));
    }
}