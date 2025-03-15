package io.littlehorse.e2e;

import io.littlehorse.container.LittleHorseCluster;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.test.internal.TestBootstrapper;

public class StandaloneBootstrapper implements TestBootstrapper {

    private final LittleHorseCluster littleHorseCluster = LittleHorseCluster.newBuilder()
            .withInstances(2)
            .withKafkaImage("apache/kafka-native:3.8.0")
            .withLittlehorseImage("ghcr.io/littlehorse-enterprises/littlehorse/lh-server:0.12.2")
            .build();

    public StandaloneBootstrapper() {
        littleHorseCluster.start();
    }

    @Override
    public LHConfig getWorkerConfig() {
        return new LHConfig(littleHorseCluster.getClientProperties());
    }
}
