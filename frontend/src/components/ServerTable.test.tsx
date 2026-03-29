import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithRouter } from '../test/test-utils';
import ServerTable from './ServerTable';
import type { PaginatedResponse } from '../types';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

vi.mock('./ExportButtons', () => ({
  default: ({ endpoint, params }: any) => (
    <div data-testid="export-buttons" data-endpoint={endpoint} data-params={JSON.stringify(params)} />
  ),
}));

import api from '../api/client';

interface TestItem {
  id: number;
  name: string;
}

const columns = [
  { id: 'name', header: 'Item Name', accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 300, minWidth: 200 },
];

const mockResponse: PaginatedResponse<TestItem> = {
  items: [
    { id: 1, name: 'Alpha' },
    { id: 2, name: 'Beta' },
    { id: 3, name: 'Gamma' },
  ],
  page: 1,
  pageSize: 10,
  totalItems: 3,
  totalPages: 1,
};

const renderTable = (props?: Partial<React.ComponentProps<typeof ServerTable<TestItem>>>) =>
  renderWithRouter(
    <ServerTable<TestItem>
      endpoint="/test-items"
      exportEndpoint="/test-items/export"
      columns={columns}
      defaultSortField="name"
      itemLabel="items"
      {...props}
    />,
  );

beforeEach(() => {
  sessionStorage.clear();
  vi.mocked(api.get).mockResolvedValue({ data: mockResponse });
});

describe('ServerTable', () => {
  it('shows loading state', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    renderTable();
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders data in table after loading', async () => {
    renderTable();
    await waitFor(() => {
      expect(screen.getByText('Alpha')).toBeInTheDocument();
    });
    expect(screen.getByText('Beta')).toBeInTheDocument();
    expect(screen.getByText('Gamma')).toBeInTheDocument();
  });

  it('renders column headers', async () => {
    renderTable();
    await waitFor(() => {
      expect(screen.getByText('Item Name')).toBeInTheDocument();
    });
  });

  it('shows item count with label', async () => {
    renderTable();
    await waitFor(() => {
      expect(screen.getByText('3 items')).toBeInTheDocument();
    });
  });

  it('uses custom item label', async () => {
    renderTable({ itemLabel: 'widgets' });
    await waitFor(() => {
      expect(screen.getByText('3 widgets')).toBeInTheDocument();
    });
  });

  it('fetches data with default sort and pagination params', async () => {
    renderTable();
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/test-items', {
        params: { sortField: 'name', sortDir: 'asc', page: 1, pageSize: 10 },
      });
    });
  });

  it('uses custom default sort direction', async () => {
    renderTable({ defaultSortDir: 'desc' });
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/test-items', {
        params: expect.objectContaining({ sortDir: 'desc' }),
      });
    });
  });

  it('shows export buttons with correct endpoint', async () => {
    renderTable();
    await waitFor(() => {
      const exportBtn = screen.getByTestId('export-buttons');
      expect(exportBtn.getAttribute('data-endpoint')).toBe('/test-items/export');
    });
  });

  it('toggle Show All / Paginate', async () => {
    const user = userEvent.setup();
    renderTable();
    await waitFor(() => {
      expect(screen.getByText('Show All')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Show All'));
    await waitFor(() => {
      expect(screen.getByText('Paginate')).toBeInTheDocument();
    });

    expect(api.get).toHaveBeenCalledWith('/test-items', expect.objectContaining({
      params: expect.objectContaining({ pageSize: -1 }),
    }));
  });

  it('hides pagination controls when showing all', async () => {
    const user = userEvent.setup();
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockResponse, totalPages: 3 } });
    renderTable();
    await waitFor(() => {
      expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Show All'));
    await waitFor(() => {
      expect(screen.queryByText(/Page \d+ of/)).not.toBeInTheDocument();
    });
  });

  it('shows pagination controls when paginated', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockResponse, totalPages: 3 } });
    renderTable();
    await waitFor(() => {
      expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
    });
    expect(screen.getByText('Next')).toBeInTheDocument();
    expect(screen.getByText('Previous')).toBeInTheDocument();
  });

  it('navigates pages on button click', async () => {
    const user = userEvent.setup();
    vi.mocked(api.get).mockResolvedValue({ data: { ...mockResponse, totalPages: 3 } });
    renderTable();
    await waitFor(() => {
      expect(screen.getByText('Next')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Next'));
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/test-items', {
        params: expect.objectContaining({ page: 2 }),
      });
    });
  });

  it('renders page-size selector with options', async () => {
    renderTable();
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
    renderTable();
    await waitFor(() => {
      expect(screen.getByDisplayValue('10')).toBeInTheDocument();
    });

    await user.selectOptions(screen.getByDisplayValue('10'), '50');
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/test-items', {
        params: expect.objectContaining({ pageSize: 50, page: 1 }),
      });
    });
  });

  it('has correct column alignment', async () => {
    renderTable();
    await waitFor(() => {
      expect(screen.getByText('Item Name')).toBeInTheDocument();
    });
    expect(screen.getByText('Item Name').getAttribute('data-alignment')).toBe('left');
  });

  it('toggles sort direction without unsorted state', async () => {
    const user = userEvent.setup();
    renderTable();
    await waitFor(() => {
      expect(screen.getByText('Item Name')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Item Name'));
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/test-items', {
        params: expect.objectContaining({ sortField: 'name', sortDir: 'desc' }),
      });
    });

    await user.click(screen.getByText('Item Name'));
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/test-items', {
        params: expect.objectContaining({ sortField: 'name', sortDir: 'asc' }),
      });
    });
  });
});
