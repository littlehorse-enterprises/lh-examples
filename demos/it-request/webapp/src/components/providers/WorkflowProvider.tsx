'use client';

import { createContext, useContext, useState, ReactNode, useCallback } from 'react';

interface WorkflowContextType {
  wfRunId: string | null;
  currentStep: number;
  
  isLoading: boolean;
  statusText: string;
  responseText: string;
  showResultModal: boolean;
  
  taskGuid?: string;
  userId?: string;
  requestData?: {
    item: string;
    justification: string;
  };
  financeDecision?: 'APPROVE' | 'DECLINE';
  
  setLoading: (loading: boolean) => void;
  setStatus: (text: string) => void;
  setResponse: (text: string) => void;
  setShowResultModal: (show: boolean) => void;
  setTaskGuid: (guid: string) => void;
  setRequestData: (data: { item: string; justification: string }) => void;
  setFinanceDecision: (decision: 'APPROVE' | 'DECLINE') => void;
}

const WorkflowContext = createContext<WorkflowContextType | null>(null);

export function useWorkflowContext(): WorkflowContextType {
  const context = useContext(WorkflowContext);

  if (!context) {
    throw new Error('WorkflowProvider not found');
  }

  return context;
}

interface WorkflowProviderProps {
  children: ReactNode;
  wfRunId: string | null;
  currentStep: number;
  taskGuid?: string;
  userId?: string;
}

export function WorkflowProvider({ 
  children, 
  wfRunId, 
  currentStep,
  userId,
  taskGuid: initialTaskGuid 
}: WorkflowProviderProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [statusText, setStatusText] = useState('');
  const [responseText, setResponseText] = useState('');
  const [showResultModal, setShowResultModal] = useState(false);
  
  const [taskGuid, setTaskGuid] = useState(initialTaskGuid || '');
  const [requestData, setRequestData] = useState({ item: '', justification: '' });
  const [financeDecision, setFinanceDecision] = useState<'APPROVE' | 'DECLINE'>('APPROVE');
  
  const setLoading = useCallback((loading: boolean) => {
    setIsLoading(loading);
  }, []);
  
  const setStatus = useCallback((text: string) => {
    setStatusText(text);
  }, []);
  
  const setResponse = useCallback((text: string) => {
    setResponseText(text);
  }, []);

  const value = {
    wfRunId,
    currentStep,
    isLoading,
    statusText,
    responseText,
    showResultModal,
    userId,
    taskGuid,
    requestData,
    financeDecision,
    setLoading,
    setStatus,
    setResponse,
    setShowResultModal,
    setTaskGuid,
    setRequestData,
    setFinanceDecision,
  };

  return (
    <WorkflowContext.Provider value={value}>
      {children}
    </WorkflowContext.Provider>
  );
}