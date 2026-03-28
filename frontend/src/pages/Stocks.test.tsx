import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { renderWithRouter } from '../test/test-utils';
import Stocks from './Stocks';
import type { StockDto } from '../types';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

import api from '../api/client';

const mockStocks: StockDto[] = [
  {
    isin: 'US0378331005', name: 'Apple Inc.', country: 'United States', branch: 'Technology',
    totalShares: 10, avgEntryPrice: 150.5, currentQuote: 175.2,
    performancePercent: 16.41, dividendPerShare: 0.96, estimatedAnnualIncome: 9.6,
  },
  {
    isin: 'DE0007164600', name: 'SAP SE', country: 'Germany', branch: 'Technology',
    totalShares: 5, avgEntryPrice: 120.0, currentQuote: 180.0,
    performancePercent: 50.0, dividendPerShare: 2.2, estimatedAnnualIncome: 11.0,
  },
  {
    isin: 'AT0000730007', name: 'Andritz AG', country: 'Austria', branch: 'Industrials',
    totalShares: 20, avgEntryPrice: 45.0, currentQuote: 55.0,
    performancePercent: 22.22, dividendPerShare: 2.5, estimatedAnnualIncome: 50.0,
  },
];

beforeEach(() => {
  vi.mocked(api.get).mockResolvedValue({ data: mockStocks });
});

describe('Stocks', () => {
  it('shows loading indicator while fetching', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    renderWithRouter(<Stocks />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders stocks heading', async () => {
    renderWithRouter(<Stocks />);
    await waitFor(() => {
      expect(screen.getByText('Stocks')).toBeInTheDocument();
    });
  });

  it('displays total stock count when unfiltered', async () => {
    renderWithRouter(<Stocks />);
    await waitFor(() => {
      expect(screen.getByText('3 stocks')).toBeInTheDocument();
    });
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

  it('renders stock data rows', async () => {
    renderWithRouter(<Stocks />);
    await waitFor(() => {
      expect(screen.getByText('US0378331005')).toBeInTheDocument();
    });
    expect(screen.getByText('Apple Inc.')).toBeInTheDocument();
    expect(screen.getByText('SAP SE')).toBeInTheDocument();
    expect(screen.getByText('Andritz AG')).toBeInTheDocument();
  });

  it('renders filter dropdowns and Refresh button', async () => {
    renderWithRouter(<Stocks />);
    await waitFor(() => {
      expect(screen.getByText('Refresh')).toBeInTheDocument();
    });
    // Dropdowns should have default "All countries" / "All branches" options
    expect(screen.getByText('All countries')).toBeInTheDocument();
    expect(screen.getByText('All branches')).toBeInTheDocument();
  });

  it('calls GET /stocks on mount', async () => {
    renderWithRouter(<Stocks />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/stocks');
    });
  });
});
