import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import PaginationControls from './PaginationControls';

describe('PaginationControls', () => {
  const defaultProps = {
    page: 2,
    totalPages: 5,
    pageSize: 10,
    onPageChange: vi.fn(),
    onPageSizeChange: vi.fn(),
  };

  it('shows page info', () => {
    render(<PaginationControls {...defaultProps} />);
    expect(screen.getByText('Page 2 of 5')).toBeInTheDocument();
  });

  it('renders page-size selector with Select.Option values', () => {
    render(<PaginationControls {...defaultProps} />);
    const pageSizeSelect = screen.getByDisplayValue('10');
    const options = pageSizeSelect.querySelectorAll('option');
    const values = Array.from(options).map(o => o.value);
    expect(values).toEqual(['10', '20', '50', '100']);
    const labels = Array.from(options).map(o => o.textContent);
    expect(labels).toEqual(['10', '20', '50', '100']);
  });

  it('calls onPageSizeChange when selecting a new size', async () => {
    const user = userEvent.setup();
    const onPageSizeChange = vi.fn();
    render(<PaginationControls {...defaultProps} onPageSizeChange={onPageSizeChange} />);
    await user.selectOptions(screen.getByDisplayValue('10'), '50');
    expect(onPageSizeChange).toHaveBeenCalledWith(50);
  });

  it('calls onPageChange for First button', async () => {
    const user = userEvent.setup();
    const onPageChange = vi.fn();
    render(<PaginationControls {...defaultProps} onPageChange={onPageChange} />);
    await user.click(screen.getByText('First'));
    expect(onPageChange).toHaveBeenCalledWith(1);
  });

  it('calls onPageChange for Previous button', async () => {
    const user = userEvent.setup();
    const onPageChange = vi.fn();
    render(<PaginationControls {...defaultProps} onPageChange={onPageChange} />);
    await user.click(screen.getByText('Previous'));
    expect(onPageChange).toHaveBeenCalledWith(1);
  });

  it('calls onPageChange for Next button', async () => {
    const user = userEvent.setup();
    const onPageChange = vi.fn();
    render(<PaginationControls {...defaultProps} onPageChange={onPageChange} />);
    await user.click(screen.getByText('Next'));
    expect(onPageChange).toHaveBeenCalledWith(3);
  });

  it('calls onPageChange for Last button', async () => {
    const user = userEvent.setup();
    const onPageChange = vi.fn();
    render(<PaginationControls {...defaultProps} onPageChange={onPageChange} />);
    await user.click(screen.getByText('Last'));
    expect(onPageChange).toHaveBeenCalledWith(5);
  });

  it('disables First and Previous on first page', () => {
    render(<PaginationControls {...defaultProps} page={1} />);
    expect(screen.getByText('First')).toBeDisabled();
    expect(screen.getByText('Previous')).toBeDisabled();
  });

  it('disables Next and Last on last page', () => {
    render(<PaginationControls {...defaultProps} page={5} />);
    expect(screen.getByText('Next')).toBeDisabled();
    expect(screen.getByText('Last')).toBeDisabled();
  });
});