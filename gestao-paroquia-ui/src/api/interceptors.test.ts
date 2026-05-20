import apiClient from './client';

// Simple mock for window.location
const originalLocation = window.location;
delete (window as any).location;
(window as any).location = { ...originalLocation, href: '' };

test('apiClient should have withCredentials enabled', () => {
  expect(apiClient.defaults.withCredentials).toBe(true);
});

test('apiClient should not have Authorization request interceptor', () => {
  // In the implementation, we will remove the interceptor.
  // We can check if any request interceptor exists.
  // The current implementation has 1 request interceptor.
  // We want 0 for now (unless we add others later).
  expect((apiClient.interceptors.request as any).handlers.length).toBe(0);
});

test('apiClient response interceptor handles 401', async () => {
  // Mock a 401 error response
  const error = {
    response: {
      status: 401,
    },
  };

  // Trigger the response interceptor
  const handlers = (apiClient.interceptors.response as any).handlers;
  const onResponseError = handlers[handlers.length - 1].rejected;
  
  // Set something in localStorage to check if it's removed
  localStorage.setItem('user', 'some-user');

  try {
    await onResponseError(error);
  } catch (e) {
    expect(localStorage.getItem('user')).toBeNull();
    // Verify that window.location.href has NOT been changed to '/login'
    expect(window.location.href).not.toBe('/login');
  }
});
