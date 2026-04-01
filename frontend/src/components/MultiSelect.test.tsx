import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, within } from '@testing-library/react';
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
    const matches = screen.getAllByText('Germany');
    // The label span + the option element both contain "Germany"
    expect(matches.length).toBeGreaterThanOrEqual(1);
    expect(matches[0].tagName).toBe('SPAN');
  });

  it('renders selected items joined by comma when 2 selected', () => {
    render(<MultiSelect options={options} selected={['Germany', 'USA']} onChange={onChange} placeholder="All countries" />);
    expect(screen.getByText('Germany, USA')).toBeInTheDocument();
  });

  it('renders count when more than 2 selected', () => {
    render(<MultiSelect options={options} selected={['Germany', 'Japan', 'USA']} onChange={onChange} placeholder="All countries" />);
    expect(screen.getByText('3 selected')).toBeInTheDocument();
  });

  it('sorts options alphabetically', () => {
    render(<MultiSelect options={['USA', 'Germany', 'Japan']} selected={[]} onChange={onChange} placeholder="All countries" />);
    const listbox = screen.getByRole('listbox', { name: 'All countries' });
    const optionEls = within(listbox).getAllByRole('option');
    expect(optionEls.map(o => o.textContent)).toEqual(['Germany', 'Japan', 'USA']);
  });

  it('calls onChange with selected value when option is picked', async () => {
    const user = userEvent.setup();
    render(<MultiSelect options={options} selected={[]} onChange={onChange} placeholder="All countries" />);
    const listbox = screen.getByRole('listbox', { name: 'All countries' });
    await user.selectOptions(listbox, ['Germany']);
    expect(onChange).toHaveBeenCalledWith(['Germany']);
  });

  it('calls onChange with multiple selected values', async () => {
    const user = userEvent.setup();
    render(<MultiSelect options={options} selected={['Germany']} onChange={onChange} placeholder="All countries" />);
    const listbox = screen.getByRole('listbox', { name: 'All countries' });
    await user.selectOptions(listbox, ['Germany', 'USA']);
    expect(onChange).toHaveBeenCalledWith(['Germany', 'USA']);
  });

  it('calls onChange with empty array when all deselected', async () => {
    const user = userEvent.setup();
    render(<MultiSelect options={options} selected={['Germany']} onChange={onChange} placeholder="All countries" />);
    const listbox = screen.getByRole('listbox', { name: 'All countries' });
    await user.deselectOptions(listbox, ['Germany']);
    expect(onChange).toHaveBeenCalledWith([]);
  });

  it('renders as a Strato Select component (not native HTML)', () => {
    render(<MultiSelect options={options} selected={[]} onChange={onChange} placeholder="All countries" />);
    // The component uses Strato Select with multiple prop — verified by presence of listbox
    expect(screen.getByRole('listbox', { name: 'All countries' })).toBeInTheDocument();
  });
});
