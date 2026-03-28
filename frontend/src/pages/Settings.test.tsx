import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithRouter } from '../test/test-utils';
import Settings from './Settings';
import type { QuoteSettingsDto } from '../types';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
    post: vi.fn(),
  },
}));

// Need to also mock @dynatrace/strato-components/forms for Switch
vi.mock('@dynatrace/strato-components/forms', () => ({
  TextInput: ({ value, onChange, ...props }: any) => (
    <input type="text" value={value} onChange={(e: any) => onChange?.(e.target.value, e)} {...props} />
  ),
  Select: Object.assign(
    ({ children, value, onChange, ...props }: any) => (
      <select value={value ?? ''} onChange={(e: any) => onChange?.(e.target.value)} {...props}>
        {children}
      </select>
    ),
    {
      Content: ({ children }: any) => <>{children}</>,
      Option: ({ value, children }: any) => <option value={value}>{children}</option>,
    },
  ),
  Switch: ({ checked, onChange }: any) => (
    <input type="checkbox" role="switch" checked={checked} onChange={(e) => onChange?.(e.target.checked)} />
  ),
}));

import api from '../api/client';

const mockSettings: QuoteSettingsDto = {
  enabled: true,
  intervalMinutes: 60,
  lastFetchAt: '27.03.2026 10:00',
};

beforeEach(() => {
  vi.mocked(api.get).mockResolvedValue({ data: mockSettings });
  vi.mocked(api.put).mockResolvedValue({ data: {} });
  vi.mocked(api.post).mockResolvedValue({ data: 5 });
});

describe('Settings', () => {
  it('shows loading state initially', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    renderWithRouter(<Settings />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders settings page after loading', async () => {
    renderWithRouter(<Settings />);
    await waitFor(() => {
      expect(screen.getByText('Settings')).toBeInTheDocument();
    });
    expect(screen.getByText('Quote Fetching')).toBeInTheDocument();
  });

  it('shows enable toggle checked when enabled', async () => {
    renderWithRouter(<Settings />);
    await waitFor(() => {
      expect(screen.getByRole('switch')).toBeChecked();
    });
  });

  it('shows enable toggle unchecked when disabled', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockSettings, enabled: false } });
    renderWithRouter(<Settings />);
    await waitFor(() => {
      expect(screen.getByRole('switch')).not.toBeChecked();
    });
  });

  it('shows last fetch timestamp', async () => {
    renderWithRouter(<Settings />);
    await waitFor(() => {
      expect(screen.getByText(/Last fetch:/)).toBeInTheDocument();
    });
    expect(screen.getByText(/27\.03\.2026 10:00/)).toBeInTheDocument();
  });

  it('shows dash when no last fetch', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockSettings, lastFetchAt: null } });
    renderWithRouter(<Settings />);
    await waitFor(() => {
      expect(screen.getByText(/Last fetch:.*\u2014/)).toBeInTheDocument();
    });
  });

  it('renders Save and Fetch Now buttons', async () => {
    renderWithRouter(<Settings />);
    await waitFor(() => {
      expect(screen.getByText('Save')).toBeInTheDocument();
    });
    expect(screen.getByText('Fetch Now')).toBeInTheDocument();
  });

  it('triggers fetch and shows result', async () => {
    const user = userEvent.setup();
    renderWithRouter(<Settings />);
    await waitFor(() => {
      expect(screen.getByText('Fetch Now')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Fetch Now'));
    await waitFor(() => {
      expect(screen.getByText('Fetched 5 quotes')).toBeInTheDocument();
    });
    expect(api.post).toHaveBeenCalledWith('/quotes/fetch');
  });
});