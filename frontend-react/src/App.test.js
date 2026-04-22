import { render, screen } from '@testing-library/react';
import App from './App';

jest.mock('./api', () => ({
  getAnonymousAuth: jest.fn(() => Promise.resolve({ token: 'token', userId: 'user-1' })),
  getJobs: jest.fn(() => Promise.resolve([])),
  uploadTextFile: jest.fn(),
  pushJob: jest.fn(),
  deleteJob: jest.fn(),
  downloadJobResult: jest.fn(),
}));

jest.mock('./websocket', () => ({
  createJobsSocket: jest.fn(() => ({
    connect: jest.fn(),
    disconnect: jest.fn(),
  })),
}));

test('renders dashboard title', async () => {
  localStorage.clear();
  render(<App />);
  const heading = await screen.findByText(/tts jobs dashboard/i);
  expect(heading).toBeInTheDocument();
});
