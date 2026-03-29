import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router';
import Analytics from './Analytics';
import type { DiversificationEntry, PaginatedResponse } from '../types';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

vi.mock('../components/ExportButtons', () => ({
  default: () => <div data-testid="export-buttons" />,
}));

vi.mock('recharts', () => ({
  PieChart: ({ children }: any) => <div data-testid="pie-chart">{children}</div>,
  Pie: () => null,
  Cell: () => null,
  Legend: () => null,
  Tooltip: () => null,
  ResponsiveContainer: ({ children }: any) => <div>{children}</div>,
}));

import api from '../api/client';

const mockResponse: PaginatedResponse<DiversificationEntry> = {
  items: [
    { name: 'Germany', investedAmount: 5000.0, percentage: 50.0 },
    { name: 'USA', investedAmount: 3000.0, percentage: 30.0 },
    { name: 'Japan', investedAmount: 2000.0, percentage: 20.0 },
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

describe('Analytics', () => {
  const renderAnalytics = (type = 'countries') =>
    render(
      <MemoryRouter initialEntries={[`/analytics/${type}`]}>
        <Routes>
          <Route path="/analytics/:type" element={<Analytics />} />
        </Routes>
      </MemoryRouter>,
    );

  it('shows loading state', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    renderAnalytics();
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders country diversification heading', async () => {
    renderAnalytics('countries');
    await waitFor(() => {
      expect(screen.getByText('Country Diversification')).toBeInTheDocument();
    });
  });

  it('renders diversification data in table', async () => {
    renderAnalytics();
    await waitFor(() => {
      expect(screen.getByText('Germany')).toBeInTheDocument();
    });
    expect(screen.getByText('USA')).toBeInTheDocument();
    expect(screen.getByText('Japan')).toBeInTheDocument();
  });

  it('renders pie chart', async () => {
    renderAnalytics();
    await waitFor(() => {
      expect(screen.getByTestId('pie-chart')).toBeInTheDocument();
    });
  });

  it('shows item count', async () => {
    renderAnalytics();
    await waitFor(() => {
      expect(screen.getByText('3 entries')).toBeInTheDocument();
    });
  });

  it('toggle Show All / Paginate', async () => {
    const user = userEvent.setup();
    renderAnalytics();
    await waitFor(() => {
      expect(screen.getByText('Show All')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Show All'));
    await waitFor(() => {
      expect(screen.getByText('Paginate')).toBeInTheDocument();
    });
  });

  it('shows export buttons', async () => {
    renderAnalytics();
    await waitFor(() => {
      expect(screen.getByTestId('export-buttons')).toBeInTheDocument();
    });
  });

  it('fetches data with default sort params', async () => {
    renderAnalytics();
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/analytics/countries', {
        params: { sortField: 'investedAmount', sortDir: 'desc', page: 1, pageSize: 10 },
      });
    });
  });

  it('fetches all entries for chart', async () => {
    renderAnalytics();
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/analytics/countries', {
        params: { sortField: 'investedAmount', sortDir: 'desc', pageSize: -1 },
      });
    });
  });

  it('shows pagination controls when paginated', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockResponse, totalPages: 3 } });
    renderAnalytics();
    await waitFor(() => {
      expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
    });
  });

  it('formats invested amounts with EUR', async () => {
    renderAnalytics();
    await waitFor(() => {
      expect(screen.getByText('5.000,00 EUR')).toBeInTheDocument();
    });
  });

  it('formats percentages', async () => {
    renderAnalytics();
    await waitFor(() => {
      expect(screen.getByText('50,00 %')).toBeInTheDocument();
    });
  });
});
