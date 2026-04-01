import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithRouter } from '../test/test-utils';
import StocksPerDepot from './StocksPerDepot';
import type { StockDto, StockFiltersDto, StockPaginatedResponse } from '../types';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

vi.mock('../components/ExportButtons', () => ({
  default: () => <div data-testid="export-buttons" />,
}));

import api from '../api/client';

const mockStocks: StockPaginatedResponse = {
  items: [
    { isin: 'DE000BASF111', name: 'BASF SE', country: 'Germany', branch: 'Chemicals', depot: 'DeGiro', count: 10, avgEntryPrice: 50.0, currentQuote: 55.0, performancePercent: 10.0, dividendPerShare: 3.4, estimatedAnnualIncome: 34.0 },
    { isin: 'US0378331005', name: 'Apple Inc.', country: 'USA', branch: 'Technology', depot: 'ZERO', count: 5, avgEntryPrice: 150.0, currentQuote: 180.0, performancePercent: 20.0, dividendPerShare: 0.96, estimatedAnnualIncome: 4.8 },
    { isin: 'JP3633400001', name: 'Toyota Motor', country: 'Japan', branch: 'Automotive', depot: 'DeGiro', count: 20, avgEntryPrice: 15.0, currentQuote: null, performancePercent: null, dividendPerShare: null, estimatedAnnualIncome: null },
  ],
  page: 1,
  pageSize: 10,
  totalItems: 3,
  totalPages: 1,
  sumCount: 35.0,
};

const mockFilters: StockFiltersDto = {
  countries: ['Germany', 'Japan', 'USA'],
  branches: ['Automotive', 'Chemicals', 'Technology'],
  depots: ['DeGiro', 'ZERO'],
};

beforeEach(() => {
  sessionStorage.clear();
  vi.mocked(api.get).mockImplementation((url: string) => {
    if (url === '/stocks-per-depot/filters') return Promise.resolve({ data: mockFilters });
    return Promise.resolve({ data: mockStocks });
  });
});

