package lh.demo.fraud.detection.api.workflow;

import io.littlehorse.sdk.usertask.annotations.UserTaskField;

public class FraudTicketForm {

    public static final String FRAUD_TICKET_FORM_NAME = "fraud-ticket";

    @UserTaskField(displayName = "Approved?", description = "Check the box if this is not a fraudulent transaction")
    public boolean isApproved;

    @UserTaskField(
            displayName = "Comments",
            description = "Additional information that explains the approval or rejection",
            required = false)
    public String comments;
}
