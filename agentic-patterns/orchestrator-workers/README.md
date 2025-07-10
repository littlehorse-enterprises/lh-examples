# AI Orchestrator Workers with LittleHorse

This example demonstrates how to build an AI-powered orchestration system using LittleHorse. The workflow coordinates multiple AI workers to process and generate content based on an initial prompt.

## Features

- AI-powered topic orchestration using LangChain and OpenAI
- Parallel worker execution for efficient processing
- Dynamic content generation and synthesis
- Modular worker architecture
- Asynchronous task processing
- Structured output handling

## Prerequisites

- Python 3.9+
- Docker (for running LittleHorse)
- OpenAI API key (set in your environment variables)

## Setup

1. First, get a running instance of LittleHorse:

```bash
docker run --pull always --name lh-standalone --rm -d -p 2023:2023 -p 8080:8080 \
  ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:latest
```

2. Create and activate a virtual environment:

```bash
python -m venv .venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate
```

3. Install dependencies:

```bash
pip install -r requirements.txt
```

4. Set up your environment variables:

Copy the `.env.example` file and rename it to `.env`.

Now update the env with your OpenAI API key.

```bash
OPENAI_API_KEY=sk-proj-XXXXXXXX
```

## How It Works

The workflow processes content through the following steps:

1. Receives an initial prompt
2. Orchestrates topics for parallel processing
3. Spawns multiple worker threads for each topic
4. Delegates work to specialized AI workers
5. Synthesizes results into a final report

## Running the Example

To run the example:

```bash
python src/main.py
```

This will:

- Register the workflow and task definitions
- Start the task workers
- Execute a sample workflow run with a business plan generation prompt

## Project Structure

- `src/main.py`: Main workflow definition and execution
- `src/workers.py`: Task worker implementations
- `src/utils/`: Helper modules and constants
- `requirements.txt`: Python dependencies

## Customization

You can customize the workflow by:

- Modifying the topic orchestration logic
- Adding new worker types
- Changing the synthesis process
- Integrating with different AI models
- Adjusting the parallel processing strategy
