package io.littlehorse.examples.streaming;

import io.littlehorse.examples.services.CouponService;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@ApplicationScoped
@Startup
public class CouponStream {

    @Inject
    CouponService couponService;

    private KafkaConsumer<String, Long> consumer;
    private Thread consumerThread;
    private volatile boolean running = true;
    String topicName = WorkflowExcecutionStream.OUTPUT_TOPIC_NAME;

    @PostConstruct
    public void init() {
        System.out.println("Starting Kafka Consumer...");
        Properties props = getProperties();

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of(topicName));

        consumerThread = new Thread(() -> {
            while (running) {
                ConsumerRecords<String, Long> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, Long> record : records) {
                    System.out.println("Key from coupun stream: " + record.key());
                    long clientId = Long.parseLong(record.key().split(":")[0]); // Example logic to derive clientId
                    long productId = Long.parseLong(record.key().split(":")[1]);
                    String productName = record.key().split(":")[2];
                    System.out.println("Creating coupon for clientId: " + clientId + ", productId: " + productId + ", productName: " + productName);
                    couponService.runGenerateCouponWorkflow(clientId,productId,productName);
                }
            }
        });
        consumerThread.start();
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "promo-app");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Serdes.Long().deserializer().getClass());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    @PreDestroy
    public void shutdown() {
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
