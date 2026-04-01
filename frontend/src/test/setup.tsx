import '@testing-library/jest-dom/vitest';
import { vi } from 'vitest';

// Mock Strato design tokens
vi.mock('@dynatrace/strato-design-tokens/variables', () => ({ default: {} }));
vi.mock('@dynatrace/strato-design-tokens/variables-dark', () => ({ default: {} }));

// Mock Strato components with simple HTML equivalents
vi.mock('@dynatrace/strato-components/core', () => ({
  AppRoot: ({ children }: any) => children,
}));

vi.mock('@dynatrace/strato-components/layouts', () => ({
  Flex: ({ children, ...props }: any) => <div data-testid="flex" {...props}>{children}</div>,
  Surface: ({ children, ...props }: any) => <div data-testid="surface" {...props}>{children}</div>,
  AppHeader: Object.assign(
    ({ children }: any) => <header data-testid="app-header">{children}</header>,
    {
      Navigation: ({ children }: any) => <nav>{children}</nav>,
      AppNavLink: ({ children, ...props }: any) => <a {...props}>{children}</a>,
    },
  ),
  Page: Object.assign(
    ({ children }: any) => <div data-testid="page">{children}</div>,
    {
      Header: ({ children }: any) => <div data-testid="page-header">{children}</div>,
      Sidebar: ({ children }: any) => <aside data-testid="page-sidebar">{children}</aside>,
      Main: ({ children }: any) => <main data-testid="page-main">{children}</main>,
    },
  ),
}));

vi.mock('@dynatrace/strato-components/typography', () => ({
  Heading: ({ children, level, ...props }: any) => {
    const Tag = `h${level || 1}` as keyof JSX.IntrinsicElements;
    return <Tag {...props}>{children}</Tag>;
  },
  Paragraph: ({ children, ...props }: any) => <p {...props}>{children}</p>,
}));

vi.mock('@dynatrace/strato-components/buttons', () => ({
  Button: ({ children, onClick, ...props }: any) => (
    <button onClick={onClick} {...props}>{children}</button>
  ),
}));

vi.mock('@dynatrace/strato-components/content', () => ({
  ProgressCircle: (props: any) => <div role="progressbar" aria-label={props['aria-label']} />,
}));

vi.mock('@dynatrace/strato-components/tables', () => {
  // Track sort state per table instance to simulate TanStack three-state cycle
  let sortState: { id: string; desc: boolean } | null = null;
  return {
    DataTable: ({ data, columns, children, onSortByChange, sortBy, defaultSortBy }: any) => {
      const controlled = sortBy?.[0];
      if (controlled) {
        sortState = { id: controlled.id, desc: controlled.desc };
      } else if (sortState === null && defaultSortBy?.[0]) {
        sortState = { id: defaultSortBy[0].id, desc: defaultSortBy[0].desc };
      }
      const handleHeaderClick = (colId: string) => {
        if (!onSortByChange) return;
        if (sortState?.id === colId) {
          if (!sortState.desc) {
            // asc -> desc
            sortState = { id: colId, desc: true };
            onSortByChange([{ id: colId, desc: true }]);
          } else {
            // desc -> removal (empty array)
            sortState = null;
            onSortByChange([]);
          }
        } else {
          // new column -> asc
          sortState = { id: colId, desc: false };
          onSortByChange([{ id: colId, desc: false }]);
        }
      };
      return (
        <div data-testid="data-table">
          <table>
            <thead>
              <tr>
                {columns?.map((col: any) => (
                  <th key={col.id} data-alignment={col.alignment || 'left'}
                    onClick={() => handleHeaderClick(col.id)} style={{ cursor: 'pointer' }}>{col.header}{sortState?.id === col.id && <span data-testid="sort-indicator">{sortState.desc ? ' ↓' : ' ↑'}</span>}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {data?.map((row: any, i: number) => (
                <tr key={i}>
                  {columns?.map((col: any) => {
                    const value = typeof col.accessor === 'function'
                      ? col.accessor(row)
                      : row[col.accessor];
                    return (
                      <td key={col.id}>
                        {col.cell ? col.cell({ value, rowData: row }) : value}
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
          {children}
        </div>
      );
    },
    DataTablePagination: () => <div data-testid="pagination" />,
  };
});

vi.mock('@dynatrace/strato-components/overlays', () => ({
  Modal: ({ children, show, title, onDismiss, footer }: any) =>
    show ? (
      <div data-testid="modal" role="dialog">
        <div data-testid="modal-title">{title}</div>
        <div>{children}</div>
        {footer && <div data-testid="modal-footer">{footer}</div>}
      </div>
    ) : null,
}));

vi.mock('@dynatrace/strato-components/filters', () => {
  const Presets = ({ children }: any) => <div data-testid="timeframe-presets">{children}</div>;
  Presets.displayName = 'TimeframeSelector.Presets';
  const PresetItem = ({ children, value }: any) => (
    <button data-testid="timeframe-preset" data-from={value?.from} data-to={value?.to}>{children}</button>
  );
  PresetItem.displayName = 'TimeframeSelector.PresetItem';
  const TimeframeSelector = Object.assign(
    ({ children, value, onChange, clearable }: any) => (
      <div data-testid="timeframe-selector">
        {value ? <span data-testid="timeframe-value">{value.from || ''} – {value.to || ''}</span> : <span>Select timeframe</span>}
        {clearable && value && (
          <button data-testid="timeframe-clear" onClick={() => onChange?.(null)}>Clear</button>
        )}
        {children}
      </div>
    ),
    { Presets, PresetItem },
  );
  return { TimeframeSelector };
});

vi.mock('@dynatrace/strato-components/forms', () => ({
  TextInput: ({ value, onChange, ...props }: any) => (
    <input
      type="text"
      value={value}
      onChange={(e: any) => onChange?.(e.target.value, e)}
      {...props}
    />
  ),
  Select: Object.assign(
    ({ children, value, onChange, multiple, placeholder, clearable, ...props }: any) => {
      if (multiple) {
        const selectedValues: string[] = Array.isArray(value) ? value : [];
        const handleMultiChange = (e: any) => {
          const selected = Array.from(e.target.selectedOptions).map((o: any) => o.value);
          onChange?.(selected.length > 0 ? selected : null);
        };
        return (
          <div>
            {selectedValues.length === 0 && <span>{placeholder}</span>}
            {selectedValues.length > 0 && selectedValues.length <= 2 && <span>{selectedValues.join(', ')}</span>}
            {selectedValues.length > 2 && <span>{selectedValues.length} selected</span>}
            <select multiple value={selectedValues} onChange={handleMultiChange} aria-label={placeholder}>
              {children}
            </select>
          </div>
        );
      }
      return (
        <select value={value} onChange={(e: any) => onChange?.(e.target.value)} {...props}>
          {children}
        </select>
      );
    },
    {
      Content: ({ children }: any) => <>{children}</>,
      Option: ({ value, children }: any) => <option value={value}>{children}</option>,
    },
  ),
}));
