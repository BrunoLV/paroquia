import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect } from 'vitest';
import PoliticaPrivacidadePage from './PoliticaPrivacidadePage';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';

describe('PoliticaPrivacidadePage', () => {
  it('deve renderizar a política de privacidade paroquial com diretrizes LGPD e contatos do DPO', () => {
    render(
      <QueryProvider>
        <MemoryRouter>
          <PoliticaPrivacidadePage />
        </MemoryRouter>
      </QueryProvider>
    );

    expect(screen.getByText(/Política de Privacidade e Proteção de Dados/i)).toBeInTheDocument();
    expect(screen.getByText(/DPO Paroquial/i)).toBeInTheDocument();
    expect(screen.getByText(/privacidade@nsfatima\.org\.br/i)).toBeInTheDocument();
    expect(screen.getAllByText(/Vedação Comercial Absoluta/i)[0]).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Acessar Portal de Consentimento/i })).toBeInTheDocument();
  });
});
