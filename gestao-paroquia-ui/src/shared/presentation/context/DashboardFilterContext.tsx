import React, {createContext, ReactNode, useContext, useMemo, useState} from 'react';
import {PeriodoDashboard} from '@/shared/domain/types/types';

interface DashboardFilterContextType {
  periodo: PeriodoDashboard;
  grupoParoquialId?: string;
  setPeriodo: (periodo: PeriodoDashboard) => void;
  setGrupoParoquialId: (id?: string) => void;
}

const DashboardFilterContext = createContext<DashboardFilterContextType | undefined>(undefined);

/**
 * Estado do filtro global do Painel (recorte temporal + comunidade). Vive em contexto porque
 * várias seções independentes da tela consomem o mesmo recorte e o repassam aos hooks de dados.
 */
export const useDashboardFilter = (): DashboardFilterContextType => {
  const context = useContext(DashboardFilterContext);
  if (!context) {
    throw new Error('useDashboardFilter deve ser usado dentro de DashboardFilterProvider');
  }
  return context;
};

export const DashboardFilterProvider = ({children}: {children: ReactNode}) => {
  const [periodo, setPeriodo] = useState<PeriodoDashboard>('SEMANA');
  const [grupoParoquialId, setGrupoParoquialId] = useState<string | undefined>(undefined);

  const value = useMemo(
    () => ({periodo, grupoParoquialId, setPeriodo, setGrupoParoquialId}),
    [periodo, grupoParoquialId],
  );

  return <DashboardFilterContext.Provider value={value}>{children}</DashboardFilterContext.Provider>;
};
