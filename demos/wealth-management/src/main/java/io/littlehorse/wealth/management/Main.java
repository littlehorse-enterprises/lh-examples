package io.littlehorse.wealth.management;

import io.littlehorse.sdk.common.config.LHConfig;

public class Main {

    public static void main(String[] args) {
        LHConfig config = new LHConfig();

        PortfolioTaskWorkers workers = new PortfolioTaskWorkers(config);
        Runtime.getRuntime().addShutdownHook(new Thread(workers::close));
        workers.registerAllWorkersAndExternalEvents();

        PortfolioWorkflow workflow = new PortfolioWorkflow(config);
        workflow.registerWorkflow();

        System.out.println("Starting task workers");
        workers.startWorkers();

        RESTController server = new RESTController(config);
        server.start();
    }
}
