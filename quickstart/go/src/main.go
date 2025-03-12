package main

import (
	"context"
	"fmt"
	"log"
	"os"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

var config = littlehorse.NewConfigFromEnv()

func main() {
	if len(os.Args) != 2 || (os.Args[1] != "register" && os.Args[1] != "workers") {
		fmt.Fprintln(os.Stderr, "Please provide one argument: either 'register' or 'workers'")
		os.Exit(1)
	}

	if os.Args[1] == "register" {
		registerMetadata()
	} else {
		startTaskWorkers()
	}
}

func registerMetadata() {
	client, err := config.GetGrpcClient()
	if err != nil {
		log.Fatal(err)
	}

	// Register the ExternalEventDef
	(*client).PutExternalEventDef(context.Background(),
		&lhproto.PutExternalEventDefRequest{
			Name: IDENTITY_VERIFIED_EVENT,
		},
	)

	// First, register the 3 TaskDefs
	log.Default().Print("Registering TaskDefs")
	verifyIdentityWorker, err := littlehorse.NewTaskWorker(config, VerifyIdentity, VERIFY_IDENTITY_TASK)
	if err != nil {
		log.Fatal("Failed to create verify-identity worker", err)
	}
	notifyCustomerVerifiedWorker, err2 := littlehorse.NewTaskWorker(config, NotifyCustomerVerified, NOTIFY_CUSTOMER_VERIFIED_TASK)
	if err2 != nil {
		log.Fatal("Failed to create notify-customer-verified worker", err2)
	}
	notifyCustomerNotVerifiedWorker, err3 := littlehorse.NewTaskWorker(config, NotifyCustomerNotVerified, NOTIFY_CUSTOMER_NOT_VERIFIED_TASK)
	if err3 != nil {
		log.Fatal("Failed to create notify-customer-not-verified worker", err3)
	}

	err = verifyIdentityWorker.RegisterTaskDef()
	if err != nil {
		log.Fatal("Failed to register verify-identity", err)
	}
	err = notifyCustomerVerifiedWorker.RegisterTaskDef()
	if err != nil {
		log.Fatal("Failed to register notify-customer-verified", err)
	}
	err = notifyCustomerNotVerifiedWorker.RegisterTaskDef()
	if err != nil {
		log.Fatal("Failed to register notify-customer-not-verified", err)
	}

	// Register the Workflow Spec
	log.Default().Print("Registering WfSpec")
	workflow := littlehorse.NewWorkflow(QuickstartWorkflow, WORKFLOW_NAME)
	putWf, err := workflow.Compile()
	if err != nil {
		log.Fatal(err)
	}

	resp, err := (*client).PutWfSpec(context.Background(), putWf)
	if err != nil {
		log.Fatal(err)
	}
	littlehorse.PrintProto(resp)
}

func startTaskWorkers() {
	verifyIdentityWorker, err1 := littlehorse.NewTaskWorker(config, VerifyIdentity, VERIFY_IDENTITY_TASK)
	notifyCustomerVerifiedWorker, err2 := littlehorse.NewTaskWorker(config, NotifyCustomerVerified, NOTIFY_CUSTOMER_VERIFIED_TASK)
	notifyCustomerNotVerifiedWorker, err3 := littlehorse.NewTaskWorker(config, NotifyCustomerNotVerified, NOTIFY_CUSTOMER_NOT_VERIFIED_TASK)

	if err1 != nil || err2 != nil || err3 != nil {
		log.Fatal(err1, err2, err3)
	}

	defer func() {
		log.Default().Print("Shutting down task worker")
		verifyIdentityWorker.Close()
		notifyCustomerVerifiedWorker.Close()
		notifyCustomerNotVerifiedWorker.Close()
	}()

	log.Default().Print("Starting Task Workers")
	go verifyIdentityWorker.Start()
	go notifyCustomerVerifiedWorker.Start()
	go notifyCustomerNotVerifiedWorker.Start()

	select {}
}
