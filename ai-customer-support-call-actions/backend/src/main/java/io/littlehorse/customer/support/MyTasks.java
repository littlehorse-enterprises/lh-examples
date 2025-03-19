package io.littlehorse.customer.support;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.worker.LHTaskMethod;

public class MyTasks {
	LittleHorseBlockingStub lhClient = new LHConfig().getBlockingStub();
	ChatLanguageModel model = OpenAiChatModel.builder()
			.baseUrl("http://langchain4j.dev/demo/openai/v1")
			.apiKey("demo")
			.modelName("gpt-4o-mini")
			.build();

	@LHTaskMethod(LHConstants.TRANSCRIBE_CALL_TASK)
	public String transcribeCall() {
		String transcript = model.chat(
				"Generate a example call transcript for a customer support call, with a customer who is angry and the agent who is trying to help them, make it 5 minutes long. Make sure to include the customer's name and the agent's name.");
		return transcript;
	}

	// Tool specifications for customer support actions
	ToolSpecification updateCustomerRecordsSpec = ToolSpecification.builder()
			.name("update-customer-notes")
			.description("Update customer's profile with relevant information discussed during the call")
			.parameters(JsonObjectSchema.builder()
					.addStringProperty("customerId", "The ID of the customer whose record needs to be updated")
					.addStringProperty("notes", "Notes about the issue, preferences, or follow-up details")
					.required("customerId", "notes")
					.build())
			.build();

	ToolSpecification escalateCaseSpec = ToolSpecification.builder()
			.name("escalate-case")
			.description("Escalate the case to a higher-level support team or forward to appropriate department")
			.parameters(JsonObjectSchema.builder()
					.addStringProperty("reason", "Reason for escalation")
					.addStringProperty("department", "The department or team to escalate to")
					.required("reason", "department")
					.build())
			.build();

	ToolSpecification sendFollowUpEmailSpec = ToolSpecification.builder()
			.name("send-follow-up-email")
			.description("Send a follow-up email to confirm actions taken and provide additional information")
			.parameters(JsonObjectSchema.builder()
					.addStringProperty("customerEmail", "The email address of the customer")
					.addStringProperty("subject", "Subject line for the follow-up email")
					.addStringProperty("content", "Content of the follow-up email")
					.required("customerEmail", "subject", "content")
					.build())
			.build();

	@LHTaskMethod(LHConstants.DETERMINE_ACTIONS_TASK)
	public List<WorkflowExecutionRequest> determineActions(String transcript) {
		ChatRequest request = ChatRequest.builder()
				.messages(new SystemMessage(
						"Analyze this customer support call transcript and determine what actions should be taken if any. If you decide there 1 or more actions to take, call the appropriate tool to execute the action: \n\n"
								+ transcript))
				.toolSpecifications(updateCustomerRecordsSpec, escalateCaseSpec, sendFollowUpEmailSpec)
				.build();
		ChatResponse response = model.chat(request);
		System.out.println(response.aiMessage());

		if (response.aiMessage().toolExecutionRequests() == null)
			return null;

		return response.aiMessage().toolExecutionRequests().stream()
				.map(item -> new WorkflowExecutionRequest(item.id(), item.name(), item.arguments())).toList();
	}

	@LHTaskMethod(LHConstants.RUN_WORKFLOWS_TASK)
	public void runWorkflows(List<Map<String, Object>> requests) {
		requests.forEach(request -> {
			Gson gson = new Gson();

			RunWfRequest.Builder runWfRequestBuilder = RunWfRequest.newBuilder()
					.setWfSpecName(request.get("name").toString());

			Map<String, Object> arguments = gson.fromJson(request.get("arguments").toString(), Map.class);
			arguments.forEach((key, value) -> {
				runWfRequestBuilder.putVariables(key, LHLibUtil.objToVarVal(value));
			});

			System.out.println(runWfRequestBuilder.build());
			lhClient.runWf(runWfRequestBuilder.build());
		});
	}
}
