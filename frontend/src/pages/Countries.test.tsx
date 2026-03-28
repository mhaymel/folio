import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithRouter } from '../test/test-utils';
import Countries from './Countries';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

import api from '../api/client';

const mockCountries = [
  { id: 1, name: 'Austria' },
  { id: 2, name: 'Germany' },
  { id: 3, name: 'United States' },
];

beforeEach(() => {
  vi.mocked(api.get).mockResolvedValue({ data: mockCountries });
});

describe('Countries', () => {
  it('shows loading indicator while fetching', () => {
    vi.mocked(api.get).mockReturnValue(new Promise(() => {}));
    renderWithRouter(<Countries />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders countries heading', async () => {
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByText('Countries')).toBeInTheDocument();
    });
  });

  it('displays the count of countries', async () => {
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByText('3 countries')).toBeInTheDocument();
    });
  });

  it('renders country data in the table', async () => {
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByText('Austria')).toBeInTheDocument();
    });
    expect(screen.getByText('Germany')).toBeInTheDocument();
    expect(screen.getByText('United States')).toBeInTheDocument();
  });

  it('renders Show All / Paginate toggle', async () => {
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByText('Show All')).toBeInTheDocument();
    });
  });

  it('toggles Show All / Paginate', async () => {
    const user = userEvent.setup();
    renderWithRouter(<Countries />);

    await waitFor(() => {
      expect(screen.getByText('Show All')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Show All'));
    expect(screen.getByText('Paginate')).toBeInTheDocument();

    await user.click(screen.getByText('Paginate'));
    expect(screen.getByText('Show All')).toBeInTheDocument();
  });

  it('renders export buttons', async () => {
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByText('Export CSV')).toBeInTheDocument();
    });
    expect(screen.getByText('Export Excel')).toBeInTheDocument();
  });

  it('shows pagination by default', async () => {
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByTestId('pagination')).toBeInTheDocument();
    });
  });

  it('hides pagination when Show All is clicked', async () => {
    const user = userEvent.setup();
    renderWithRouter(<Countries />);
    await waitFor(() => {
      expect(screen.getByText('Show All')).toBeInTheDocument();
    });

    await user.click(screen.getByText('Show All'));
    expect(screen.queryByTestId('pagination')).not.toBeInTheDocument();
  });
});
