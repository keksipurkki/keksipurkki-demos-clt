package net.keksipurkki.demos.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableConfigurationProperties(Topics.TopicProperties.class)
public class Topics {

    private final TopicProperties props;

    public Topics(TopicProperties props) {
        this.props = props;
    }

    @Bean
    public NewTopic samplesTopic() {
        return TopicBuilder.name(props.samples())
            .partitions(1)
            .compact()
            .build();
    }

    @Bean
    public NewTopic averagesTopic() {
        return TopicBuilder.name(props.averages())
            .partitions(5)
            .compact()
            .build();
    }

    @ConfigurationProperties(prefix = "app.topics")
    public record TopicProperties(String samples, String averages) {

    }

}
