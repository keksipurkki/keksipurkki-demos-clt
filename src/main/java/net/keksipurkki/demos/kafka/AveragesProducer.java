package net.keksipurkki.demos.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.StreamPartitioner;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.DoubleStream;

@Slf4j
public class AveragesProducer {

    private final NewTopic samplesTopic;
    private final NewTopic averagesTopic;

    AveragesProducer(NewTopic samplesTopic, NewTopic averagesTopic) {
        this.samplesTopic = samplesTopic;
        this.averagesTopic = averagesTopic;
    }

    public KStream<UUID, Double> stream(StreamsBuilder builder) {
        var result = builder.<UUID, Double>stream(samplesTopic.name());

        final var levelDepth = averagesTopic.numPartitions();
        final var sampleSize = 2;

        log.info("Configuring a stream for sample averaging. Depth = {}, SampleSize = {}", levelDepth, sampleSize);

        // Averaging level = 0 == no averaging
        result.to(averagesTopic.name(), fixedPartition(0));

        for (var partition = 1; partition < levelDepth; partition++) {
            log.trace("Configuring stream topology for partition {}", partition);

            final int level = partition + 1;

            result = result
                .process(() -> new DoubleAggregator(sampleSize))
                .peek((_, v) -> log.debug("Level = {}. Got a sample of length {}", level, v.length))
                .mapValues((_, v) -> DoubleStream.of(v).average().orElseThrow())
                .peek((_, v) -> log.debug("Level = {}. Average = {}", level, v));

            result.to(averagesTopic.name(), fixedPartition(partition));
        }

        return result;
    }

    private Produced<UUID, Double> fixedPartition(int partition) {
        return Produced.streamPartitioner((_, _, _, _) -> partition);
    }

    private static class DoubleAggregator implements Processor<UUID, Double, UUID, double[]> {
        private final ArrayList<Double> values;
        private final int size;
        private ProcessorContext<UUID, double[]> context;

        DoubleAggregator(int size) {
            this.size = size;
            this.values = new ArrayList<>();
        }

        @Override
        public void init(ProcessorContext<UUID, double[]> context) {
            this.context = context;
        }

        @Override
        public void process(Record<UUID, Double> record) {
            if (values.size() >= size) {
                var array = values.stream().mapToDouble(Double::doubleValue).toArray();
                context.forward(record.withValue(array));
                values.clear();
            }
            values.add(record.value());
        }
    }

}
