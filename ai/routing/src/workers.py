import os
from getpass import getpass
from typing import Any

from dotenv import load_dotenv
from langchain.chat_models import init_chat_model
from langchain.schema import HumanMessage, SystemMessage
from utils.constants import TaskDefNames, UserTaskDefNames
from utils.worker_registry import worker

load_dotenv()
if not os.environ.get("OPENAI_API_KEY"):
  os.environ["OPENAI_API_KEY"] = getpass("Enter API key for OpenAI: ")

llm = init_chat_model("gpt-4o-mini", model_provider="openai")

@worker(TaskDefNames.FETCH_CUSTOMER_DATA)
async def fetch_customer_data(customer_id: str) -> dict[str, Any]:
    # Simulate fetching customer data from a database
    return {
        "id": customer_id,
        "name": "John Doe",
        "email": "john.doe@example.com",
        "phone": "123-456-7890",
        "address": "123 Main St, Anytown, USA",
    }

@worker(TaskDefNames.AI_ROUTER)
async def ai_router(customer_message: str) -> str:
    chat = llm.with_structured_output({
        "title": "Route",
        "description": "Which route should the customer message be routed to?",
        "type": "object",
        "properties": {
            "route": {
                "type": "string",
                "enum": [TaskDefNames.GENERAL_QUESTION, TaskDefNames.TECHNICAL_SUPPORT, UserTaskDefNames.OTHER],
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

@worker(TaskDefNames.GENERAL_QUESTION)
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
        SystemMessage(content="You are a helpful assistant that answers general customer support questions and sends an email to the customer. The customer is asking the question so please make sure to answer the question in a way that is helpful to the customer. Please dont have template names in the footer of the body. Use 'Hazim Arafa' as the name from LittleHorse Customer Support Team.\n\nCustomer Data: " + customer_data.__str__()),
        HumanMessage(content=customer_message)
    ])

    return {"subject": response["subject"], "body": response["body"]}

@worker(TaskDefNames.TECHNICAL_SUPPORT)
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
        SystemMessage(content="You are a helpful assistant that answers technical support questions and sends an email to the customer. The customer is asking the question so please make sure to answer the question in a way that is helpful to the customer. Please dont have template names in the footer of the body. Use 'Hazim Arafa' as the name from LittleHorse Customer Support Team.\n\nCustomer Data: " + customer_data.__str__()),
        HumanMessage(content=customer_message)
    ])

    return {"subject": response["subject"], "body": response["body"]}

@worker(TaskDefNames.SEND_EMAIL)
async def send_email(request: dict[str, Any]) -> None:
    missing_fields = []
    if not request["to"]:
        missing_fields.append("to")
    if not request["subject"]:
        missing_fields.append("subject")
    if not request["body"]:
        missing_fields.append("body")
    
    if len(missing_fields) > 0:
        raise ValueError(f"Invalid email request missing required fields: {missing_fields}")
    
    # Simulate sending an email
    email = f"To: {request['to']}\nSubject: {request['subject']}\n\n{request['body']}"
    return email
