package io.littlehorse.examples.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.littlehorse.examples.exception.ExceptionDetails;
import io.littlehorse.examples.services.CouponService;
import io.littlehorse.sdk.common.proto.OutputTopicRecord;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

@ApplicationScoped
@Startup
@Slf4j
public class WorkflowExcecutionStream {

    public static String OUTPUT_TOPIC_NAME = "coupon-granted-topic";

    private KafkaStreams streams;
    private KafkaConsumer<String, Long> consumer;
    private Thread consumerThread;
    private volatile boolean running = true;
    private final CountDownLatch topicReadyLatch = new CountDownLatch(1);
    
    @Inject
    ObjectMapper mapper;
    
    @Inject
    CouponService couponService;

    @PostConstruct
    public void start() {
        log.info("Starting WorkflowExecution services...");
        
        // Step 1: Create the topic first
        try {
            createTopicIfNotExists(OUTPUT_TOPIC_NAME);
            topicReadyLatch.countDown();
            startStreamsProcessor();
            startConsumer();
            
            log.info("All stream services started successfully");
        } catch (Exception e) {
            log.error("Error starting WorkflowExecution services", e);
            throw new RuntimeException("Failed to start streaming services", e);
        }
    }
    
    private void startStreamsProcessor() {
        log.info("Starting Kafka Streams processor...");
        try {
            Properties props = new Properties();
            props.put(StreamsConfig.APPLICATION_ID_CONFIG, "coupon-stream-app");
            props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
            props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());
            props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000); // 10 seconds
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0); // For development
            
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
                                results.add(KeyValue.pair(composedKey, 1));
                            }
                        } catch (Exception e) {
                            log.error("Failed to parse exception JSON", e);
                        }
                        return results;
                    })
                    .groupByKey()
                    .count(Materialized.with(Serdes.String(), Serdes.Long()))
                    .filter((key, count) -> count == 3)
                    .toStream() // you get discount after 3 failures only once
                    .peek((key, count) -> {
                        if (count != null) {
                            log.info("key:{} count:{}", key, count);
                        }
                    }).to(OUTPUT_TOPIC_NAME);

            streams = new KafkaStreams(builder.build(), props);
            streams.start();
            log.info("Kafka Streams processor started successfully");
        } catch (Exception e) {
            log.error("Error starting Kafka Streams processor", e);
            throw new RuntimeException("Failed to start Kafka Streams processor", e);
        }
    }
    
    private void startConsumer() {
        log.info("Starting Consumer for topic: {}", OUTPUT_TOPIC_NAME);
        try {
            // Wait for topic to be ready - should already be ready but just in case
            if (!topicReadyLatch.await(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout waiting for topic to be ready");
            }
            
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "promo-app");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Serdes.Long().deserializer().getClass());
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(List.of(OUTPUT_TOPIC_NAME));

            consumerThread = new Thread(() -> {
                Instant lastRecordTime = Instant.now();

                while (running) {
                    try {
                        ConsumerRecords<String, Long> records = consumer.poll(Duration.ofMillis(500));

                        if (!records.isEmpty()) {
                            lastRecordTime = Instant.now();
                            for (ConsumerRecord<String, Long> record : records) {
                                log.debug("Received record: key={}", record.key());
                                try {
                                    String[] parts = record.key().split(":");
                                    long clientId = Long.parseLong(parts[0]);
                                    long productId = Long.parseLong(parts[1]);
                                    String productName = parts[2];
                                    couponService.runGenerateCouponWorkflow(clientId, productId, productName);
                                } catch (Exception e) {
                                    log.error("Failed to process record key: {}", record.key(), e);
                                }
                            }
                        } else {
                            if (Duration.between(lastRecordTime, Instant.now()).getSeconds() >= 60) {
                                log.warn("No records received on topic '{}' in the last 60 seconds.", OUTPUT_TOPIC_NAME);
                                lastRecordTime = Instant.now(); // Reset so it only logs once every 60s
                            }
                        }
                    } catch (Exception e) {
                        if (!running) {
                            break;
                        }
                        log.error("Error in consumer thread", e);
                    }
                }
            }, "coupon-consumer");

            consumerThread.start();
            log.info("Consumer started successfully");
        } catch (Exception e) {
            log.error("Error starting consumer", e);
            throw new RuntimeException("Failed to start consumer", e);
        }
    }

    @PreDestroy
    public void stop() {
        log.info("Shutting down WorkflowExecution services...");
        
        // Stop consumer first
        running = false;
        if (consumer != null) {
            log.info("Shutting down consumer...");
            consumer.wakeup();
            try {
                if (consumerThread != null) {
                    consumerThread.join(5000); // Wait up to 5 seconds
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                consumer.close();
            } catch (Exception e) {
                log.error("Error closing consumer", e);
            }
        }
        
        // Then stop streams
        if (streams != null) {
            log.info("Shutting down Kafka Streams application...");
            try {
                streams.close();
            } catch (Exception e) {
                log.error("Error closing Kafka Streams", e);
            }
        }
        
        log.info("All stream services shut down successfully");
    }
    
    private void createTopicIfNotExists(String topicName) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        
        try (AdminClient adminClient = AdminClient.create(props)) {
            // Check if topic exists
            ListTopicsResult topicsResult = adminClient.listTopics();
            Set<String> topicNames = topicsResult.names().get();
            
            if (!topicNames.contains(topicName)) {
                System.out.println("Creating topic: " + topicName);
                log.info("Creating topic: {}", topicName);
                NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
                adminClient.createTopics(Collections.singleton(newTopic)).all().get();
                System.out.println("Topic created: " + topicName);
                log.info("Topic created: {}", topicName);
            } else {
                System.out.println("Topic already exists: " + topicName);
                log.info("Topic already exists: {}", topicName);
            }
        } catch (Exception e) {
            System.err.println("Error while creating topic " + topicName + ": " + e.getMessage());
            e.printStackTrace();
            log.error("Error while creating topic: {}", topicName, e);
        }
    }
}
