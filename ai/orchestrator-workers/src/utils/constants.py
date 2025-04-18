from enum import Enum

class TaskDefNames(str, Enum):
    """Enumeration of Task names."""
    
    ORCHESTRATE_TOPICS = "orchestrate-topics"
    DELEGATE_WORKERS = "delegate-workers"
    SYNTHESIZE_REPORTS = "synthesize-reports"

class WorkflowNames(str, Enum):
    """Enumeration of Workflow names."""
    
    STARTUP_GENERATOR = "startup-generator"
