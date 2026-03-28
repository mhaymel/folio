import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithRouter } from '../test/test-utils';
import Countries from './Countries';
import type { Country, PaginatedResponse } from '../types';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

vi.mock('../components/ExportButtons', () => ({
  default: () => <div data-testid="export-buttons" />,
}));

import api from '../api/client';

const mockResponse: PaginatedResponse<Country> = {
  items: [
    { id: 1, name: 'Germany' },
    { id: 2, name: 'USA' },
    { id: 3, name: 'Japan' },
  ],
  page: 1,
  pageSize: 10,
  totalItems: 3,
  totalPages: 1,
};

beforeEach(() => {
  vi.mocked(api.get).mockResolvedValue({ data: mockResponse });
});

describe('Countries', () => {
  it('shows loading state', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    renderWithRouter(<Countries />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders country names in table', async () => {
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByText('Germany')).toBeInTheDocument();
    });
    expect(screen.getByText('USA')).toBeInTheDocument();
    expect(screen.getByText('Japan')).toBeInTheDocument();
  });

  it('shows item count', async () => {
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByText('3 countries')).toBeInTheDocument();
    });
  });

  it('toggle Show All / Paginate', async () => {
    const user = userEvent.setup();
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByText('Show All')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Show All'));
    await waitFor(() => {
      expect(screen.getByText('Paginate')).toBeInTheDocument();
    });

    // Verify API was called with pageSize=-1
    expect(api.get).toHaveBeenCalledWith('/countries', expect.objectContaining({
      params: expect.objectContaining({ pageSize: -1 }),
    }));
  });

  it('shows export buttons', async () => {
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByTestId('export-buttons')).toBeInTheDocument();
    });
  });

  it('fetches data with sort and pagination params', async () => {
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/countries', {
        params: { sortField: 'name', sortDir: 'asc', page: 1, pageSize: 10 },
      });
    });
  });

  it('shows pagination controls when paginated', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockResponse, totalPages: 3 } });
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
    });
    expect(screen.getByText('Next')).toBeInTheDocument();
    expect(screen.getByText('Previous')).toBeInTheDocument();
  });

  it('navigates pages on button click', async () => {
    const user = userEvent.setup();
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockResponse, totalPages: 3 } });
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByText('Next')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Next'));
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/countries', {
        params: expect.objectContaining({ page: 2 }),
      });
    });
  });

  it('renders page-size selector with Select.Option values', async () => {
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByDisplayValue('10')).toBeInTheDocument();
    });
    const pageSizeSelect = screen.getByDisplayValue('10');
    const options = pageSizeSelect.querySelectorAll('option');
    const values = Array.from(options).map(o => o.value);
    expect(values).toEqual(['10', '20', '50', '100']);
    const labels = Array.from(options).map(o => o.textContent);
    expect(labels).toEqual(['10', '20', '50', '100']);
  });

  it('changes page size and resets to page 1', async () => {
    const user = userEvent.setup();
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockResponse, totalPages: 3 } });
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByDisplayValue('10')).toBeInTheDocument();
    });

    await user.selectOptions(screen.getByDisplayValue('10'), '50');
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/countries', {
        params: expect.objectContaining({ pageSize: 50, page: 1 }),
      });
    });
  });

  it('has correct column alignment', async () => {
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByText('Country')).toBeInTheDocument();
    });
    expect(screen.getByText('Country').getAttribute('data-alignment')).toBe('left');
  });

  it('toggles sort direction without unsorted state', async () => {
    const user = userEvent.setup();
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByText('Country')).toBeInTheDocument();
    });

    // Default sort is asc on 'name'. Click header once -> desc
    await user.click(screen.getByText('Country'));
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/countries', {
        params: expect.objectContaining({ sortField: 'name', sortDir: 'desc' }),
      });
    });

    // Click again -> TanStack sends empty array (removal), handler toggles back to asc
    await user.click(screen.getByText('Country'));
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/countries', {
        params: expect.objectContaining({ sortField: 'name', sortDir: 'asc' }),
      });
    });
  });
});