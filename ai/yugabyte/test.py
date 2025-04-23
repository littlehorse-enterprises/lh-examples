from process_data import process, check_pdf_unique, register_tasks
from littlehorse.config import LHConfig
from littlehorse import create_task_def
import asyncio


async def main():
    config = LHConfig()
    # client = config.stub()

    #task names must be in lowercase and not contain spaces
    register_tasks(config)
    await process("data/Evolution/s41467-021-23804-7.pdf", "Evolution")

if __name__ == "__main__":
    asyncio.run(main())