package io.littlehorse.examples.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.littlehorse.examples.exception.ExceptionDetails;
import io.littlehorse.sdk.common.proto.OutputTopicRecord;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@ApplicationScoped
@Startup
public class WorkflowExcecutionStream {

    public static String OUTPUT_TOPIC_NAME = "coupon-granted-topic";

    private KafkaStreams streams;
    @Inject
    ObjectMapper mapper;

    @PostConstruct
    public void start() throws InterruptedException {
        // Wait for the output topic creation
        Thread.sleep(5000);
        System.out.println("✅ Starting Kafka Streams app...");
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "coupon-stream-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, OutputTopicRecord> stream = builder.stream(
                "my-cluster_default_execution",
                Consumed.with(
                        Serdes.String(),
                        new OutputTopicRecordSerde()
                )
        );

        stream
                .filter((key, value) -> value.getTaskRun().getStatus() == TaskStatus.TASK_EXCEPTION)
                .filter(
                        (key, value) -> {
                            if (value.getTaskRun().getAttemptsCount() <= 0) return false;
                            var name = value.getTaskRun().getAttempts(value.getTaskRun().getAttemptsCount() - 1).getException().getName();
                            return name.equals("out-of-stock");
                        }
                )
                .flatMap((key, value) -> {
                    List<KeyValue<String, Integer>> results = new ArrayList<>();
                    try {
                        var lastAttempt = value.getTaskRun().getAttempts(value.getTaskRun().getAttemptsCount() - 1);
                        String exceptionJson = lastAttempt.getException().getMessage();

                        ExceptionDetails details = mapper.readValue(exceptionJson, ExceptionDetails.class);
                        int clientId = details.clientId;
                        for (ExceptionDetails.Product product : details.products) {
                            String composedKey = clientId + ":" + product.productId + ":" + product.name;
                            System.out.println(composedKey);
                            results.add(KeyValue.pair(composedKey, 1));
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to parse exception JSON: " + e.getMessage());
                        e.printStackTrace();
                    }
                    return results;
                })
                .groupByKey()
                .count(Materialized.with(Serdes.String(), Serdes.Long()))
                .filter((key, count) -> count == 3)
                .toStream()// you get discount after 3 failures only once
                .peek((key, count) -> {
                    if (count != null) {
                        System.out.println("key:" + key + " count:" + count);
                    }
                }).to(OUTPUT_TOPIC_NAME);

        streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }

    @PreDestroy
    public void stop() {
        System.out.println("🛑 Shutting down Kafka Streams app...");
        if (streams != null) {
            streams.close();
        }
    }
}
