package io.littlehorse.examples.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.littlehorse.examples.model.ExceptionDetails;
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
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@ApplicationScoped
@Startup
public class CouponStream {

    private KafkaStreams streams;
    @Inject
    ObjectMapper mapper;

    @PostConstruct
    public void start() {
        System.out.println("✅ Starting Kafka Streams app...");
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "cupon-stream-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());

        StoreBuilder<KeyValueStore<String, Boolean>> rewardedKeysStoreBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore("rewarded-keys-store"),
                        Serdes.String(),
                        Serdes.Boolean()
                );
        StreamsBuilder builder = new StreamsBuilder();
        builder.addStateStore(rewardedKeysStoreBuilder);

        KStream<String, OutputTopicRecord> stream = builder.stream(
                "my-cluster_default_execution",
                Consumed.with(
                        Serdes.String(),
                        new OutputTopicRecordSerde()
                )
        );

        stream
                .filter((key, value) -> value.getTaskRun().getStatus() == TaskStatus.TASK_EXCEPTION)
                .flatMap((key, value) -> {
                    List<KeyValue<String, Integer>> results = new ArrayList<>();
                    try {
                        var lastAttempt = value.getTaskRun().getAttempts(value.getTaskRun().getAttemptsCount() - 1);
                        String exceptionJson = lastAttempt.getException().getMessage();

                        ExceptionDetails details = mapper.readValue(exceptionJson, ExceptionDetails.class);
                        int clientId = details.clientId;

                        if (details.products != null) {
                            for (ExceptionDetails.Product product : details.products) {
                                if (product.availableStock != null && product.requestedQuantity != null &&
                                        product.requestedQuantity > product.availableStock) {
                                    String composedKey = clientId + ":" + product.productId;
                                    System.out.println(composedKey);
                                    results.add(KeyValue.pair(composedKey, 1));
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Failed to parse exception JSON: " + e.getMessage());
                        e.printStackTrace();
                    }
                    return results;
                })
                .groupByKey()
                .count(Materialized.with(Serdes.String(), Serdes.Long()))
                .toStream()
                .filter((key, count) -> count >= 3) // you get discount after 3 failures only once
                .foreach((key, count) -> {
                    System.out.println("key:" + key + " count:" + count);
                    if (count == 3) {
                        System.out.printf("Cuppont granted to Client/Product [%s] failed 3 times — total count: %d%n", key, count);
                    }
                });

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
