from enum import Enum


class TaskDefNames(str, Enum):
    """Enumeration of Task names."""

    FETCH_CUSTOMER_CRM_DATA = "fetch-customer-crm-data"
    GENERATE_EMAIL = "generate-email"
    APPROVE_EMAIL = "approve-email"


class WorkflowNames(str, Enum):
    """Enumeration of Workflow names."""

    SALES_EMAIL_PERSONALIZATION = "sales-email-personalization"


class ThreadNames(str, Enum):
    """Enumeration of Thread names."""


class VariableNames(str, Enum):
    """Enumeration of Variable names."""

    CUSTOMER_ID = "customer-id"
    APPROVED_EMAIL = "approved-email"
    INSTRUCTIONS = "instructions"
    FEEDBACK = "feedback"
    PREVIOUS_EMAIL = "previous-email"