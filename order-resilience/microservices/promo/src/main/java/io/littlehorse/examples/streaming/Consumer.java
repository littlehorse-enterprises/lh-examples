package io.littlehorse.examples.streaming;

import com.google.protobuf.Descriptors;
import io.littlehorse.sdk.common.proto.OutputTopicRecord;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@ApplicationScoped
//@Startup
public class Consumer {

    private KafkaConsumer<String, OutputTopicRecord> consumer;
    private Thread consumerThread;
    private volatile boolean running = true;
    String outputTopicName = "my-cluster_default_execution";

    @PostConstruct
    public void init() {
        System.out.println("Starting Kafka Consumer...");
        Properties props = getProperties();

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of(outputTopicName));

        consumerThread = new Thread(() -> {
            while (running) {
                ConsumerRecords<String, OutputTopicRecord> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, OutputTopicRecord> record : records) {
                    var status = record.value().getTaskRun().getStatus();

                    if (status == TaskStatus.TASK_EXCEPTION) {
                        record.value().getWfRun().getWfSpecId().getName();
                        var totalAttempts = record.value().getTaskRun().getAttemptsCount() - 1;
                        var exceptionName = record.value().getTaskRun().getAttempts(totalAttempts).getException().getName();
                        var exceptionMessage = record.value().getTaskRun().getAttempts(totalAttempts).getException().getMessage();
                        if( exceptionName.equals("out-of-stock")){
                        System.out.println(exceptionMessage);
                        }
                    }

                }
            }
        });
        consumerThread.start();
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OutputTopicRecordDeserializer.class);
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
