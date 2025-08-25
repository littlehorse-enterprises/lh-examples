'use client';

import { createContext, useContext, useState, ReactNode, useCallback } from 'react';

interface WorkflowContextType {
  wfRunId: string | null;
  currentStep: number;
  
  isLoading: boolean;
  statusText: string;
  responseText: string;
  
  taskGuid?: string;
  userId?: string;
  requestData?: {
    item: string;
    justification: string;
  };
  
  setLoading: (loading: boolean) => void;
  setStatus: (text: string) => void;
  setResponse: (text: string) => void;
  setTaskGuid: (guid: string) => void;
  setRequestData: (data: { item: string; justification: string }) => void;
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
  const [statusText, setStatusText] = useState('Idle');
  const [responseText, setResponseText] = useState('');
  
  const [taskGuid, setTaskGuid] = useState(initialTaskGuid || '');
  const [requestData, setRequestData] = useState({ item: '', justification: '' });
  
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
    userId,
    taskGuid,
    requestData,
    setLoading,
    setStatus,
    setResponse,
    setTaskGuid,
    setRequestData,
  };

  return (
    <WorkflowContext.Provider value={value}>
      {children}
    </WorkflowContext.Provider>
  );
}