package io.littlehorse.customer.support;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.littlehorse.customer.support.pojo.Note;
import io.littlehorse.customer.support.pojo.WorkflowExecutionRequest;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;

public class MyTasks {
	Random random = new Random();
	LittleHorseBlockingStub lhClient = new LHConfig().getBlockingStub();
	ChatLanguageModel model = OpenAiChatModel.builder()
			.baseUrl("http://langchain4j.dev/demo/openai/v1")
			.apiKey("demo")
			.modelName("gpt-4o-mini")
			.strictJsonSchema(true)
			.strictTools(true)
			.build();

	@LHTaskMethod(Constants.TRANSCRIBE_CALL_TASK)
	public String transcribeCall(String callId, String customerEmail) {
		ChatResponse response = model.chat(ChatRequest.builder()
				.messages(
						new SystemMessage(
								"Call ID: " + callId + "\n" + "Customer's Email: " + customerEmail
										+ "\n" + "Date and Time of the call: " + new Date().toString()
										+ "\n"
										+ "Make sure to add that metadata and any other metadata you see fit to the transcript so that AI can use that when analyzing the transcript."),
						new UserMessage(
								"Generate a example call transcript for a customer support call, with a customer who is angry and the agent who is trying to help them, make it 5 minutes long. Make sure to include the customer's name and the agent's name."))
				.build());
		return response.aiMessage().text();
	}

	// Tool specifications for customer support actions
	ToolSpecification updateCustomerRecordsSpec = ToolSpecification.builder()
			.name(Constants.ADD_NOTE_TO_CUSTOMER_WORKFLOW)
			.description("Update customer's profile with relevant information discussed during the call")
			.parameters(JsonObjectSchema.builder()
					.addStringProperty("customer-email", "The email of the customer whose record needs to be updated")
					.addStringProperty("note", "Notes about the issue, preferences, or follow-up details")
					.addStringProperty("agent-id", "ID of the agent who handled the call")
					.required("customer-email", "note")
					.build())
			.build();

	ToolSpecification escalateCaseSpec = ToolSpecification.builder()
			.name(Constants.ESCALATE_CASE_WORKFLOW)
			.description("Escalate the case to a higher-level support team or forward to appropriate department")
			.parameters(JsonObjectSchema.builder()
					.addStringProperty("case-id", "The ID of the case to escalate")
					.addStringProperty("customer-email", "Email address of the customer")
					.addStringProperty("reason", "Reason for escalation")
					.addStringProperty("department", "The department or team to escalate to")
					.addStringProperty("priority", "Priority level for escalation (low, medium, high)")
					.addStringProperty("agent-notes", "Additional notes from the agent")
					.required("case-id", "customer-email", "reason", "department")
					.build())
			.build();

	ToolSpecification sendFollowUpEmailSpec = ToolSpecification.builder()
			.name(Constants.SEND_EMAIL_WORKFLOW)
			.description("Send a follow-up email to confirm actions taken and provide additional information")
			.parameters(JsonObjectSchema.builder()
					.addStringProperty("email", "The email address of the customer")
					.addStringProperty("subject", "Subject line for the follow-up email")
					.addStringProperty("content", "Content of the follow-up email")
					.required("email", "subject", "content")
					.build())
			.build();

	@LHTaskMethod(Constants.DETERMINE_ACTIONS_TASK)
	public List<WorkflowExecutionRequest> determineActions(String transcript) {
		ChatRequest request = ChatRequest.builder()
				.toolSpecifications(updateCustomerRecordsSpec, escalateCaseSpec, sendFollowUpEmailSpec)
				.messages(
						new SystemMessage(
								"Analyze this customer support call transcript and determine what actions should be taken if any. If you decide there 1 or more actions to take, call the appropriate tool to execute the action."),
						new UserMessage(transcript))
				.build();

		ChatResponse response = model.chat(request);
		System.out.println(response.aiMessage());

		if (response.aiMessage().toolExecutionRequests() == null)
			return null;

		return response.aiMessage().toolExecutionRequests().stream()
				.map(item -> new WorkflowExecutionRequest(item.id(), item.name(), item.arguments()))
				.toList();
	}

	@LHTaskMethod(Constants.RUN_WORKFLOWS_TASK)
	public String runWorkflows(List<Map<String, Object>> requests, WorkerContext context) {
		if (context.getAttemptNumber() == 0) {
			throw new RuntimeException("Failed to run workflows");
		}

		StringBuilder sb = new StringBuilder();

		requests.forEach(request -> {
			Gson gson = new Gson();

			String wfSpecName = request.get("name").toString();
			RunWfRequest.Builder runWfRequestBuilder = RunWfRequest.newBuilder().setWfSpecName(wfSpecName);

			Map<String, Object> arguments = gson.fromJson(request.get("arguments").toString(), Map.class);
			arguments.forEach((key, value) -> {
				runWfRequestBuilder.putVariables(key, LHLibUtil.objToVarVal(value));
			});

			WfRun response = lhClient.runWf(runWfRequestBuilder.build());
			// context.log("Started `" + wfSpecName + "` " + response.getId());
			sb.append("Started `" + wfSpecName + "` " + response.getId() + "\n");
		});

		return sb.toString();
	}

