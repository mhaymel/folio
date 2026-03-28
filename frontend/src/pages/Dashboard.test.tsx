import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { renderWithRouter } from '../test/test-utils';
import Dashboard from './Dashboard';
import type { DashboardDto } from '../types';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

vi.mock('../components/ExportButtons', () => ({
  default: () => <div data-testid="export-buttons" />,
}));

import api from '../api/client';

const mockDashboard: DashboardDto = {
  totalPortfolioValue: 12345.67,
  stockCount: 5,
  totalDividendRatio: 3.5,
  top5Holdings: [
    { isin: 'DE000BASF111', name: 'BASF SE', investedAmount: 5000.0 },
    { isin: 'US0378331005', name: 'Apple Inc.', investedAmount: 3000.0 },
  ],
  top5DividendSources: [
    { isin: 'DE000BASF111', name: 'BASF SE', estimatedAnnualIncome: 340.0 },
  ],
  lastQuoteFetchAt: '27.03.2026 14:30',
};

beforeEach(() => {
  vi.mocked(api.get).mockResolvedValue({ data: mockDashboard });
});

describe('Dashboard', () => {
  it('shows loading state initially', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    renderWithRouter(<Dashboard />);
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders KPI cards', async () => {
    renderWithRouter(<Dashboard />);
    await waitFor(() => {
      expect(screen.getByText('Portfolio Value')).toBeInTheDocument();
    });
    expect(screen.getByText('Stock Count')).toBeInTheDocument();
    expect(screen.getByText('Dividend Ratio')).toBeInTheDocument();
  });

  it('renders top 5 holdings table', async () => {
    renderWithRouter(<Dashboard />);
    await waitFor(() => {
      expect(screen.getByText('Top 5 Holdings')).toBeInTheDocument();
    });
    expect(screen.getAllByText('DE000BASF111').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('BASF SE').length).toBeGreaterThanOrEqual(1);
  });

  it('renders top 5 dividend sources table', async () => {
    renderWithRouter(<Dashboard />);
    await waitFor(() => {
      expect(screen.getByText('Top 5 Dividend Sources')).toBeInTheDocument();
    });
  });

  it('shows formatted last fetch timestamp', async () => {
    renderWithRouter(<Dashboard />);
    await waitFor(() => {
      expect(screen.getByText(/27\.03\.2026 14:30/)).toBeInTheDocument();
    });
  });

  it('shows dash when no last fetch timestamp', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockDashboard, lastQuoteFetchAt: null } });
    renderWithRouter(<Dashboard />);
    await waitFor(() => {
      expect(screen.getByText(/Last quote fetch:.*\u2014/)).toBeInTheDocument();
    });
  });

  it('calls GET /dashboard on mount', async () => {
    renderWithRouter(<Dashboard />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/dashboard');
    });
  });
});