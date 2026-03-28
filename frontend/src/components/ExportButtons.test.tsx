import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ExportButtons from './ExportButtons';

describe('ExportButtons', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders CSV and Excel export buttons', () => {
    render(<ExportButtons endpoint="/stocks/export" />);
    expect(screen.getByText('Export CSV')).toBeInTheDocument();
    expect(screen.getByText('Export Excel')).toBeInTheDocument();
  });

  it('triggers CSV download with correct URL', async () => {
    const user = userEvent.setup();
    render(<ExportButtons endpoint="/countries/export" params={{ sortField: 'name', sortDir: 'asc' }} />);

    // Capture the URL from the anchor click
    let clickedHref = '';
    const origCreateElement = document.createElement.bind(document);
    vi.spyOn(document, 'createElement').mockImplementation((tag: string, options?: any) => {
      if (tag === 'a') {
        const anchor = origCreateElement('a') as HTMLAnchorElement;
        const origClick = anchor.click.bind(anchor);
        anchor.click = () => { clickedHref = anchor.href; };
        return anchor as any;
      }
      return origCreateElement(tag, options);
    });

    await user.click(screen.getByText('Export CSV'));
    expect(clickedHref).toContain('/countries/export');
    expect(clickedHref).toContain('format=csv');
    expect(clickedHref).toContain('sortField=name');
    expect(clickedHref).toContain('sortDir=asc');
  });

  it('triggers Excel download with correct URL', async () => {
    const user = userEvent.setup();
    render(<ExportButtons endpoint="/stocks/export" />);

    let clickedHref = '';
    const origCreateElement = document.createElement.bind(document);
    vi.spyOn(document, 'createElement').mockImplementation((tag: string, options?: any) => {
      if (tag === 'a') {
        const anchor = origCreateElement('a') as HTMLAnchorElement;
        anchor.click = () => { clickedHref = anchor.href; };
        return anchor as any;
      }
      return origCreateElement(tag, options);
    });

    await user.click(screen.getByText('Export Excel'));
    expect(clickedHref).toContain('/stocks/export');
    expect(clickedHref).toContain('format=xlsx');
  });

  it('omits empty params from URL', async () => {
    const user = userEvent.setup();
    render(<ExportButtons endpoint="/test/export" params={{ keep: 'yes', empty: '', nullable: null }} />);

    let clickedHref = '';
    const origCreateElement = document.createElement.bind(document);
    vi.spyOn(document, 'createElement').mockImplementation((tag: string, options?: any) => {
      if (tag === 'a') {
        const anchor = origCreateElement('a') as HTMLAnchorElement;
        anchor.click = () => { clickedHref = anchor.href; };
        return anchor as any;
      }
      return origCreateElement(tag, options);
    });

    await user.click(screen.getByText('Export CSV'));
    expect(clickedHref).toContain('keep=yes');
    expect(clickedHref).not.toContain('empty=');
    expect(clickedHref).not.toContain('nullable=');
  });
});
