import os
from getpass import getpass
from typing import Any

from dotenv import load_dotenv
from langchain.chat_models import init_chat_model
from langchain.schema import HumanMessage, SystemMessage

load_dotenv()
if not os.environ.get("OPENAI_API_KEY"):
  os.environ["OPENAI_API_KEY"] = getpass("Enter API key for OpenAI: ")

llm = init_chat_model("gpt-4o-mini", model_provider="openai")

ROUTES = ["general-question", "technical-support", "other"]

async def fetch_customer_data(customer_id: str) -> dict[str, Any]:
    # Simulate fetching customer data from a database
    return {
        "id": customer_id,
        "name": "John Doe",
        "email": "john.doe@example.com",
        "phone": "123-456-7890",
        "address": "123 Main St, Anytown, USA",
    }

async def ai_router(customer_message: str) -> str:
    chat = llm.with_structured_output({
        "title": "Route",
        "description": "Which route should the customer message be routed to?",
        "type": "object",
        "properties": {
            "route": {
                "type": "string",
                "enum": ROUTES,
                "description": "Which route should the customer message be routed to?"
            }
        },
        "required": ["route"]
    })
    response = chat.invoke([
        SystemMessage(content="You are a helpful assistant that routes customer messages to the appropriate department. If you are not confident in the route or if you think a human needs to be involved just return 'other'."),
        HumanMessage(content=customer_message)
    ])
    return response["route"]

async def general_question(customer_message: str, customer_data: dict[str, Any]) -> str:
    chat = llm.with_structured_output({
        "title": "Email",
        "description": "The email to send to the customer.",
        "type": "object",
        "properties": {
            "subject": {
                "type": "string",
                "description": "The subject of the email."
            },
            "body": {
                "type": "string",
                "description": "The body of the email."
            }
        },
        "required": ["subject", "body"]
    })
    response = chat.invoke([
        SystemMessage(content="You are a helpful assistant that answers general customer support questions and sends an email to the customer. The customer is asking the question so please make sure to answer the question in a way that is helpful to the customer.\n\n Customer Data: " + customer_data.__str__()),
        HumanMessage(content=customer_message)
    ])

    return await send_email(customer_data["email"], response["subject"], response["body"])

async def technical_support(customer_message: str, customer_data: dict[str, Any]) -> str:
    chat = llm.with_structured_output({
        "title": "Email",
        "description": "The email to send to the customer.",
        "type": "object",
        "properties": {
            "subject": {
                "type": "string",
                "description": "The subject of the email."
            },
            "body": {
                "type": "string",
                "description": "The body of the email."
            }
        },
        "required": ["subject", "body"]
    })  
    response = chat.invoke([
        SystemMessage(content="You are a helpful assistant that answers technical support questions and sends an email to the customer. The customer is asking the question so please make sure to answer the question in a way that is helpful to the customer.\n\n Customer Data: " + customer_data.__str__()),
        HumanMessage(content=customer_message)
    ])

    return await send_email(customer_data["email"], response["subject"], response["body"])

async def send_email(to: str, subject: str, body: str) -> None:
    # Simulate sending an email
    email = f"To: {to}\nSubject: {subject}\n\n{body}"
    return email