	@LHTaskMethod(Constants.ADD_NOTE_TO_CUSTOMER_TASK)
	public void addNoteToCustomer(
			String customerEmail, String note, String agentId, WorkerContext context) {
		// In a real implementation, you would add the note to the customer's record in
		// your database

		// Simulate a database connection error
		if (context.getAttemptNumber() == 0) {
			String databaseUrl = "jdbc:sqlite:customer_support.db";
			throw new RuntimeException("Failed to connect to database: " + databaseUrl);
		}

		// Simulate a customer not found error
		if (context.getAttemptNumber() == 1) {
			throw new RuntimeException("Customer not found with ID: " + customerEmail);
		}
	}

	@LHTaskMethod(Constants.ESCALATE_CASE_TASK)
	public void escalateCase(
			String caseId,
			String customerEmail,
			String reason,
			String department,
			String priority,
			String agentNotes,
			WorkerContext context) {
		// In a real implementation, you would:
		// 1. Create a ticket in your support system
		// 2. Assign it to the appropriate team/department
		// 3. Set priority and include all relevant information

		// Simulate potential errors in escalation process
		if (context.getAttemptNumber() == 0) {
			throw new RuntimeException("Failed to connect to ticketing system");
		}
	}

	@LHTaskMethod(Constants.SEND_EMAIL_TASK)
	public void sendEmail(String email, String subject, String content, WorkerContext context) {
		// In a real implementation, you would connect to your email service here

		if (context.getAttemptNumber() == 0) {
			// Simulate an error sending the email
			throw new RuntimeException("Failed to send email");
		}
	}

	@LHTaskMethod(Constants.AUDIT_LOG_TASK)
	public void auditLog(String caseId, String activity, WorkerContext context) {
		// In a real implementation, you would:
		// 1. Connect to your audit logging system
		// 2. Record the activity with all relevant metadata
		// 3. Ensure compliance with any regulatory requirements

		// Simulate occasional logging failures
		if (context.getAttemptNumber() == 0) {
			throw new RuntimeException("Failed to write to audit log");
		}
	}

	@LHTaskMethod(Constants.ANALYZE_CUSTOMER_NOTE_TASK)
	public Note analyzeCustomerNote(String note) {
		ResponseFormat responseFormat = ResponseFormat.builder()
				.type(ResponseFormatType.JSON)
				.jsonSchema(JsonSchema.builder()
						.name("Note")
						.rootElement(JsonObjectSchema.builder()
								.addBooleanProperty("containsSensitiveInfo")
								.description("Boolean indicating if the note contains sensitive information")
								.addNumberProperty("followUpTimestamp")
								.description(
										"Suggested follow-up time if applicable (in unix timestamp format). This can be null if there is no follow-up information.")
								.addStringProperty("text").description("The text of the note")
								.required("containsSensitiveInfo", "text")
								.build())
						.build())
				.build();

		ChatResponse response = model.chat(ChatRequest.builder()
				.parameters(ChatRequestParameters.builder().responseFormat(responseFormat).build())
				.messages(new SystemMessage(
						"Analyze the following customer support note and extract structured information when and only when applicable. The unix timestamp now is "
								+ System.currentTimeMillis()
								+ "\nIf there is no follow-up information, set the followUpTimestamp to 0."),
						new UserMessage("Note: " + note))
				.build());

		System.out.println(response.aiMessage());

		try {
			return new ObjectMapper().readValue(response.aiMessage().text(), Note.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to parse note", e);
		}
	}

	@LHTaskMethod(Constants.REDACT_SENSITIVE_INFO_TASK)
	public String redactSensitiveInfo(String note) {

		ChatResponse redactedNote = model.chat(
				ChatRequest.builder().messages(new SystemMessage(
						"The following customer note may contain sensitive information (like credit card numbers, SSNs, passwords, etc.). Please redact any sensitive information and return the redacted note, If you need to redact data please replace it with [REDACTED]"),
						new UserMessage("Note: " + note)).build());

		return redactedNote.aiMessage().text();
	}

	@LHTaskMethod(Constants.SCHEDULE_FOLLOW_UP_TASK)
	public void scheduleFollowUp(String email, long followUpTimestamp, String followUpNote) {
		// In a real implementation, this would create a task in your CRM or
		// task management system for the follow-up

		// Simulate occasional scheduling failures
		if (random.nextInt(3) == 0) {
			throw new RuntimeException("Failed to schedule follow-up in task system");
		}
	}
}
