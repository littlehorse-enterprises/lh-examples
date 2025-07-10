package io.littlehorse.examples.streaming;

import io.littlehorse.examples.services.CouponService;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

@ApplicationScoped
@Startup
@Slf4j
public class CouponStream {

    @Inject
    CouponService couponService;

    private KafkaConsumer<String, Long> consumer;
    private Thread consumerThread;
    private volatile boolean running = true;
    private final String topicName = WorkflowExcecutionStream.OUTPUT_TOPIC_NAME;

    @PostConstruct
    public void init() {
        waitForTopicWithRetry(topicName, Duration.ofSeconds(3600), Duration.ofSeconds(2));

        log.info("Starting CouponStream consumer for topic: {}", topicName);
        Properties props = getProperties();

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of(topicName));

        consumerThread = new Thread(() -> {
            Instant lastRecordTime = Instant.now();

            while (running) {
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
                        log.warn("No records received on topic '{}' in the last 60 seconds.", topicName);
                        lastRecordTime = Instant.now(); // Reset so it only logs once every 60s
                    }
                }
            }
        }, "coupon-consumer");

        consumerThread.start();
    }

    private void waitForTopicWithRetry(String topic, Duration timeout, Duration interval) {
        Properties props = getProperties();
        long start = System.currentTimeMillis();

        try (AdminClient adminClient = KafkaAdminClient.create(props)) {
            while ((System.currentTimeMillis() - start) < timeout.toMillis()) {
                try {
                    DescribeTopicsResult result = adminClient.describeTopics(List.of(topic));
                    result.all().get();
                    log.info("Topic '{}' is available", topic);
                    return;
                } catch (Exception e) {
                    log.warn("Topic '{}' not yet available, retrying...", topic);
                    Thread.sleep(interval.toMillis());
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error checking topic existence: " + topic, ex);
        }

        throw new RuntimeException("Timeout while waiting for topic '" + topic + "' to be created");
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "promo-app");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Serdes.Long().deserializer().getClass());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down CouponStream consumer...");
        running = false;
        if (consumer != null) {
            consumer.wakeup();
            try {
                consumerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            consumer.close();
        }
    }
}
