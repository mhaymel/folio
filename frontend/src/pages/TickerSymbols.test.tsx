import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithRouter } from '../test/test-utils';
import TickerSymbols from './TickerSymbols';
import type { TickerSymbolDto, PaginatedResponse } from '../types';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

vi.mock('../components/ExportButtons', () => ({
  default: () => <div data-testid="export-buttons" />,
}));

import api from '../api/client';

const mockResponse: PaginatedResponse<TickerSymbolDto> = {
  items: [
    { isin: 'DE000BASF111', tickerSymbol: 'BASF.DE', name: 'BASF SE' },
    { isin: 'IE00B4L5Y983', tickerSymbol: 'IWDA.AS', name: 'iShares Core MSCI World ETF' },
    { isin: 'US0378331005', tickerSymbol: 'AAPL', name: 'Apple Inc.' },
  ],
  page: 1,
  pageSize: 10,
  totalItems: 3,
  totalPages: 1,
};

beforeEach(() => {
  sessionStorage.clear();
  vi.mocked(api.get).mockResolvedValue({ data: mockResponse });
});

describe('TickerSymbols', () => {
  it('shows loading state', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    renderWithRouter(<TickerSymbols />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders ticker symbol data in table', async () => {
    renderWithRouter(<TickerSymbols />);
    await waitFor(() => {
      expect(screen.getByText('DE000BASF111')).toBeInTheDocument();
    });
    expect(screen.getByText('BASF.DE')).toBeInTheDocument();
    expect(screen.getByText('BASF SE')).toBeInTheDocument();
    expect(screen.getByText('AAPL')).toBeInTheDocument();
  });

  it('renders all column headers', async () => {
    renderWithRouter(<TickerSymbols />);
    await waitFor(() => {
      expect(within(screen.getByTestId('data-table')).getByText('ISIN')).toBeInTheDocument();
    });
    const table = screen.getByTestId('data-table');
    expect(within(table).getByText('Ticker Symbol')).toBeInTheDocument();
    expect(within(table).getByText('Name')).toBeInTheDocument();
  });

  it('shows item count', async () => {
    renderWithRouter(<TickerSymbols />);
    await waitFor(() => {
      expect(screen.getByText('3 ticker symbols')).toBeInTheDocument();
    });
  });

  it('toggle Show All / Paginate', async () => {
    const user = userEvent.setup();
    renderWithRouter(<TickerSymbols />);
    await waitFor(() => {
      expect(screen.getByText('Show All')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Show All'));
    await waitFor(() => {
      expect(screen.getByText('Paginate')).toBeInTheDocument();
    });

    expect(api.get).toHaveBeenCalledWith('/ticker-symbols', expect.objectContaining({
      params: expect.objectContaining({ pageSize: -1 }),
    }));
  });

  it('shows export buttons', async () => {
    renderWithRouter(<TickerSymbols />);
    await waitFor(() => {
      expect(screen.getByTestId('export-buttons')).toBeInTheDocument();
    });
  });

  it('fetches data with default sort params', async () => {
    renderWithRouter(<TickerSymbols />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/ticker-symbols', {
        params: { sortField: 'isin', sortDir: 'asc', page: 1, pageSize: 10 },
      });
    });
  });

  it('shows pagination controls when paginated', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockResponse, totalPages: 3 } });
    renderWithRouter(<TickerSymbols />);
    await waitFor(() => {
      expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
    });
  });

  it('navigates pages on button click', async () => {
    const user = userEvent.setup();
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockResponse, totalPages: 3 } });
    renderWithRouter(<TickerSymbols />);
    await waitFor(() => {
      expect(screen.getByText('Next')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Next'));
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/ticker-symbols', {
        params: expect.objectContaining({ page: 2 }),
      });
    });
  });

  it('has correct column alignments', async () => {
    renderWithRouter(<TickerSymbols />);
    await waitFor(() => {
      expect(within(screen.getByTestId('data-table')).getByText('ISIN')).toBeInTheDocument();
    });
    const table = screen.getByTestId('data-table');
    expect(within(table).getByText('ISIN').getAttribute('data-alignment')).toBe('left');
    expect(within(table).getByText('Ticker Symbol').getAttribute('data-alignment')).toBe('left');
    expect(within(table).getByText('Name').getAttribute('data-alignment')).toBe('left');
  });

  it('toggles sort direction without unsorted state', async () => {
    const user = userEvent.setup();
    renderWithRouter(<TickerSymbols />);
    await waitFor(() => {
      expect(within(screen.getByTestId('data-table')).getByText('ISIN')).toBeInTheDocument();
    });
    const isinHeader = within(screen.getByTestId('data-table')).getByText('ISIN');

    await user.click(isinHeader);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/ticker-symbols', {
        params: expect.objectContaining({ sortField: 'isin', sortDir: 'desc' }),
      });
    });

    await user.click(isinHeader);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/ticker-symbols', {
        params: expect.objectContaining({ sortField: 'isin', sortDir: 'asc' }),
      });
    });
  });
});
