package io.littlehorse;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.littlehorse.BasicExample.GREET_TASK;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    @LHTaskMethod(GREET_TASK)
    public String greeting(String name) {
        log.debug("Executing task greet");
        return "Hello there! " + name;
    }

}
