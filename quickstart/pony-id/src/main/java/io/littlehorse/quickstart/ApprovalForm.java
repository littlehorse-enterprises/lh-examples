package io.littlehorse.quickstart;

import io.littlehorse.sdk.usertask.annotations.UserTaskField;

public class ApprovalForm {
    // Define the fields that will be used in the user task form
    @UserTaskField(displayName = "Approve IT rental?", required = true)
    public boolean isApproved;

    @UserTaskField(displayName = "Comments", required = false)
    public String comments;
}
