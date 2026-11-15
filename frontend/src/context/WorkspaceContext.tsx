import { createContext, useContext, useState, useEffect, useCallback, type ReactNode, type FC } from 'react';
import { CompactBusiness } from '../types';
import { request } from '../services/api';
import { useAuth } from './AuthContext';

interface WorkspaceContextType {
  workspaces: CompactBusiness[];
  activeWorkspace: CompactBusiness | null;
  loading: boolean;
  setActiveWorkspace: (workspace: CompactBusiness) => void;
  reloadWorkspaces: () => Promise<void>;
}

const WorkspaceContext = createContext<WorkspaceContextType | undefined>(undefined);

export const WorkspaceProvider: FC<{ children: ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [workspaces, setWorkspaces] = useState<CompactBusiness[]>([]);
  const [activeWorkspace, setActiveWorkspaceState] = useState<CompactBusiness | null>(null);
  const [loading, setLoading] = useState(false);

  const reloadWorkspaces = useCallback(async () => {
    if (!isAuthenticated) {
      setWorkspaces([]);
      setActiveWorkspaceState(null);
      return;
    }

    setLoading(true);
    try {
      const data = await request<CompactBusiness[]>('/businesses');
      setWorkspaces(data || []);

      const savedId = localStorage.getItem('active_workspace_id');
      if (data && data.length > 0) {
        const found = savedId ? data.find((w) => w.id === Number(savedId)) : data[0];
        const selected = found || data[0];
        setActiveWorkspaceState(selected);
        localStorage.setItem('active_workspace_id', String(selected.id));
      } else {
        setActiveWorkspaceState(null);
        localStorage.removeItem('active_workspace_id');
      }
    } catch {
      setWorkspaces([]);
      setActiveWorkspaceState(null);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    reloadWorkspaces();
  }, [reloadWorkspaces]);

  const setActiveWorkspace = (workspace: CompactBusiness) => {
    setActiveWorkspaceState(workspace);
    localStorage.setItem('active_workspace_id', String(workspace.id));
  };

  return (
    <WorkspaceContext.Provider
      value={{
        workspaces,
        activeWorkspace,
        loading,
        setActiveWorkspace,
        reloadWorkspaces,
      }}
    >
      {children}
    </WorkspaceContext.Provider>
  );
};

export function useWorkspace() {
  const context = useContext(WorkspaceContext);
  if (!context) {
    throw new Error('useWorkspace must be used within a WorkspaceProvider');
  }
  return context;
}

