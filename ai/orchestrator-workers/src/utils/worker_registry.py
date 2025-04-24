from enum import Enum
from typing import Callable, Dict

import littlehorse
from littlehorse import create_task_def
from littlehorse.config import LHConfig
from littlehorse.model import DeleteTaskDefRequest, LittleHorseStub, TaskDefId
from littlehorse.worker import LHTaskWorker
from utils.logger import logger


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
        logger.info(f"Started {len(workers)} workers")
        await littlehorse.start(*workers)

# Create a convenient decorator
def worker(task_name: str):
    return WorkerRegistry.register(task_name) 