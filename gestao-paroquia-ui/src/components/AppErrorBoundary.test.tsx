import {render, screen} from '@testing-library/react';
import {vi} from 'vitest';
import AppErrorBoundary from '@/components/AppErrorBoundary';

const BrokenPage = (): never => {
  throw new Error('Falha simulada');
};

describe('AppErrorBoundary', () => {
  it('renders a safe recovery state when a route throws', () => {
    vi.spyOn(console, 'error').mockImplementation(() => undefined);

    render(
      <AppErrorBoundary>
        <BrokenPage />
      </AppErrorBoundary>
    );

    expect(screen.getByRole('heading', {name: 'Não foi possível carregar esta tela'})).toBeInTheDocument();
  });
});
