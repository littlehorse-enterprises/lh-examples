package main

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
)

func KycWorkflow(wf *wflib.WorkflowThread) {
	// Receive user details to execute task
	// User { firstname, lastname, email }
	user := wf.AddVariable("user", model.VariableType_JSON_OBJ).Required()
	passport := wf.AddVariable("passport", model.VariableType_JSON_OBJ)

	wf.Execute("notify-request-passport", user)
	passportUpload := wf.WaitForEvent("passport-submitted")
	wf.Mutate(passport, model.VariableMutationType_ASSIGN, passportUpload)

	// Execute automated passport verification
	passportResult := wf.Execute("process-passport", passport)
	valid := wf.AddVariable("valid", model.VariableType_BOOL)
	wf.Mutate(valid, model.VariableMutationType_ASSIGN, passportResult.JsonPath("$.valid"))

	// Run conditionally based on verification results
	wf.DoIfElse(
		wf.Condition(valid, model.Comparator_EQUALS, true),
		UserVerified(user),       // Verification succeeds - Sends notification
		ManualVerification(user), // Verification failed - Perform manual verification
	)
}

func UserVerified(user *wflib.WfRunVariable) func(t *wflib.WorkflowThread) {
	return func(t *wflib.WorkflowThread) {
		t.Execute("notify-user-verified", user)
		t.Execute("create-customer", user)
	}
}

func UserRejected(user *wflib.WfRunVariable) func(t *wflib.WorkflowThread) {
	return func(t *wflib.WorkflowThread) {
		t.Execute("notify-user-rejected", user)
	}
}

func ManualVerification(user *wflib.WfRunVariable) func(t *wflib.WorkflowThread) {
	return func(t *wflib.WorkflowThread) {
		approved := t.AddVariable("approved", model.VariableType_BOOL)
		firstname := user.JsonPath("$.firstname")
		lastname := user.JsonPath("$.lastname")
		t.Execute("notify-manual-verification", user)
		verification := t.AssignUserTask("manual-verification", nil, "support").WithNotes(t.Format("{0} {1}", &firstname, &lastname))
		t.Mutate(approved, model.VariableMutationType_ASSIGN, verification.Output.JsonPath("$.approved"))

		t.DoIfElse(
			t.Condition(approved, model.Comparator_EQUALS, true),
			UserVerified(user),
			UserRejected(user),
		)
	}
}
