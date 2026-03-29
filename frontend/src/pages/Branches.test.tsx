import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithRouter } from '../test/test-utils';
import Branches from './Branches';
import type { Branch, PaginatedResponse } from '../types';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

vi.mock('../components/ExportButtons', () => ({
  default: () => <div data-testid="export-buttons" />,
}));

import api from '../api/client';

const mockResponse: PaginatedResponse<Branch> = {
  items: [
    { id: 1, name: 'Energy' },
    { id: 2, name: 'Finance' },
    { id: 3, name: 'Technology' },
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

describe('Branches', () => {
  it('shows loading state', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    renderWithRouter(<Branches />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders branch names in table', async () => {
    renderWithRouter(<Branches />);
    await waitFor(() => {
      expect(screen.getByText('Energy')).toBeInTheDocument();
    });
    expect(screen.getByText('Finance')).toBeInTheDocument();
    expect(screen.getByText('Technology')).toBeInTheDocument();
  });

  it('shows item count', async () => {
    renderWithRouter(<Branches />);
    await waitFor(() => {
      expect(screen.getByText('3 branches')).toBeInTheDocument();
    });
  });

  it('toggle Show All / Paginate', async () => {
    const user = userEvent.setup();
    renderWithRouter(<Branches />);
    await waitFor(() => {
      expect(screen.getByText('Show All')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Show All'));
    await waitFor(() => {
      expect(screen.getByText('Paginate')).toBeInTheDocument();
    });

    expect(api.get).toHaveBeenCalledWith('/branches', expect.objectContaining({
      params: expect.objectContaining({ pageSize: -1 }),
    }));
  });

  it('shows export buttons', async () => {
    renderWithRouter(<Branches />);
    await waitFor(() => {
      expect(screen.getByTestId('export-buttons')).toBeInTheDocument();
    });
  });

  it('fetches data with sort and pagination params', async () => {
    renderWithRouter(<Branches />);
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/branches', {
        params: { sortField: 'name', sortDir: 'asc', page: 1, pageSize: 10 },
      });
    });
  });

  it('shows pagination controls when paginated', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockResponse, totalPages: 3 } });
    renderWithRouter(<Branches />);
    await waitFor(() => {
      expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
    });
    expect(screen.getByText('Next')).toBeInTheDocument();
    expect(screen.getByText('Previous')).toBeInTheDocument();
  });

  it('navigates pages on button click', async () => {
    const user = userEvent.setup();
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockResponse, totalPages: 3 } });
    renderWithRouter(<Branches />);
    await waitFor(() => {
      expect(screen.getByText('Next')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Next'));
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/branches', {
        params: expect.objectContaining({ page: 2 }),
      });
    });
  });

  it('renders page-size selector with options', async () => {
    renderWithRouter(<Branches />);
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
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockResponse, totalPages: 3 } });
    renderWithRouter(<Branches />);
    await waitFor(() => {
      expect(screen.getByDisplayValue('10')).toBeInTheDocument();
    });

    await user.selectOptions(screen.getByDisplayValue('10'), '50');
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/branches', {
        params: expect.objectContaining({ pageSize: 50, page: 1 }),
      });
    });
  });

  it('has correct column alignment', async () => {
    renderWithRouter(<Branches />);
    await waitFor(() => {
      expect(screen.getByText('Branch')).toBeInTheDocument();
    });
    expect(screen.getByText('Branch').getAttribute('data-alignment')).toBe('left');
  });

  it('toggles sort direction without unsorted state', async () => {
    const user = userEvent.setup();
    renderWithRouter(<Branches />);
    await waitFor(() => {
      expect(screen.getByText('Branch')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Branch'));
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/branches', {
        params: expect.objectContaining({ sortField: 'name', sortDir: 'desc' }),
      });
    });

    await user.click(screen.getByText('Branch'));
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/branches', {
        params: expect.objectContaining({ sortField: 'name', sortDir: 'asc' }),
      });
    });
  });
});
