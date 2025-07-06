package io.littlehorse.quickstart;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.worker.LHTaskWorker;

public class Main {

    public static void main(String[] args) {
        // Load a LHConfig from the environment variables
        LHConfig config = new LHConfig();
        LittleHorseBlockingStub client = config.getBlockingStub();
        OrderTasks ourTaskLogicObject = new OrderTasks();

        // First, we register the UserTaskDef
        UserTaskSchema schema = new UserTaskSchema(new ApprovalForm(), "approve-it-rental");
        PutUserTaskDefRequest userTask = schema.compile();
        client.putUserTaskDef(userTask); // native GRPC request.

        // Next, we register the TaskDef's
        LHTaskWorker shipItemWorker = new LHTaskWorker(ourTaskLogicObject, "ship-item", config);
        LHTaskWorker notApprovedWorker = new LHTaskWorker(ourTaskLogicObject, "decline-order", config);
        shipItemWorker.registerTaskDef();
        notApprovedWorker.registerTaskDef();

        // Next, register the `WfSpec`
        new ITOrderWorkflow().getWorkflow().registerWfSpec(client);

        // Finally, we start the task workers.
        Runtime.getRuntime().addShutdownHook(new Thread(shipItemWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(notApprovedWorker::close));
        shipItemWorker.start();
        notApprovedWorker.start();
    }
}
