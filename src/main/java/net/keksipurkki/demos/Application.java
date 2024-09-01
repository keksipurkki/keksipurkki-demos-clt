package net.keksipurkki.demos;

import lombok.extern.slf4j.Slf4j;
import net.keksipurkki.demos.kafka.SamplesProducer;
import net.keksipurkki.demos.ws.HistogramMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@Slf4j
public class Application {

    private final Map<Integer, Histogram> histograms = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate template;
    private final Runnable measurement;

    Application(SamplesProducer producer, SimpMessagingTemplate template) {
        this.measurement = producer.measurement();
        this.template = template;
    }

    public static void main(String... args) {
        var app = new SpringApplication(Application.class);
        app.run(args);
    }

    @Scheduled(fixedRateString = "${app.samples.rate.millis}")
    void measureSamples() {
        measurement.run();
    }

    @KafkaListener(topics = "${app.topics.averages}", groupId = "averages")
    void onReceiveAverage(ConsumerRecord<UUID, Double> average) {
        final var topic = "/topic/histograms";
        var level = average.partition();

        log.info("Received an average. AveragingLevel = {}, Value = {}", level, average.value());
        var histogram = histograms.computeIfAbsent(level, this::newHistogram);

        log.debug("Recording value to histogram {}", histogram.name());
        histogram.record(average.value());

        log.debug("Broadcast to {}", topic);
        template.convertAndSend(topic, new HistogramMessage(histograms.values()));
    }

    private Histogram newHistogram(int level) {
        return new Histogram(level, 10, 0.0d, 1.0d);
    }

}
