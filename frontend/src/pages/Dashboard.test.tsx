import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { renderWithRouter } from '../test/test-utils';
import Dashboard from './Dashboard';
import type { DashboardDto } from '../types';

const mockDashboard: DashboardDto = {
  totalPortfolioValue: 12345.67,
  stockCount: 42,
  totalDividendRatio: 3.14,
  top5Holdings: [
    { isin: 'IE00B4L5Y983', name: 'iShares Core MSCI World', investedAmount: 5000 },
    { isin: 'US0378331005', name: 'Apple Inc.', investedAmount: 3000 },
  ],
  top5DividendSources: [
    { isin: 'US0378331005', name: 'Apple Inc.', estimatedAnnualIncome: 120 },
  ],
  lastQuoteFetchAt: '2026-03-27T14:30:00',
};

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

import api from '../api/client';

beforeEach(() => {
  vi.mocked(api.get).mockResolvedValue({ data: mockDashboard });
});

describe('Dashboard', () => {
  it('shows loading state initially', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {})); // never resolves
    renderWithRouter(<Dashboard />);
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders KPI cards after loading', async () => {
    renderWithRouter(<Dashboard />);
    await waitFor(() => {
      expect(screen.getByText('Total Portfolio Value')).toBeInTheDocument();
    });
    expect(screen.getByText('Stocks')).toBeInTheDocument();
    expect(screen.getByText('Dividend Ratio')).toBeInTheDocument();
    expect(screen.getByText('42')).toBeInTheDocument();
  });

  it('renders top 5 holdings table', async () => {
    renderWithRouter(<Dashboard />);
    await waitFor(() => {
      expect(screen.getByText('Top 5 Holdings')).toBeInTheDocument();
    });
    expect(screen.getByText('IE00B4L5Y983')).toBeInTheDocument();
    expect(screen.getByText('iShares Core MSCI World')).toBeInTheDocument();
  });

  it('renders top 5 dividend sources table', async () => {
    renderWithRouter(<Dashboard />);
    await waitFor(() => {
      expect(screen.getByText('Top 5 Dividend Sources')).toBeInTheDocument();
    });
    // Apple Inc. appears in both tables; verify the dividend table section exists with data
    expect(screen.getAllByText('Apple Inc.')).toHaveLength(2); // one in holdings, one in dividends
    expect(screen.getByText('120,00')).toBeInTheDocument();
  });

  it('shows last updated timestamp in DD.MM.YYYY HH:mm format', async () => {
    renderWithRouter(<Dashboard />);
    await waitFor(() => {
      expect(screen.getByText(/Last updated:/)).toBeInTheDocument();
    });
    // The exact formatted date depends on timezone, but it should contain the format pattern
    expect(screen.getByText(/Last updated:/).textContent).toMatch(/Last updated: \d{2}\.\d{2}\.\d{4} \d{2}:\d{2}/);
  });

  it('shows dash when lastQuoteFetchAt is null', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: { ...mockDashboard, lastQuoteFetchAt: null },
    });
    renderWithRouter(<Dashboard />);
    await waitFor(() => {
      expect(screen.getByText(/Last updated:/)).toBeInTheDocument();
    });
    expect(screen.getByText('Last updated: —')).toBeInTheDocument();
  });

  it('calls GET /dashboard on mount', async () => {
    renderWithRouter(<Dashboard />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/dashboard');
    });
  });
});
