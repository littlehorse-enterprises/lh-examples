package lh.demo.it.request.wf;

import io.littlehorse.sdk.usertask.annotations.UserTaskField;

/**
 * This class is used to generate the schema for the UserTaskDef that a Finance
 * department member uses to approve or reject an IT Request. It is a simple
 * form with only one field: a boolean `isApproved`.
 */
public class ApprovalForm {

    @UserTaskField(displayName = "Approved?", description = "Check the box if this is an acceptable purchase.")
    public boolean isApproved;

    @UserTaskField(
            displayName = "Comments",
            description = "Additional information that explains the approval or rejections",
            required = false)
    public String comments;
}
