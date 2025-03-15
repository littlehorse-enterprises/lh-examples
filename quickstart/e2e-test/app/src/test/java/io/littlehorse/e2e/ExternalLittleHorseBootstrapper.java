package io.littlehorse.e2e;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.test.internal.TestBootstrapper;

public class ExternalLittleHorseBootstrapper implements TestBootstrapper {

    @Override
    public LHConfig getWorkerConfig() {
        return new LHConfig();
    }
}
