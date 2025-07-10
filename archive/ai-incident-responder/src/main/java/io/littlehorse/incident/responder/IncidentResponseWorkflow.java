package io.littlehorse.incident.responder;

import static io.littlehorse.incident.responder.LHConstants.ERROR_DIAGNOSIS_FAILED;
import static io.littlehorse.incident.responder.LHConstants.ERROR_VALIDATION_FAILED;
import static io.littlehorse.incident.responder.LHConstants.EVENT_ENGINEER_RESPONSE;
import static io.littlehorse.incident.responder.LHConstants.STATUS_ESCALATED;
import static io.littlehorse.incident.responder.LHConstants.STATUS_FALSE_ALARM;
import static io.littlehorse.incident.responder.LHConstants.STATUS_FIXED;
import static io.littlehorse.incident.responder.LHConstants.STATUS_MANUAL_INTERVENTION;
import static io.littlehorse.incident.responder.LHConstants.TASK_ATTEMPT_REMEDIATION;
import static io.littlehorse.incident.responder.LHConstants.TASK_DIAGNOSE_INCIDENT;
import static io.littlehorse.incident.responder.LHConstants.TASK_ESCALATE_TO_ENGINEER;
import static io.littlehorse.incident.responder.LHConstants.TASK_NOTIFY_STATUS;
import static io.littlehorse.incident.responder.LHConstants.TASK_SEND_SLACK_ALERT;
import static io.littlehorse.incident.responder.LHConstants.TASK_VALIDATE_INCIDENT;
import static io.littlehorse.incident.responder.LHConstants.WORKFLOW_NAME;

import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class IncidentResponseWorkflow {
	/**
	 * This method defines the incident response workflow logic
	 */
	public void incidentResponseWf(WorkflowThread wf) {
		// Declare input variables
		WfRunVariable alertId = wf.declareStr("alert-id").searchable().required();
		WfRunVariable systemName = wf.declareStr("system-name").searchable().required();
		WfRunVariable alertType = wf.declareStr("alert-type").searchable().required();
		WfRunVariable severity = wf.declareStr("severity").searchable().required();

		// Declare internal variables
		WfRunVariable diagnosticInfo = wf.declareJsonObj("diagnostic-info");
		WfRunVariable isRealIncident = wf.declareBool("is-real-incident").searchable();
		WfRunVariable remediationSuccess = wf.declareBool("remediation-success");
		WfRunVariable remediationAction = wf.declareStr("remediation-action");
		WfRunVariable engineerResponse = wf.declareBool("engineer-response");
		WfRunVariable resolutionStatus = wf.declareStr("resolution-status");
		WfRunVariable engineerId = wf.declareStr("engineer-id").searchable();

		// Step 1: Diagnose incident using logs and metrics - with retries
		NodeOutput diagnosisResult = wf.execute(TASK_DIAGNOSE_INCIDENT, alertId, systemName, alertType, severity)
				.withRetries(5); // Retry up to 5 times if diagnosis fails

		// Handle diagnosis failure if it still fails after retries
		wf.handleError(diagnosisResult, LHErrorType.TASK_FAILURE, handler -> {
			handler.execute(TASK_NOTIFY_STATUS, alertId, systemName, "DIAGNOSIS_FAILED", "");
			handler.fail(ERROR_DIAGNOSIS_FAILED, "Failed to diagnose incident after retries");
		});

		// Store the diagnostic information
		diagnosticInfo.assign(diagnosisResult);

		// Step 2: Validate if it's a real incident or false alarm
		NodeOutput validationResult = wf.execute(TASK_VALIDATE_INCIDENT, diagnosticInfo, alertType).withRetries(3);

		// Handle validation failure
		wf.handleError(validationResult, LHErrorType.TASK_FAILURE, handler -> {
			handler.execute(TASK_NOTIFY_STATUS, alertId, systemName, "VALIDATION_FAILED", "");
			handler.fail(ERROR_VALIDATION_FAILED, "Incident validation service failed");
		});

		// Store validation result
		isRealIncident.assign(validationResult);

		// Step 3: Branch based on validation
		wf.doIfElse(
				isRealIncident.isEqualTo(true),
				realIncidentHandler -> {
					// It's a real incident - attempt automated remediation
					NodeOutput remediationResult = realIncidentHandler
							.execute(TASK_ATTEMPT_REMEDIATION, diagnosticInfo, systemName, alertType)
							.withRetries(3);

					// Store remediation info
					remediationSuccess.assign(remediationResult.jsonPath("$.success"));
					remediationAction.assign(remediationResult.jsonPath("$.action"));

					// Branch based on remediation success
					realIncidentHandler.doIfElse(
							remediationSuccess.isEqualTo(true),
							successHandler -> {
								// Remediation was successful - notify and complete
								resolutionStatus.assign(STATUS_FIXED);
								successHandler
										.execute(
												TASK_NOTIFY_STATUS,
												alertId,
												systemName,
												resolutionStatus,
												remediationAction)
										.withRetries(3);
							},
							failureHandler -> {
								// Remediation failed - escalate to engineer
								resolutionStatus.assign(STATUS_ESCALATED);

								// Assign to appropriate engineer based on alert type and system
								NodeOutput escalationResult = failureHandler
										.execute(
												TASK_ESCALATE_TO_ENGINEER,
												alertId,
												systemName,
												diagnosticInfo,
												alertType,
												severity)
										.withRetries(3);

								// Store assigned engineer ID
								engineerId.assign(escalationResult);

								// Send Slack alert to the engineer
								failureHandler
										.execute(
												TASK_SEND_SLACK_ALERT,
												engineerId,
												alertId,
												systemName,
												diagnosticInfo,
												remediationAction)
										.withRetries(3);

								// Wait for the engineer response event
								NodeOutput responseEvent = failureHandler.waitForEvent(EVENT_ENGINEER_RESPONSE);
								engineerResponse.assign(responseEvent);

								// Update final resolution status
								resolutionStatus.assign(STATUS_MANUAL_INTERVENTION);
								failureHandler
										.execute(
												TASK_NOTIFY_STATUS,
												alertId,
												systemName,
												resolutionStatus,
												"Engineer intervention required")
										.withRetries(3);
							});
				},
				falseAlarmHandler -> {
					// It's a false alarm - log and notify
					resolutionStatus.assign(STATUS_FALSE_ALARM);
					falseAlarmHandler.execute(TASK_NOTIFY_STATUS, alertId, systemName, resolutionStatus, "")
							.withRetries(3);
				});
	}

	/**
	 * Returns a LittleHorse Workflow wrapper object that can be used to register
	 * the WfSpec.
	 */
	public Workflow getWorkflow() {
		return Workflow.newWorkflow(WORKFLOW_NAME, this::incidentResponseWf);
	}
}
