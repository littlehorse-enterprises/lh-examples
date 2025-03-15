package io.littlehorse.e2e;

import io.littlehorse.BasicExample;
import io.littlehorse.MyWorker;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WithWorkers;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Test;

import static io.littlehorse.BasicExample.EXAMPLE_BASIC_WF;
import static io.littlehorse.BasicExample.INPUT_NAME_VARIABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@LHTest
public class BasicTest {

    @LHWorkflow(EXAMPLE_BASIC_WF)
    private Workflow basicWf;
    private WorkflowVerifier verifier;

    @Test
    @WithWorkers("myWorker")
    public void shouldSayHello() {
        verifier.prepareRun(basicWf, Arg.of(INPUT_NAME_VARIABLE, "Anakin Skywalker"))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRunResult(0, 1, variableValue -> assertEquals("Hello there! Anakin Skywalker", variableValue.getStr()))
                .start();
    }

    @LHWorkflow(EXAMPLE_BASIC_WF)
    public Workflow registerWf() {
        return BasicExample.getWorkflow();
    }

    public Object myWorker() {
        return new MyWorker();
    }

//    It is also possible to put a @LHTaskMethod in the test
//    @LHTaskMethod(GREET_TASK)
//    public String greeting(String name) {
//        return "Hello there! " + name;
//    }

}
