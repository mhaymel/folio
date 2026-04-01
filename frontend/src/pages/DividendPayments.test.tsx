import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithRouter } from '../test/test-utils';
import DividendPayments from './DividendPayments';
import type { DividendPaymentPaginatedResponse, DividendPaymentFiltersDto } from '../types';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

vi.mock('../components/ExportButtons', () => ({
  default: () => <div data-testid="export-buttons" />,
}));

import api from '../api/client';

const mockResponse: DividendPaymentPaginatedResponse = {
  items: [
    { id: 1, timestamp: '15.03.2026', isin: 'DE000BASF111', name: 'BASF SE', depot: 'DeGiro', value: 34.0 },
    { id: 2, timestamp: '20.06.2026', isin: 'US0378331005', name: 'Apple Inc.', depot: 'ZERO', value: 10.5 },
    { id: 3, timestamp: '25.09.2026', isin: 'DE000BASF111', name: 'BASF SE', depot: 'DeGiro', value: 34.0 },
  ],
  page: 1,
  pageSize: 10,
  totalItems: 3,
  totalPages: 1,
  sumValue: 78.5,
};

const mockFilters: DividendPaymentFiltersDto = {
  depots: ['DeGiro', 'ZERO'],
};

beforeEach(() => {
  sessionStorage.clear();
  vi.mocked(api.get).mockImplementation((url: string) => {
    if (url === '/dividend-payments/filters') return Promise.resolve({ data: mockFilters });
    return Promise.resolve({ data: mockResponse });
  });
});

