import React from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';
import { NotificationProvider, useNotification } from './NotificationContext';

const TestComponent = () => {
  const { showSuccess, showError } = useNotification();
  return (
    <div>
      <button onClick={() => showSuccess('Success message')}>Success Button</button>
      <button onClick={() => showError('Error message')}>Error Button</button>
    </div>
  );
};

describe('NotificationProvider', () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('renders children correctly', () => {
    render(
      <NotificationProvider>
        <div>Child content</div>
      </NotificationProvider>
    );
    expect(screen.getByText('Child content')).toBeInTheDocument();
  });

  it('shows success alert when showSuccess is called', () => {
    render(
      <NotificationProvider>
        <TestComponent />
      </NotificationProvider>
    );

    fireEvent.click(screen.getByText('Success Button'));
    
    const alert = screen.getByRole('alert');
    expect(alert).toBeInTheDocument();
    expect(alert).toHaveTextContent('Success message');
    expect(alert).toHaveClass('MuiAlert-colorSuccess');
  });

  it('shows error alert when showError is called', () => {
    render(
      <NotificationProvider>
        <TestComponent />
      </NotificationProvider>
    );

    fireEvent.click(screen.getByText('Error Button'));

    const alert = screen.getByRole('alert');
    expect(alert).toBeInTheDocument();
    expect(alert).toHaveTextContent('Error message');
    expect(alert).toHaveClass('MuiAlert-colorError');
  });

  it('closes automatically after timeout', () => {
    render(
      <NotificationProvider>
        <TestComponent />
      </NotificationProvider>
    );

    fireEvent.click(screen.getByText('Success Button'));
    const alert = screen.getByRole('alert');
    expect(alert).toBeInTheDocument();

    act(() => {
      jest.advanceTimersByTime(5000);
    });

    // Verify it becomes invisible or opacity becomes 0 due to transition
    expect(alert).not.toBeVisible();
  });
});
