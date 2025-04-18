from typing import Callable, Dict, List
import littlehorse
from littlehorse.worker import LHTaskWorker
from littlehorse.config import LHConfig
from littlehorse import create_task_def
from littlehorse.model import LittleHorseStub, TaskDefId, TaskDef, DeleteTaskDefRequest
from utils.logger import logger
import asyncio

class WorkerRegistry:
    _workers: Dict[str, Callable] = {}
    
    @staticmethod
    def register(task_name: str):
        def decorator(func: Callable):
            WorkerRegistry._workers[task_name] = func
            return func
        return decorator

    @staticmethod
    def delete_all(stub: LittleHorseStub):
        for task_name in WorkerRegistry._workers.keys():
            stub.DeleteTaskDef(DeleteTaskDefRequest(id=TaskDefId(name=task_name)))
        WorkerRegistry._workers.clear()
    
    @staticmethod
    def register_all(config: LHConfig) -> None:
        logger.info("Registering all workers")
        for task_name, worker_func in WorkerRegistry._workers.items():
            create_task_def(worker_func, task_name, config)
            logger.info(f"Registered worker: {task_name}")

    @staticmethod
    async def start_all(config: LHConfig) -> None:
        logger.info("Starting all workers")
        coroutines = []
        for task_name, worker_func in WorkerRegistry._workers.items():
            coroutine = littlehorse.start(LHTaskWorker(worker_func, task_name, config))
            coroutines.append(coroutine)
            logger.info(f"Queued worker: {task_name}")
        
        await asyncio.gather(*coroutines)
        logger.info("All workers started successfully")

# Create a convenient decorator
def worker(task_name: str):
    return WorkerRegistry.register(task_name) 