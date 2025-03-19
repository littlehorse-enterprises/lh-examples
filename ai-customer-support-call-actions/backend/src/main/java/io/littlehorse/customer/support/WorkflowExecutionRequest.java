package io.littlehorse.customer.support;

public class WorkflowExecutionRequest {
	private String id;
	private String name;
	private String arguments;

	public WorkflowExecutionRequest(String id, String name, String arguments) {
		this.id = id;
		this.name = name;
		this.arguments = arguments;
	}

	public WorkflowExecutionRequest() {
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getArguments() {
		return arguments;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}
}