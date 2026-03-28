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

vi.mock('@dynatrace/strato-components/tables', () => ({
  DataTable: ({ data, columns, children }: any) => (
    <div data-testid="data-table">
      <table>
        <thead>
          <tr>
            {columns?.map((col: any) => (
              <th key={col.id}>{col.header}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data?.map((row: any, i: number) => (
            <tr key={i}>
              {columns?.map((col: any) => (
                <td key={col.id}>
                  {typeof col.accessor === 'function'
                    ? col.accessor(row)
                    : row[col.accessor]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
      {children}
    </div>
  ),
  DataTablePagination: () => <div data-testid="pagination" />,
}));

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
    ({ children, value, onChange, ...props }: any) => (
      <select value={value} onChange={(e: any) => onChange?.(e.target.value)} {...props}>
        {children}
      </select>
    ),
    {
      Content: ({ children }: any) => <>{children}</>,
      Option: ({ value, children }: any) => <option value={value}>{children}</option>,
    },
  ),
}));
