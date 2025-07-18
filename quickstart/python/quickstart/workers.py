import asyncio
import logging
import random
from typing import Annotated

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.worker import LHTaskWorker, LHType, WorkerContext

logging.basicConfig(level=logging.INFO)

async def verify_identity(full_name: str, email: str, ssn: Annotated[int, LHType(name="ssn", masked=True)], ctx: WorkerContext) -> str:
    if random.random() < 0.25:
        raise RuntimeError("The external identity verification API is down")
    return f"Successfully called external API to request verification for {full_name} at {email}"

async def notify_customer_verified(full_name: str, email: str, ctx: WorkerContext) -> str:
    return f"Notification sent to customer {full_name} at {email} that their identity has been verified"

async def notify_customer_not_verified(full_name: str, email: str, ctx: WorkerContext) -> str:
    return f"Notification sent to customer {full_name} at {email} that their identity has not been verified"

async def main() -> None:
    logging.info("Starting Task Worker!")
    
    # Configuration loaded from environment variables
    config = LHConfig()

    await littlehorse.start(LHTaskWorker(verify_identity, "verify-identity", config), LHTaskWorker(notify_customer_verified, "notify-customer-verified", config), LHTaskWorker(notify_customer_not_verified, "notify-customer-not-verified", config))

if __name__ == '__main__':
    asyncio.run(main())
