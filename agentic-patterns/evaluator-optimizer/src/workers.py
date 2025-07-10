import getpass
import os
from typing import Any

from dotenv import load_dotenv
from langchain.chat_models import init_chat_model
from langchain.schema import HumanMessage, SystemMessage
from utils.constants import TaskDefNames
from utils.logger import logger
from utils.worker_registry import worker

load_dotenv()

if not os.environ.get("OPENAI_API_KEY"):
    os.environ["OPENAI_API_KEY"] = getpass.getpass("Enter API key for OpenAI: ")

llm = init_chat_model("gpt-4o-mini", model_provider="openai") 

# @worker(TaskDefNames.ORCHESTRATE_TOPICS)
async def orchestrate_topics(prompt: str) -> str:
    logger.info(f"Orchestrating topics for prompt: {prompt}")
    chat = llm.with_structured_output({
        "name": "research_orchestration",
        "description": "A number of prompts for other AI LLM workers that represent specific topics to research to form a full business plan.",
        "parameters": {
            "type": "object",
            "properties": {
                "topics": {
                    "type": "array",
                    "items": {
                        "type": "string",
                        "description": "A prompt for an AI LLM worker that will research the specific topic and compile information for."
                    }
                }
            },
            "required": ["topics"]
        }
    })

    response = chat.invoke([
        SystemMessage(
            content="You are an experienced and helpful AI orchesrator who will create a series of prompts for other AI LLM workers that represent specific topics to research to form a full business plan."),
        HumanMessage(content=prompt)
    ])

    return response


@worker(TaskDefNames.FETCH_CUSTOMER_CRM_DATA)
async def fetch_customer_crm_data(customer_id: str) -> dict[str, Any]:
    # In a real world scenario, this would be a call to a CRM API like Salesforce or Hubspot
    return {
        "customerId": customer_id,
        "firstName": "Emily",
        "lastName": "Tran",
        "email": "emily.tran@futurefintech.com",
        "phone": "+1-415-555-0198",
        "company": "Future FinTech",
        "jobTitle": "Lead Platform Engineer",
        "industry": "Financial Services",
        "location": {
            "city": "San Francisco",
            "state": "CA",
            "country": "USA"
        },
        "accountStatus": "Active",
        "lastContacted": "2025-03-15T10:34:00Z",
        "lastActivity": "2025-04-10T14:23:00Z",
        "lifecycleStage": "Customer",
        "dealSize": 150000,
        "leadSource": "Webinar - Microservice Scaling",
        "interests": ["Workflow Automation", "Observability", "Microservices"],
        "notes": "Interested in improving internal microservice reliability. Mentioned a pain point around manual escalation handling."
    }

@worker(TaskDefNames.GENERATE_EMAIL)
async def generate_email(customer_data: dict[str, Any], user_instructions: str, previous_interactions: list[Any]) -> dict[str, Any]:
    chat = llm.with_structured_output({
        "name": "email",
        "description": "A LLM that will generate an email to the customer based on the CRM data and the users instructions and any feedback the AI Evaluator has provided.",
        "parameters": {
            "type": "object",
            "properties": {
                "to": {
                    "type": "string",
                    "description": "The email address of the customer."
                },
                "subject": {
                    "type": "string",
                    "description": "The subject of the email."
                },
                "body": {
                    "type": "string",
                    "description": "The body of the email."
                }
            },
            "required": ["to", "subject", "body"]
        }
    })
    response = chat.invoke([
        SystemMessage(content="You are an experienced email writer who will write an email to a customer based on the CRM data and the users instructions and any feedback the AI Evaluator has provided. Please dont have template names in the footer of the body. Use 'Hazim Arafa' as name, account executive from LittleHorse."),
        HumanMessage(content=f"Customer CRM Data: {customer_data}\nUser Instructions: {user_instructions}\nPrevious Interactions: {previous_interactions}")
    ])

    return {
        "to": response["to"],
        "subject": response["subject"],
        "body": response["body"]
    }

@worker(TaskDefNames.APPROVE_EMAIL)
async def approve_email(email: dict[str, Any]) -> str:
    chat = llm.with_structured_output({
        "name": "feedback",
        "description": "Feedback that will be sent back to an AI LLM worker that is generating a response.",
        "parameters": {
            "type": "object",
            "properties": {
                "feedback": {
                    "type": "string",
                    "description": "Feedback that will be sent back to an AI LLM worker that is generating a response."
                }
            },
            "required": ["feedback"]
        }
    })
    response = chat.invoke([
        SystemMessage(
            content="You will recieve an email from another LLM. Your job is to decide if the text generated by the other LLM accomplishes the task it was given. If it does, return an empty string. If it does not, return feedback on how to improve the text. You are in a loop with the other LLM, so you will need to keep generating feedback until the other LLM returns text that you are happy with. This means do not force feedback if the email is good. Only when you think there is a way to make it better."),
        HumanMessage(content=f"Email: {email}")
    ])

    return response["feedback"]




