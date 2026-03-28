import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithRouter } from '../test/test-utils';
import Stocks from './Stocks';
import type { StockDto, StockFiltersDto, PaginatedResponse } from '../types';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

vi.mock('../components/ExportButtons', () => ({
  default: () => <div data-testid="export-buttons" />,
}));

import api from '../api/client';

const mockStocks: PaginatedResponse<StockDto> = {
  items: [
    { isin: 'DE000BASF111', name: 'BASF SE', country: 'Germany', branch: 'Chemicals', totalShares: 10, avgEntryPrice: 50.0, currentQuote: 55.0, performancePercent: 10.0, dividendPerShare: 3.4, estimatedAnnualIncome: 34.0 },
    { isin: 'US0378331005', name: 'Apple Inc.', country: 'USA', branch: 'Technology', totalShares: 5, avgEntryPrice: 150.0, currentQuote: 180.0, performancePercent: 20.0, dividendPerShare: 0.96, estimatedAnnualIncome: 4.8 },
    { isin: 'JP3633400001', name: 'Toyota Motor', country: 'Japan', branch: 'Automotive', totalShares: 20, avgEntryPrice: 15.0, currentQuote: null, performancePercent: null, dividendPerShare: null, estimatedAnnualIncome: null },
  ],
  page: 1,
  pageSize: 10,
  totalItems: 3,
  totalPages: 1,
};

const mockFilters: StockFiltersDto = {
  countries: ['Germany', 'Japan', 'USA'],
  branches: ['Automotive', 'Chemicals', 'Technology'],
};

beforeEach(() => {
  vi.mocked(api.get).mockImplementation((url: string) => {
    if (url === '/stocks/filters') return Promise.resolve({ data: mockFilters });
    return Promise.resolve({ data: mockStocks });
  });
});

describe('Stocks', () => {
  it('shows loading state', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    renderWithRouter(<Stocks />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders all column headers', async () => {
    renderWithRouter(<Stocks />);
    await waitFor(() => {
      expect(screen.getByText('ISIN')).toBeInTheDocument();
    });
    expect(screen.getByText('Name')).toBeInTheDocument();
    expect(screen.getByText('Country')).toBeInTheDocument();
    expect(screen.getByText('Branch')).toBeInTheDocument();
    expect(screen.getByText('Total Shares')).toBeInTheDocument();
    expect(screen.getByText('Avg Entry Price')).toBeInTheDocument();
    expect(screen.getByText('Current Quote')).toBeInTheDocument();
    expect(screen.getByText('Performance (%)')).toBeInTheDocument();
    expect(screen.getByText('Expected Dividend/Share')).toBeInTheDocument();
    expect(screen.getByText('Est. Annual Income')).toBeInTheDocument();
  });

  it('renders stock data', async () => {
    renderWithRouter(<Stocks />);
    await waitFor(() => {
      expect(screen.getByText('DE000BASF111')).toBeInTheDocument();
    });
    expect(screen.getByText('BASF SE')).toBeInTheDocument();
    expect(screen.getByText('Apple Inc.')).toBeInTheDocument();
  });

  it('shows stock count', async () => {
    renderWithRouter(<Stocks />);
    await waitFor(() => {
      expect(screen.getByText('3 stocks')).toBeInTheDocument();
    });
  });

  it('renders filter dropdowns', async () => {
    renderWithRouter(<Stocks />);
    await waitFor(() => {
      expect(screen.getByText('All countries')).toBeInTheDocument();
    });
    expect(screen.getByText('All branches')).toBeInTheDocument();
  });

  it('renders Refresh button', async () => {
    renderWithRouter(<Stocks />);
    await waitFor(() => {
      expect(screen.getByText('Refresh')).toBeInTheDocument();
    });
  });

  it('fetches stocks with sort and pagination params', async () => {
    renderWithRouter(<Stocks />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/stocks', {
        params: { sortField: 'isin', sortDir: 'asc', page: 1, pageSize: 10 },
      });
    });
  });

  it('fetches stock filters on mount', async () => {
    renderWithRouter(<Stocks />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/stocks/filters');
    });
  });

  it('renders page-size selector with options', async () => {
    renderWithRouter(<Stocks />);
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
    renderWithRouter(<Stocks />);
    await waitFor(() => {
      expect(screen.getByDisplayValue('10')).toBeInTheDocument();
    });

    await user.selectOptions(screen.getByDisplayValue('10'), '50');
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/stocks', {
        params: expect.objectContaining({ pageSize: 50, page: 1 }),
      });
    });
  });

  it('has correct column alignment', async () => {
    renderWithRouter(<Stocks />);
    await waitFor(() => {
      expect(screen.getByText('ISIN')).toBeInTheDocument();
    });
    // Text columns: left
    expect(screen.getByText('ISIN').getAttribute('data-alignment')).toBe('left');
    expect(screen.getByText('Name').getAttribute('data-alignment')).toBe('left');
    expect(screen.getByText('Country').getAttribute('data-alignment')).toBe('left');
    expect(screen.getByText('Branch').getAttribute('data-alignment')).toBe('left');
    // Numeric columns: right
    expect(screen.getByText('Total Shares').getAttribute('data-alignment')).toBe('right');
    expect(screen.getByText('Avg Entry Price').getAttribute('data-alignment')).toBe('right');
    expect(screen.getByText('Current Quote').getAttribute('data-alignment')).toBe('right');
    expect(screen.getByText('Performance (%)').getAttribute('data-alignment')).toBe('right');
    expect(screen.getByText('Expected Dividend/Share').getAttribute('data-alignment')).toBe('right');
    expect(screen.getByText('Est. Annual Income').getAttribute('data-alignment')).toBe('right');
  });
});