from enum import Enum

class TaskDefNames(str, Enum):
    """Enumeration of Task names."""
    
    ORCHESTRATE_TOPICS = "orchestrate-topics"
    DELEGATE_WORKER = "delegate-worker"
    SYNTHESIZE_REPORTS = "synthesize-reports"

class WorkflowNames(str, Enum):
    """Enumeration of Workflow names."""
    
    STARTUP_GENERATOR = "startup-generator"

class ThreadNames(str, Enum):
    """Enumeration of Thread names."""
    
    DELEGATE_WORKERS = "delegate-workers"

class VariableNames(str, Enum):
    """Enumeration of Variable names."""
    
    WORKER_PROMPTS = "worker-prompts"
