/*
 * This source file was generated by the Gradle 'init' task
 */
package org.example;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.IOException;

public class App {

    public static LHConfig getConfig() {
        return new LHConfig();
    }

    private static void registerWorkflow() throws IOException {
        LHConfig config = getConfig();

        TransferWorkflow transferWorkflow = new TransferWorkflow(config);

        FetchAccount fetchAccount = new FetchAccount();
        InitiateTransfer initiateTransfer = new InitiateTransfer();
        CheckTransfer checkTransfer = new CheckTransfer();

        LHTaskWorker fetchWorker = new LHTaskWorker(fetchAccount, "fetch-account", config);
        LHTaskWorker initiateWorker = new LHTaskWorker(initiateTransfer, "initiate-transfer", config);
        LHTaskWorker checkWorker = new LHTaskWorker(checkTransfer, "check-transfer", config);
        // config.getBlockingStub().putTaskDef("user-tasks-form");
        config.getBlockingStub()
                .putUserTaskDef(
                        new UserTaskSchema(new TransferWorkflow.UserTasksForm(), TransferWorkflow.USER_TASKS_FORM)
                                .compile());
        // TransferWorkflow transferWorkflow = new TransferWorkflow(config);
        fetchWorker.close();
        initiateWorker.close();
        checkWorker.close();

        fetchWorker.registerTaskDef();
        initiateWorker.registerTaskDef();
        checkWorker.registerTaskDef();
        // greetWorker.registerTaskDef();

        try {
            transferWorkflow.registerWorkflow();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Registered tasks and WfSpec");
    }

    private static void fetchAccount() throws IOException {

        FetchAccount fetchAccount = new FetchAccount();
        LHTaskWorker fetchAccountWorker = new LHTaskWorker(fetchAccount, "fetch-account", getConfig());

        // Close the worker upon shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(fetchAccountWorker::close));

        System.out.println("Starting fetch-account task worker!");
        fetchAccountWorker.start();
    }

    private static void initiateTransfer() throws IOException {
        InitiateTransfer initiateTransfer = new InitiateTransfer();
        LHTaskWorker initiateTransferWorker = new LHTaskWorker(initiateTransfer, "initiate-transfer", getConfig());

        // close the worker upon shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(initiateTransferWorker::close));

        System.out.println("Starting initiate-transfer worker!");
        initiateTransferWorker.start();
    }

    private static void checkTransfer() throws IOException {
        CheckTransfer checkTransfer = new CheckTransfer();
        LHTaskWorker checkTransferWorker = new LHTaskWorker(checkTransfer, "check-transfer", getConfig());

        Runtime.getRuntime().addShutdownHook(new Thread(checkTransferWorker::close));

        System.out.println("Starting check-transfer worker!");
        checkTransferWorker.start();
    }

    //    private static void checkExchangeRate() throws IOException {
    //        CheckExchangeRate checkExchangeRate = new CheckExchangeRate();
    //        LHTaskWorker checkExchangeRateWorker = new LHTaskWorker(checkExchangeRate, "check-exchange-rate",
    // getConfig());
    //
    //        Runtime.getRuntime().addShutdownHook(new Thread(checkExchangeRateWorker::close));
    //
    //        System.out.println("Starting check-exchange-rate worker!");
    //        checkExchangeRateWorker.start();
    //    }

    public static void main(String[] args) throws IOException {

        if (args.length != 1
                || (!args[0].equals("register")
                        && !args[0].equals("fetch-account")
                        && !args[0].equals("initiate-transfer")
                        && !args[0].equals("check-transfer"))) {
            System.err.println(
                    "Please provide one argument: register, fetch-account, intiate-transfer, check-transfer");
            System.exit(1);
        }

        if (args[0].equals("register")) {
            System.out.println("Registering workflow");
            registerWorkflow();
            System.exit(0);
        }
        if (args[0].equals("fetch-account")) {
            System.out.println("Starting fetch-account worker");
            fetchAccount();
        }
        if (args[0].equals("initiate-transfer")) {
            System.out.println("Starting initiate-transfer worker");
            initiateTransfer();
        }
        if (args[0].equals("check-transfer")) {
            System.out.println("Starting check-transfer worker");
            checkTransfer();
        } else {

            System.out.printf("Unknown command.  try: register, fetch-account, intiate-transfer, check-transfer");
        }
    }
}
