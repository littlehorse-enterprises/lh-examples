package io.littlehorse.examples.streaming;

import io.littlehorse.sdk.common.proto.OutputTopicRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class OutputTopicRecordSerde implements Serde<OutputTopicRecord> {

    @Override
    public Serializer<OutputTopicRecord> serializer() {
        return (topic, data) -> data == null ? null : data.toByteArray();
    }

    @Override
    public Deserializer<OutputTopicRecord> deserializer() {
        return (topic, data) -> {
            try {
                return OutputTopicRecord.parseFrom(data);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        };
    }
}