describe('StocksPerDepot', () => {
  it('shows loading state', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    renderWithRouter(<StocksPerDepot />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders page heading', async () => {
    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(screen.getByText('Stocks per Depot')).toBeInTheDocument();
    });
  });

  it('renders all column headers including Depot', async () => {
    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(screen.getByText('ISIN')).toBeInTheDocument();
    });
    expect(screen.getByText('Name')).toBeInTheDocument();
    expect(screen.getByText('Depot')).toBeInTheDocument();
    expect(screen.getByText('Country')).toBeInTheDocument();
    expect(screen.getByText('Branch')).toBeInTheDocument();
    expect(screen.getByText('Count')).toBeInTheDocument();
    expect(screen.getByText('Avg Entry Price')).toBeInTheDocument();
    expect(screen.getByText('Current Quote')).toBeInTheDocument();
    expect(screen.getByText('Performance (%)')).toBeInTheDocument();
    expect(screen.getByText('Expected Dividend/Share')).toBeInTheDocument();
    expect(screen.getByText('Est. Annual Income')).toBeInTheDocument();
  });

  it('renders columns in correct order (country/branch after currentQuote)', async () => {
    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(screen.getByText('ISIN')).toBeInTheDocument();
    });
    const headers = screen.getAllByRole('columnheader');
    const headerTexts = headers.map(h => h.textContent?.replace(/ [↑↓]/, ''));
    const depotIdx = headerTexts.indexOf('Depot');
    const countIdx = headerTexts.indexOf('Count');
    const quoteIdx = headerTexts.indexOf('Current Quote');
    const countryIdx = headerTexts.indexOf('Country');
    const branchIdx = headerTexts.indexOf('Branch');
    // Country and Branch should come after Current Quote
    expect(countryIdx).toBeGreaterThan(quoteIdx);
    expect(branchIdx).toBeGreaterThan(quoteIdx);
    // Depot should come before Count
    expect(depotIdx).toBeLessThan(countIdx);
  });

  it('renders stock data', async () => {
    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(screen.getByText('DE000BASF111')).toBeInTheDocument();
    });
    expect(screen.getByText('BASF SE')).toBeInTheDocument();
    expect(screen.getByText('Apple Inc.')).toBeInTheDocument();
  });

  it('shows stock count', async () => {
    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(screen.getByText('3 stocks')).toBeInTheDocument();
    });
  });

  it('shows sum of count', async () => {
    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(screen.getByText(/Sum:/)).toBeInTheDocument();
    });
    expect(screen.getByText(/35,00/)).toBeInTheDocument();
  });

  it('renders multi-select filter dropdowns including depot', async () => {
    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(screen.getByText('All countries')).toBeInTheDocument();
    });
    expect(screen.getByText('All branches')).toBeInTheDocument();
    expect(screen.getByText('All depots')).toBeInTheDocument();
  });

  it('renders Refresh button', async () => {
    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(screen.getByText('Refresh')).toBeInTheDocument();
    });
  });

  it('fetches stocks with sort and pagination params', async () => {
    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/stocks-per-depot', {
        params: { sortField: 'isin', sortDir: 'asc', page: 1, pageSize: 10 },
      });
    });
  });

  it('fetches stock filters on mount', async () => {
    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/stocks-per-depot/filters');
    });
  });

  it('filters by country when selecting from multi-select', async () => {
    const user = userEvent.setup();
    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(screen.getByText('All countries')).toBeInTheDocument();
    });

    const listbox = screen.getByRole('listbox', { name: 'All countries' });
    await user.selectOptions(listbox, ['Germany']);

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/stocks-per-depot', {
        params: expect.objectContaining({ country: 'Germany' }),
      });
    });
  });

  it('sends comma-separated values for multi-select filters', async () => {
    sessionStorage.setItem('stocks_per_depot_filters', JSON.stringify({
      isin: '', name: '', depots: ['DeGiro', 'ZERO'], countries: [], branches: [],
    }));

    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/stocks-per-depot', {
        params: expect.objectContaining({ depot: 'DeGiro,ZERO' }),
      });
    });
  });

  it('renders page-size selector with options', async () => {
    renderWithRouter(<StocksPerDepot />);
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
    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(screen.getByDisplayValue('10')).toBeInTheDocument();
    });

    await user.selectOptions(screen.getByDisplayValue('10'), '50');
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/stocks-per-depot', {
        params: expect.objectContaining({ pageSize: 50, page: 1 }),
      });
    });
  });

  it('has correct column alignment', async () => {
    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(screen.getByText('ISIN')).toBeInTheDocument();
    });
    // Text columns: left
    expect(screen.getByText('ISIN').getAttribute('data-alignment')).toBe('left');
    expect(screen.getByText('Name').getAttribute('data-alignment')).toBe('left');
    expect(screen.getByText('Country').getAttribute('data-alignment')).toBe('left');
    expect(screen.getByText('Branch').getAttribute('data-alignment')).toBe('left');
    expect(screen.getByText('Depot').getAttribute('data-alignment')).toBe('left');
    // Numeric columns: right
    expect(screen.getByText('Count').getAttribute('data-alignment')).toBe('right');
    expect(screen.getByText('Avg Entry Price').getAttribute('data-alignment')).toBe('right');
    expect(screen.getByText('Current Quote').getAttribute('data-alignment')).toBe('right');
    expect(screen.getByText('Performance (%)').getAttribute('data-alignment')).toBe('right');
    expect(screen.getByText('Expected Dividend/Share').getAttribute('data-alignment')).toBe('right');
    expect(screen.getByText('Est. Annual Income').getAttribute('data-alignment')).toBe('right');
  });

  it('preserves filters in sessionStorage', async () => {
    const user = userEvent.setup();
    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(screen.getByPlaceholderText('Filter ISIN...')).toBeInTheDocument();
    });

    await user.type(screen.getByPlaceholderText('Filter ISIN...'), 'DE');

    const stored = JSON.parse(sessionStorage.getItem('stocks_per_depot_filters') || '{}');
    expect(stored.isin).toContain('D');
  });

  it('restores filters from sessionStorage on mount', async () => {
    sessionStorage.setItem('stocks_per_depot_filters', JSON.stringify({
      isin: 'DE000', name: '', depots: [], countries: [], branches: [],
    }));

    renderWithRouter(<StocksPerDepot />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/stocks-per-depot', {
        params: expect.objectContaining({ isin: 'DE000' }),
      });
    });
  });
});
