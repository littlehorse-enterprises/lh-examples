# AI Evaluator Optimizer with LittleHorse

This example demonstrates how to build an AI-powered evaluation and optimization system using LittleHorse. The workflow evaluates and optimizes AI-generated content through a feedback loop, ensuring high-quality output.

## Features

- AI-powered content evaluation using LangChain and OpenAI
- Iterative content optimization through feedback loops
- Customer data integration and personalization
- Email generation and approval workflow
- Structured output validation
- Quality assurance automation

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

1. Fetches customer data from CRM
2. Generates initial content based on user instructions
3. Evaluates content quality through AI feedback
4. Optimizes content based on feedback
5. Approves final content for delivery
6. Generates personalized email responses

## Running the Example

To run the example:

```bash
python src/register.py
```

This will:

- Register the workflow and task definitions
- Start the task workers
- Execute a sample workflow run with customer data

## Project Structure

- `src/register.py`: Workflow registration and execution
- `src/workers.py`: Task worker implementations
- `src/model.py`: AI model configuration
- `src/utils/`: Helper modules and constants
- `requirements.txt`: Python dependencies

## Customization

You can customize the workflow by:

- Modifying the evaluation criteria
- Adjusting the feedback loop parameters
- Integrating with different CRM systems
- Changing the email generation templates
- Using alternative AI models
- Adding new optimization strategies
