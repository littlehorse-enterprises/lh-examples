import getpass
import os

from dotenv import load_dotenv

load_dotenv()

if not os.environ.get("OPENAI_API_KEY"):
  os.environ["OPENAI_API_KEY"] = getpass.getpass("Enter API key for OpenAI: ")

from langchain.chat_models import init_chat_model

model = init_chat_model("gpt-4o-mini", model_provider="openai")

async def ai_router(transcript: str) -> str:
    response = await model.ainvoke(transcript)
    return response.content