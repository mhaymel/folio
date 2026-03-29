import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import MultiSelect from './MultiSelect';

describe('MultiSelect', () => {
  const options = ['Germany', 'Japan', 'USA'];
  let onChange: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    onChange = vi.fn();
  });

  it('renders placeholder when nothing selected', () => {
    render(<MultiSelect options={options} selected={[]} onChange={onChange} placeholder="All countries" />);
    expect(screen.getByText('All countries')).toBeInTheDocument();
  });

  it('renders selected items when 1-2 selected', () => {
    render(<MultiSelect options={options} selected={['Germany']} onChange={onChange} placeholder="All countries" />);
    expect(screen.getByText('Germany')).toBeInTheDocument();
  });

  it('renders selected items joined by comma when 2 selected', () => {
    render(<MultiSelect options={options} selected={['Germany', 'USA']} onChange={onChange} placeholder="All countries" />);
    expect(screen.getByText('Germany, USA')).toBeInTheDocument();
  });

  it('renders count when more than 2 selected', () => {
    render(<MultiSelect options={options} selected={['Germany', 'Japan', 'USA']} onChange={onChange} placeholder="All countries" />);
    expect(screen.getByText('3 selected')).toBeInTheDocument();
  });

  it('opens dropdown when button is clicked', async () => {
    const user = userEvent.setup();
    render(<MultiSelect options={options} selected={[]} onChange={onChange} placeholder="All countries" />);

    await user.click(screen.getByRole('button'));

    // Should show checkboxes for each option
    const checkboxes = screen.getAllByRole('checkbox');
    expect(checkboxes).toHaveLength(4); // 3 options + 1 "All" checkbox
  });

  it('calls onChange with added value when option checkbox is clicked', async () => {
    const user = userEvent.setup();
    render(<MultiSelect options={options} selected={[]} onChange={onChange} placeholder="All countries" />);

    await user.click(screen.getByRole('button'));

    // Click Germany checkbox
    const labels = screen.getAllByText('Germany');
    await user.click(labels[labels.length - 1]); // click the label in dropdown

    expect(onChange).toHaveBeenCalledWith(['Germany']);
  });

  it('calls onChange with removed value when selected option is unchecked', async () => {
    const user = userEvent.setup();
    render(<MultiSelect options={options} selected={['Germany', 'USA']} onChange={onChange} placeholder="All countries" />);

    await user.click(screen.getByRole('button'));

    // Find and click the Germany checkbox in dropdown to deselect
    const checkboxes = screen.getAllByRole('checkbox');
    // First checkbox is "All", then Germany, Japan, USA
    await user.click(checkboxes[1]); // Germany

    expect(onChange).toHaveBeenCalledWith(['USA']);
  });

  it('calls onChange with empty array when "All" is clicked', async () => {
    const user = userEvent.setup();
    render(<MultiSelect options={options} selected={['Germany']} onChange={onChange} placeholder="All countries" />);

    await user.click(screen.getByRole('button'));

    // Click the "All countries" option (first checkbox)
    const checkboxes = screen.getAllByRole('checkbox');
    await user.click(checkboxes[0]);

    expect(onChange).toHaveBeenCalledWith([]);
  });

  it('"All" checkbox is checked when nothing is selected', async () => {
    const user = userEvent.setup();
    render(<MultiSelect options={options} selected={[]} onChange={onChange} placeholder="All countries" />);

    await user.click(screen.getByRole('button'));

    const checkboxes = screen.getAllByRole('checkbox');
    expect(checkboxes[0]).toBeChecked(); // "All" is checked
    expect(checkboxes[1]).not.toBeChecked(); // Germany
  });

  it('closes dropdown when clicking outside', async () => {
    const user = userEvent.setup();
    const { container } = render(
      <div>
        <div data-testid="outside">Outside</div>
        <MultiSelect options={options} selected={[]} onChange={onChange} placeholder="All countries" />
      </div>
    );

    await user.click(screen.getByRole('button'));
    expect(screen.getAllByRole('checkbox').length).toBeGreaterThan(0);

    await user.click(screen.getByTestId('outside'));
    // Checkboxes should no longer be visible
    expect(screen.queryAllByRole('checkbox')).toHaveLength(0);
  });
});
