# LittleHorse AI Customer Support Call Actions

This example demonstrates how LittleHorse can be used with LLMs as an AI Agent to handle reliable, observable and predictable action taking after processing a customer support call.

## Running the example

Export your OpenAI API key as an environment variable:

```bash
export OPENAI_API_KEY=sk-proj-XXX
```

Then run the example:

```bash
./gradlew run
```

This will register the metadata needed, start the task workers, and execute a `WfRun` for the `CustomerSupportWorkflow`.

## Understanding the example
