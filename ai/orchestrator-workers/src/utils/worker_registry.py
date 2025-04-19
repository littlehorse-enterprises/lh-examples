from typing import Callable, Dict, List
import littlehorse
from littlehorse.worker import LHTaskWorker
from littlehorse.config import LHConfig
from littlehorse import create_task_def
from littlehorse.model import LittleHorseStub, TaskDefId, TaskDef, DeleteTaskDefRequest
from utils.logger import logger
import asyncio
from enum import Enum

class WorkerRegistry:
    _workers: Dict[str, Callable] = {}
    
    @staticmethod
    def register(task_name: str | Enum):
        # If task_name is an Enum, get its value
        task_name_str = task_name.value if isinstance(task_name, Enum) else task_name
        logger.info(f"Indexing worker: {task_name_str}")
        def decorator(func: Callable):
            WorkerRegistry._workers[task_name_str] = func
            return func
        return decorator

    @staticmethod
    def delete_all(stub: LittleHorseStub):
        for task_name in WorkerRegistry._workers.keys():
            logger.info(f"Deleting TaskDef: {task_name}")
            stub.DeleteTaskDef(DeleteTaskDefRequest(id=TaskDefId(name=task_name)))
        WorkerRegistry._workers.clear()
    
    @staticmethod
    def register_all(config: LHConfig) -> None:
        logger.info("Registering all workers")
        logger.info(WorkerRegistry._workers.values())
        for task_name, worker_func in WorkerRegistry._workers.items():
            create_task_def(worker_func, task_name, config)
            logger.info(f"Registered worker: {task_name}")

    @staticmethod
    async def start_all(config: LHConfig) -> None:
        logger.info("Starting all workers")
        workers = [
            LHTaskWorker(worker_func, task_name, config)
            for task_name, worker_func in WorkerRegistry._workers.items()
        ]
        await littlehorse.start(*workers)
        logger.info(f"Started {len(workers)} workers")

# Create a convenient decorator
def worker(task_name: str):
    return WorkerRegistry.register(task_name) 