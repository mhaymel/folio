import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithRouter } from '../test/test-utils';
import IsinNames from './IsinNames';
import type { IsinNameDto, PaginatedResponse } from '../types';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

vi.mock('../components/ExportButtons', () => ({
  default: () => <div data-testid="export-buttons" />,
}));

import api from '../api/client';

const mockResponse: PaginatedResponse<IsinNameDto> = {
  items: [
    { isin: 'DE000BASF111', name: 'BASF SE' },
    { isin: 'DE000BASF111', name: 'BASF' },
    { isin: 'US0378331005', name: 'Apple Inc.' },
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

describe('IsinNames', () => {
  it('shows loading state', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    renderWithRouter(<IsinNames />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders ISIN name data in table', async () => {
    renderWithRouter(<IsinNames />);
    await waitFor(() => {
      expect(screen.getByText('BASF SE')).toBeInTheDocument();
    });
    expect(screen.getByText('BASF')).toBeInTheDocument();
    expect(screen.getByText('Apple Inc.')).toBeInTheDocument();
  });

  it('renders all column headers', async () => {
    renderWithRouter(<IsinNames />);
    await waitFor(() => {
      expect(screen.getByText('ISIN')).toBeInTheDocument();
    });
    expect(screen.getByText('Name')).toBeInTheDocument();
  });

  it('shows item count', async () => {
    renderWithRouter(<IsinNames />);
    await waitFor(() => {
      expect(screen.getByText('3 ISIN names')).toBeInTheDocument();
    });
  });

  it('toggle Show All / Paginate', async () => {
    const user = userEvent.setup();
    renderWithRouter(<IsinNames />);
    await waitFor(() => {
      expect(screen.getByText('Show All')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Show All'));
    await waitFor(() => {
      expect(screen.getByText('Paginate')).toBeInTheDocument();
    });

    expect(api.get).toHaveBeenCalledWith('/isin-names', expect.objectContaining({
      params: expect.objectContaining({ pageSize: -1 }),
    }));
  });

  it('shows export buttons', async () => {
    renderWithRouter(<IsinNames />);
    await waitFor(() => {
      expect(screen.getByTestId('export-buttons')).toBeInTheDocument();
    });
  });

  it('fetches data with default sort params', async () => {
    renderWithRouter(<IsinNames />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/isin-names', {
        params: { sortField: 'name', sortDir: 'asc', page: 1, pageSize: 10 },
      });
    });
  });

  it('shows pagination controls when paginated', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockResponse, totalPages: 3 } });
    renderWithRouter(<IsinNames />);
    await waitFor(() => {
      expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
    });
  });

  it('navigates pages on button click', async () => {
    const user = userEvent.setup();
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockResponse, totalPages: 3 } });
    renderWithRouter(<IsinNames />);
    await waitFor(() => {
      expect(screen.getByText('Next')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Next'));
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/isin-names', {
        params: expect.objectContaining({ page: 2 }),
      });
    });
  });

  it('has correct column alignments', async () => {
    renderWithRouter(<IsinNames />);
    await waitFor(() => {
      expect(screen.getByText('ISIN')).toBeInTheDocument();
    });
    expect(screen.getByText('ISIN').getAttribute('data-alignment')).toBe('left');
    expect(screen.getByText('Name').getAttribute('data-alignment')).toBe('left');
  });

  it('toggles sort direction without unsorted state', async () => {
    const user = userEvent.setup();
    renderWithRouter(<IsinNames />);
    await waitFor(() => {
      expect(screen.getByText('Name')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Name'));
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/isin-names', {
        params: expect.objectContaining({ sortField: 'name', sortDir: 'desc' }),
      });
    });

    await user.click(screen.getByText('Name'));
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/isin-names', {
        params: expect.objectContaining({ sortField: 'name', sortDir: 'asc' }),
      });
    });
  });
});
