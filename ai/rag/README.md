# LittleHorse Enterprises - AI Yugabyte Examples

This example simulates a Retrieval Augmented Generation (RAG) workflow for real-time data processing and summarization. This workflow leverages a Yugabyte database with Postgres for data storage and LittleHorse for workflow orchestration.

## Features

- **PDF Processing**: Load and process PDF files from a specified directory.
- **Text Chunking**: Split text into manageable chunks for embedding.
- **AI Integration**: Use OpenAI models for text embedding and summarization.
- **Database Storage**: Store and retrieve data from a Yugabyte database.
- **Workflow Automation**: Automate tasks using LittleHorse Workflow Engine.
- **Chat UI**: Talk to an LLM about your PDF data

## Prerequisites

- Python 3.9+
- OpenAI API key
- Docker

## Installation

1. Clone the repository:

   ```bash
      git clone https://github.com/littlehorse-enterprises/lh-examples.git
      cd lh-examples/ai/rag/backend
   ```

2. Create and activate a python virtual environment:

   ```bash
      python -m venv .venv
      source .venv/bin/activate # On Windows: .venv\Scripts\activate
   ```

3. Install python dependencies:

   ```bash
      pip install -r requirements.txt
   ```

4. Set up environment variables:

   Rename the `.env.example` file to `.env` and fill in the `OPENAI_API_KEY` variable with your API key.

5. Set up your Littlehorse and Yugabyte instances with Docker:

   ```bash
      docker compose up
   ```

Note: you may need to add `sudo` before the docker command.

## How it Works

The workflow simulates uploading a PDF to an LLM by pulling a file from the `temp` directory. When the PDF is received, the workflow extracts the text, converts it to embeddings, and stores them in a vector database provided by Yugabyte. Another workflow is launched that allows the user to interact with a chatbot that uses the uploaded PDF data as context.


## Running the Example

Assuming `docker compose up` ran successfully, open up a new terminal and run:

```bash
   source .venv/bin/activate # On Windows: .venv\Scripts\activate
   python main.py
```

This will register the task definitions `TaskDef`s with the Littlehorse server and start each dedicated task worker to begin polling for tasks. These task workers live on your local machine, and persist until they are stopped.

Note: if the terminal output says something like: `Establishing insecure channel at localhost:2023`, that is OK.

That is it! The workflow is now running on Littlehorse! You can view the UI by navigating to the Littlehorse Dashboard at:

<https://localhost:8080>

There, you will be able to see the various `TaskDef`s and `WfSpec`s that are registered. You can view all of the runs for a specific workflow by clicking on the workflow spec.

Note: Littlehorse stores and logs the output of all workflow runs, click on a link to a specific workflow run below to view the status of the workflow.

Note: if you want to restart the code, you need to run `docker compose down` and then `docker compose up` again to reset the server.


## Viewing the UI
The chat interface is viewable by navigating to <https://localhost:3000> here you will be able to interact with the GPT model and ask questions relevant to your data!