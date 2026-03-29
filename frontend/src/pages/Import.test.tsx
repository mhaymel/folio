import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithRouter } from '../test/test-utils';
import Import from './Import';

vi.mock('../api/client', () => ({
  default: {
    post: vi.fn(),
  },
}));

import api from '../api/client';

beforeEach(() => {
  vi.mocked(api.post).mockReset();
});

describe('Import', () => {
  it('renders heading', () => {
    renderWithRouter(<Import />);
    expect(screen.getByText('Import Data')).toBeInTheDocument();
  });

  it('renders all import sections', () => {
    renderWithRouter(<Import />);
    expect(screen.getByText('DeGiro Transactions')).toBeInTheDocument();
    expect(screen.getByText('DeGiro Account (Dividends)')).toBeInTheDocument();
    expect(screen.getByText('ZERO Orders')).toBeInTheDocument();
    expect(screen.getByText('ZERO Account (Dividends)')).toBeInTheDocument();
    expect(screen.getByText('Dividends')).toBeInTheDocument();
    expect(screen.getByText('Branches')).toBeInTheDocument();
    expect(screen.getByText('Countries')).toBeInTheDocument();
    expect(screen.getByText('Ticker Symbols')).toBeInTheDocument();
  });

  it('renders descriptions for each section', () => {
    renderWithRouter(<Import />);
    expect(screen.getByText('Upload DeGiro Transactions.csv')).toBeInTheDocument();
    expect(screen.getByText('Upload DeGiro Account.csv')).toBeInTheDocument();
    expect(screen.getByText('Upload ZERO orders CSV')).toBeInTheDocument();
    expect(screen.getByText('Upload ZERO kontoumsaetze CSV')).toBeInTheDocument();
    expect(screen.getByText('Upload dividende.csv')).toBeInTheDocument();
    expect(screen.getByText('Upload branches.csv')).toBeInTheDocument();
    expect(screen.getByText('Upload countries.csv')).toBeInTheDocument();
    expect(screen.getByText('Upload ticker_symbol.csv')).toBeInTheDocument();
  });

  it('renders Choose file buttons for each section', () => {
    renderWithRouter(<Import />);
    const buttons = screen.getAllByText('Choose file');
    expect(buttons).toHaveLength(8);
  });

  it('shows success state after successful upload', async () => {
    const user = userEvent.setup();
    vi.mocked(api.post).mockResolvedValue({
      data: { success: true, imported: 42, durationMs: 1500, errors: [] },
    });
    renderWithRouter(<Import />);

    const fileInputs = document.querySelectorAll('input[type="file"]');
    const file = new File(['test'], 'Transactions.csv', { type: 'text/csv' });
    await user.upload(fileInputs[0] as HTMLInputElement, file);

    await waitFor(() => {
      expect(screen.getByText('Imported 42 rows in 1s 500ms')).toBeInTheDocument();
    });
  });

  it('formats duration as ms only when under 1 second', async () => {
    const user = userEvent.setup();
    vi.mocked(api.post).mockResolvedValue({
      data: { success: true, imported: 10, durationMs: 250, errors: [] },
    });
    renderWithRouter(<Import />);

    const fileInputs = document.querySelectorAll('input[type="file"]');
    const file = new File(['test'], 'test.csv', { type: 'text/csv' });
    await user.upload(fileInputs[0] as HTMLInputElement, file);

    await waitFor(() => {
      expect(screen.getByText('Imported 10 rows in 250ms')).toBeInTheDocument();
    });
  });

  it('shows error state after failed upload', async () => {
    const user = userEvent.setup();
    vi.mocked(api.post).mockResolvedValue({
      data: { success: false, imported: 0, durationMs: 100, errors: ['Invalid CSV format'] },
    });
    renderWithRouter(<Import />);

    const fileInputs = document.querySelectorAll('input[type="file"]');
    const file = new File(['bad'], 'bad.csv', { type: 'text/csv' });
    await user.upload(fileInputs[0] as HTMLInputElement, file);

    await waitFor(() => {
      expect(screen.getByText('Invalid CSV format')).toBeInTheDocument();
    });
  });

  it('shows error with partial import count', async () => {
    const user = userEvent.setup();
    vi.mocked(api.post).mockResolvedValue({
      data: { success: false, imported: 5, durationMs: 100, errors: ['Row 6 failed'] },
    });
    renderWithRouter(<Import />);

    const fileInputs = document.querySelectorAll('input[type="file"]');
    const file = new File(['data'], 'data.csv', { type: 'text/csv' });
    await user.upload(fileInputs[0] as HTMLInputElement, file);

    await waitFor(() => {
      expect(screen.getByText(/Row 6 failed/)).toBeInTheDocument();
      expect(screen.getByText(/5 rows imported/)).toBeInTheDocument();
    });
  });

  it('shows error state on network failure', async () => {
    const user = userEvent.setup();
    vi.mocked(api.post).mockRejectedValue(new Error('Network Error'));
    renderWithRouter(<Import />);

    const fileInputs = document.querySelectorAll('input[type="file"]');
    const file = new File(['data'], 'data.csv', { type: 'text/csv' });
    await user.upload(fileInputs[0] as HTMLInputElement, file);

    await waitFor(() => {
      expect(screen.getByText('Network Error')).toBeInTheDocument();
    });
  });

  it('posts to correct endpoint with multipart form data', async () => {
    const user = userEvent.setup();
    vi.mocked(api.post).mockResolvedValue({
      data: { success: true, imported: 1, durationMs: 50, errors: [] },
    });
    renderWithRouter(<Import />);

    const fileInputs = document.querySelectorAll('input[type="file"]');
    const file = new File(['test'], 'Transactions.csv', { type: 'text/csv' });
    await user.upload(fileInputs[0] as HTMLInputElement, file);

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith(
        '/import/degiro/transactions',
        expect.any(FormData),
        { headers: { 'Content-Type': 'multipart/form-data' } },
      );
    });
  });

  it('accepts only .csv files', () => {
    renderWithRouter(<Import />);
    const fileInputs = document.querySelectorAll('input[type="file"]');
    fileInputs.forEach(input => {
      expect(input.getAttribute('accept')).toBe('.csv');
    });
  });
});
