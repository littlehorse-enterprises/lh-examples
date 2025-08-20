import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { immer } from 'zustand/middleware/immer';

export type StepNumber = 1 | 2 | 3 | 4 | 5 | 6 | 7;
export type FinanceDecision = 'APPROVE' | 'DECLINE';

interface WorkflowState {
  currentStep: StepNumber;
  apiHealthy: boolean;
  
  wfRunId: string;
  requestingUserId: string;
  
  requestingUserTaskGuid: string;
  financeUserTaskGuid: string;
  
  requestedItem: string;
  justification: string;
  requestingTaskSubmitted: boolean;
  
  financeAssigneeUserId: string;
  financeOverride: boolean;
  financeAssigned: boolean;
  financeDecision: FinanceDecision;
  
  isLoading: boolean;
  statusText: string;
  responseText: string;
  showResultModal: boolean;
  workflowCompleted: boolean;
  deleteScope: 'none' | 'current' | 'all';
}

interface WorkflowActions {
  setStep: (step: StepNumber) => void;
  nextStep: () => void;
  
  setApiHealth: (healthy: boolean) => void;
  
  setWfRunId: (id: string) => void;
  setRequestingUserId: (userId: string) => void;
  
  setRequestingUserTaskGuid: (guid: string) => void;
  setFinanceUserTaskGuid: (guid: string) => void;
  
  setRequestData: (item: string, justification: string) => void;
  setRequestingTaskSubmitted: (submitted: boolean) => void;
  
  setFinanceAssignee: (userId: string) => void;
  setFinanceOverride: (override: boolean) => void;
  setFinanceAssigned: (assigned: boolean) => void;
  setFinanceDecision: (decision: FinanceDecision) => void;
  
  setLoading: (loading: boolean) => void;
  setStatus: (text: string) => void;
  setResponse: (text: string) => void;
  setShowResultModal: (show: boolean) => void;
  setWorkflowCompleted: (completed: boolean) => void;
  setDeleteScope: (scope: 'none' | 'current' | 'all') => void;
  
  reset: () => void;
}

const initialState: WorkflowState = {
  currentStep: 1,
  apiHealthy: false,
  wfRunId: '',
  requestingUserId: '',
  requestingUserTaskGuid: '',
  financeUserTaskGuid: '',
  requestedItem: '',
  justification: '',
  requestingTaskSubmitted: false,
  financeAssigneeUserId: '',
  financeOverride: false,
  financeAssigned: false,
  financeDecision: 'APPROVE',
  isLoading: false,
  statusText: '',
  responseText: '',
  showResultModal: false,
  workflowCompleted: false,
  deleteScope: 'none',
};

export const useWorkflowStore = create<WorkflowState & WorkflowActions>()(
  devtools( // TODO: devtools is optional, but useful for debugging
    immer((set) => ({
      ...initialState,
      
      setStep: (step) => set((state) => {
        state.currentStep = step;
        state.responseText = '';
      }),
      
      nextStep: () => set((state) => {
        if (state.currentStep < 7) {
          state.currentStep = (state.currentStep + 1) as StepNumber;
          state.responseText = '';
        }
      }),
      
      setApiHealth: (healthy) => set((state) => {
        state.apiHealthy = healthy;
      }),
      
      setWfRunId: (id) => set((state) => {
        state.wfRunId = id;
      }),
      
      setRequestingUserId: (userId) => set((state) => {
        state.requestingUserId = userId;
      }),
      
      setRequestingUserTaskGuid: (guid) => set((state) => {
        state.requestingUserTaskGuid = guid;
      }),
      
      setFinanceUserTaskGuid: (guid) => set((state) => {
        state.financeUserTaskGuid = guid;
      }),
      
      setRequestData: (item, justification) => set((state) => {
        state.requestedItem = item;
        state.justification = justification;
      }),
      
      setRequestingTaskSubmitted: (submitted) => set((state) => {
        state.requestingTaskSubmitted = submitted;
      }),
      
      setFinanceAssignee: (userId) => set((state) => {
        state.financeAssigneeUserId = userId;
      }),
      
      setFinanceOverride: (override) => set((state) => {
        state.financeOverride = override;
      }),
      
      setFinanceAssigned: (assigned) => set((state) => {
        state.financeAssigned = assigned;
      }),
      
      setFinanceDecision: (decision) => set((state) => {
        state.financeDecision = decision;
      }),
      
      setLoading: (loading) => set((state) => {
        state.isLoading = loading;
      }),
      
      setStatus: (text) => set((state) => {
        state.statusText = text;
      }),
      
      setResponse: (text) => set((state) => {
        state.responseText = text;
      }),
      
      setShowResultModal: (show) => set((state) => {
        state.showResultModal = show;
      }),
      
      setWorkflowCompleted: (completed) => set((state) => {
        state.workflowCompleted = completed;
      }),
      
      setDeleteScope: (scope) => set((state) => {
        state.deleteScope = scope;
      }),
      
      reset: () => set(() => initialState)
    }))
  )
);