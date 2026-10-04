import React from 'react';
import { useDynamicFavicon } from '@/shared/presentation/hooks/useDynamicFavicon';

/**
 * Componente declarativo sem renderização visual que acopla o hook useDynamicFavicon
 * ao ciclo de vida da aplicação dentro de um contexto com QueryClient ativo.
 */
export const DynamicFavicon: React.FC = () => {
  useDynamicFavicon();
  return null;
};

export default DynamicFavicon;
