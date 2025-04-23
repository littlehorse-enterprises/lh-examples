from database import CONNECT

from langchain.chat_models import init_chat_model
from langchain_postgres import PGVector
from langchain_openai import OpenAIEmbeddings
from langchain_core.prompts import ChatPromptTemplate


class RAG_Agent:


    def __init__(self):
        
        self.prompt_text = f"""
                    You are an AI Assistant tasked with understanding detailed
                    information about recent US tarrifs from text and tables. You are to answer the question based on the 
                    context provided to you. You must not go beyond the context given to you.
                    """

    def retrieve(self):
        embedding_model = OpenAIEmbeddings(model="text-embedding-ada-002")
        vector_store = PGVector(
            embeddings=embedding_model,
            connection=CONNECT,
        )
        question = """give me a detailed summary of US tarrifs""" #key words specific to field here
        retrieved_docs = vector_store.similarity_search(question)
        docs_content = "\n\n".join(doc.page_content for doc in retrieved_docs)

        return docs_content

    def generate(self):
        prompt_text = self.prompt_text + """    


                    Context:
                    {context}

                    Question:
                    {question}
                    """

        prompt = ChatPromptTemplate.from_template(prompt_text)

        prompt = prompt.invoke(
            {
                "context": self.retrieve(),
                "question": """give me a detailed summary of the context """
            }
        )

        llm = init_chat_model("openai:gpt-4o-mini")
        answer = llm.invoke(prompt)

        return answer.content
