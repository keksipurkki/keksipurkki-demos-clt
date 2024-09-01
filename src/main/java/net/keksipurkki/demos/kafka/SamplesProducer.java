package net.keksipurkki.demos.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class SamplesProducer {

    private final NewTopic samplesTopic;
    private final KafkaTemplate<UUID, Double> template;

    public SamplesProducer(NewTopic samplesTopic, KafkaTemplate<UUID, Double> template) {
        this.samplesTopic = samplesTopic;
        this.template = template;
    }

    public Runnable measurement() {
        // A simulated sensor device
        final var topic = samplesTopic.name();
        final Supplier<Double> sensor = ThreadLocalRandom.current()::nextDouble;

        return () -> {
            template.send(topic, UUID.randomUUID(), sensor.get());
        };
    }
}
