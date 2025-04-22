# AI Routing Workflow with LittleHorse

This example demonstrates how to build an AI-powered customer service routing system using LittleHorse. The workflow automatically routes customer inquiries to the appropriate support channel based on the content of their message.

## Features

- AI-powered message classification using LangChain and OpenAI
- Automatic routing to specialized support channels
- Customer data integration
- Email response generation
- Human-in-the-loop for complex cases
- Retry mechanisms for reliability

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

Copy the `.env.example` file and rename it to `.env`. Now update the env with your OpenAI API key.

## How It Works

The workflow processes customer inquiries through the following steps:

1. Receives a customer message and ID
2. Fetches customer data from your system
3. Uses AI to classify the message type (general question, technical support, or other)
4. Routes to appropriate handler:
   - General questions: AI generates a response
   - Technical support: AI generates a technical response
   - Other: Creates a human review task
5. Sends the response via email

## Running the Example

To run the example:

```bash
python src/main.py
```

This will:

- Register the workflow and task definitions
- Start the task workers
- Execute a sample workflow run

## Project Structure

- `src/main.py`: Main workflow definition and execution
- `src/workers.py`: Task worker implementations
- `src/utils/`: Helper modules and constants
- `requirements.txt`: Python dependencies

## Customization

You can customize the workflow by:

- Modifying the AI classification logic in the workers
- Adding new routing categories
- Changing the response generation templates
- Integrating with your own customer data system
- Using another AI model instead of OpenAI
