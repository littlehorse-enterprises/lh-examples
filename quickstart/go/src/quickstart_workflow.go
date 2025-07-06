package main

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const (
	WORKFLOW_NAME                     = "quickstart"
	IDENTITY_VERIFIED_EVENT           = "identity-verified"
	VERIFY_IDENTITY_TASK              = "verify-identity"
	NOTIFY_CUSTOMER_VERIFIED_TASK     = "notify-customer-verified"
	NOTIFY_CUSTOMER_NOT_VERIFIED_TASK = "notify-customer-not-verified"
)

func QuickstartWorkflow(wf *littlehorse.WorkflowThread) {
	// Declare the input variables for the workflow.
	//
	// Using .Searchable() allows us to search for WfRun's based on the value of
	// these variables, and .Required() makes it required to pass the variable
	// as input.
	fullName := wf.DeclareStr("full-name").Searchable().Required()
	email := wf.DeclareStr("email").Searchable().Required()

	// Social Security Numbers are sensitive, so we mask the variable.
	ssn := wf.DeclareInt("ssn").MaskedValue().Required()

	// Internal variable representing whether the customer's identity has been verified.
	identityVerified := wf.DeclareBool("identity-verified").Searchable()

	// Call the verify-identity task and retry it up to 3 times if it fails
	wf.Execute(VERIFY_IDENTITY_TASK, fullName, email, ssn).WithRetries(3)

	// Make the WfRun wait until the event is posted or if the timeout is reached
	identityVerificationResult := wf.WaitForEvent(
		IDENTITY_VERIFIED_EVENT,
	).Timeout(
		60 * 60 * 24 * 3, // 5-minute timeout
	).SetCorrelationId(
		// Using a correlation id allows you to post the callback/ `ExternalEvent`
		// without knowing the WfRun ID that is waiting for it. In this case,
		// we correlate based on the email address of the customer.
		email,
	)

	exceptionName := littlehorse.Timeout
	wf.HandleError(identityVerificationResult, &exceptionName, func(handler *littlehorse.WorkflowThread) {
		handler.Execute(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, fullName, email)
		message := "Unable to verify customer identity in time."

		// Make the handler fail, so the WfRun stops here.
		handler.Fail(nil, "customer-not-verified", &message)
	})

	// Assign the output of the ExternalEvent to the `identityVerified` variable.
	identityVerified.Assign(identityVerificationResult)

	// Notify the customer if their identity was verified or not
	wf.DoIfElse(
		identityVerified.IsEqualTo(true),
		func(ifBody *littlehorse.WorkflowThread) {
			ifBody.Execute(NOTIFY_CUSTOMER_VERIFIED_TASK, fullName, email)
			// If you want, you could add more tasks or workflow steps in here
			// using the `ifBody` just like we use `wf` above.
			//
			// Also note that you could pass in a function pointer rather than
			// a lambda if you want to better organize the code.

		},
		func(elseBody *littlehorse.WorkflowThread) {
			elseBody.Execute(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, fullName, email)
		},
	)
}
