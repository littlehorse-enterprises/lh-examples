"""Constants for the routing service."""
from enum import Enum


class UserTaskDefNames(str, Enum):
    """Constants for UserTaskDefs names."""

    OTHER = "other"

class TaskDefNames(str, Enum):
    """Constants for TaskDefs names."""

    AI_ROUTER = "ai-router"
    GENERAL_QUESTION = "general-question"
    FETCH_CUSTOMER_DATA = "fetch-customer-data"
    TECHNICAL_SUPPORT = "technical-support"
    SEND_EMAIL = "send-email"

class WorkflowNames(str, Enum):
    """Constants for Workflows names."""

    CUSTOMER_SERVICE_ROUTING = "customer-service-routing"

class VariableNames(str, Enum):
    """Constants for Variables names."""

    CUSTOMER_MESSAGE = "customer-message"
    CUSTOMER_ID = "customer-id"
    ROUTE = "route"
    CUSTOMER_DATA = "customer-data"
    EMAIL_REQUEST = "email-request"