describe('DividendPayments', () => {
  it('shows loading state', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    renderWithRouter(<DividendPayments />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders all column headers', async () => {
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByText('Date')).toBeInTheDocument();
    });
    expect(screen.getByText('ISIN')).toBeInTheDocument();
    expect(screen.getByText('Name')).toBeInTheDocument();
    expect(screen.getByText('Depot')).toBeInTheDocument();
    expect(screen.getByText('Amount (EUR)')).toBeInTheDocument();
  });

  it('renders dividend payment data', async () => {
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByText('15.03.2026')).toBeInTheDocument();
    });
    expect(screen.getAllByText('DE000BASF111').length).toBeGreaterThanOrEqual(1);
  });

  it('shows dividend payment count', async () => {
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByText('3 dividend payments')).toBeInTheDocument();
    });
  });

  it('shows sum of values', async () => {
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByText(/Total:/)).toBeInTheDocument();
    });
    expect(screen.getByText(/78,50 EUR/)).toBeInTheDocument();
  });

  it('fetches data with default sort, pagination, and 2026 date filter', async () => {
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/dividend-payments', {
        params: { sortField: 'timestamp', sortDir: 'desc', page: 1, pageSize: 10, fromDate: '2026-01-01', toDate: '2026-12-31' },
      });
    });
  });

  it('fetches filter options on mount', async () => {
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/dividend-payments/filters');
    });
  });

  it('filters by ISIN when typing in filter input', async () => {
    const user = userEvent.setup();
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByPlaceholderText('Filter ISIN...')).toBeInTheDocument();
    });

    await user.type(screen.getByPlaceholderText('Filter ISIN...'), 'DE000');
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/dividend-payments', {
        params: expect.objectContaining({ isin: expect.stringContaining('D') }),
      });
    });
  });

  it('renders multi-select depot dropdown', async () => {
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByText('All depots')).toBeInTheDocument();
    });
  });

  it('filters by depot when selecting from multi-select', async () => {
    const user = userEvent.setup();
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByText('All depots')).toBeInTheDocument();
    });

    const listbox = screen.getByRole('listbox', { name: 'All depots' });
    await user.selectOptions(listbox, ['DeGiro']);

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/dividend-payments', {
        params: expect.objectContaining({ depot: 'DeGiro' }),
      });
    });
  });

  it('clears filters when Clear button clicked and resets to no date filter', async () => {
    const user = userEvent.setup();
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByText('Date')).toBeInTheDocument();
    });

    // Find the page-level Clear button (not the TimeframeSelector's clear)
    const buttons = screen.getAllByRole('button');
    const clearBtn = buttons.find(b => b.textContent === 'Clear' && b.getAttribute('variant') === 'emphasized');
    await user.click(clearBtn!);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/dividend-payments', {
        params: { sortField: 'timestamp', sortDir: 'desc', page: 1, pageSize: 10 },
      });
    });
  });

  it('shows export buttons', async () => {
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByTestId('export-buttons')).toBeInTheDocument();
    });
  });

  it('renders page-size selector with options', async () => {
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByDisplayValue('10')).toBeInTheDocument();
    });
    const pageSizeSelect = screen.getByDisplayValue('10');
    const options = pageSizeSelect.querySelectorAll('option');
    const values = Array.from(options).map(o => o.value);
    expect(values).toEqual(['10', '20', '50', '100']);
  });

  it('changes page size and resets to page 1', async () => {
    const user = userEvent.setup();
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByDisplayValue('10')).toBeInTheDocument();
    });

    await user.selectOptions(screen.getByDisplayValue('10'), '50');
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/dividend-payments', {
        params: expect.objectContaining({ pageSize: 50, page: 1 }),
      });
    });
  });

  it('has correct column alignment', async () => {
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByText('Date')).toBeInTheDocument();
    });
    expect(screen.getByText('Date').getAttribute('data-alignment')).toBe('center');
    expect(screen.getByText('ISIN').getAttribute('data-alignment')).toBe('left');
    expect(screen.getByText('Name').getAttribute('data-alignment')).toBe('left');
    expect(screen.getByText('Depot').getAttribute('data-alignment')).toBe('left');
    expect(screen.getByText('Amount (EUR)').getAttribute('data-alignment')).toBe('right');
  });

  it('preserves filters in sessionStorage', async () => {
    const user = userEvent.setup();
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByPlaceholderText('Filter ISIN...')).toBeInTheDocument();
    });

    await user.type(screen.getByPlaceholderText('Filter ISIN...'), 'DE');

    const stored = JSON.parse(sessionStorage.getItem('dividend_payments_filters') || '{}');
    expect(stored.isin).toContain('D');
  });

  it('restores filters from sessionStorage on mount', async () => {
    sessionStorage.setItem('dividend_payments_filters', JSON.stringify({
      isin: 'US037', name: '', depots: [], fromDate: '', toDate: '',
    }));

    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/dividend-payments', {
        params: expect.objectContaining({ isin: 'US037' }),
      });
    });
  });

  it('sends comma-separated depot values for multi-select', async () => {
    sessionStorage.setItem('dividend_payments_filters', JSON.stringify({
      isin: '', name: '', depots: ['DeGiro', 'ZERO'], fromDate: '', toDate: '',
    }));

    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/dividend-payments', {
        params: expect.objectContaining({ depot: 'DeGiro,ZERO' }),
      });
    });
  });

  it('shows Refresh button', async () => {
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByText('Refresh')).toBeInTheDocument();
    });
  });

  it('renders TimeframeSelector with year presets', async () => {
    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(screen.getByTestId('timeframe-selector')).toBeInTheDocument();
    });
    const presets = screen.getAllByTestId('timeframe-preset');
    expect(presets.length).toBe(13);
    expect(presets[0].textContent).toBe('All');
    expect(presets[1].textContent).toBe('2026');
    expect(presets[12].textContent).toBe('2015');
  });

  it('sends date params from sessionStorage timeframe', async () => {
    sessionStorage.setItem('dividend_payments_filters', JSON.stringify({
      isin: '', name: '', depots: [],
      fromDate: '2025-01-01T00:00:00.000Z',
      toDate: '2025-12-31T23:59:59.999Z',
    }));

    renderWithRouter(<DividendPayments />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/dividend-payments', {
        params: expect.objectContaining({ fromDate: '2025-01-01', toDate: '2025-12-31' }),
      });
    });
  });
});