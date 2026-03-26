# General UI Requirements

This document defines the baseline UI conventions that apply across all pages. Page-specific requirements (e.g., `dashboard.md`, `transactions.md`) take precedence when they conflict with these general rules.

---

## Core Principles

- **User-centred design** — Prioritise ease of use and task efficiency.
- **Consistency** — Follow the Strato design system for components, tokens, and layout patterns.
- **Accessibility** — Ensure the interface is usable by all users, regardless of ability or device.

---

## Design

### Visual Design

- Maintain a clean, modern, and uncluttered layout.
- Use Strato design-system components and CSS custom-property tokens consistently.

### Responsiveness

- Support desktop and mobile viewports.
- Adapt to different screen sizes and orientations; layout components must respond to viewport changes.

### Intuitiveness

- Provide clear, predictable navigation.
- Give immediate visual feedback on interactive elements.
- Display user-friendly, actionable error messages.

---

## Functional Requirements

### Performance

- Pages shall load without unnecessary delays.
- Data tables shall handle large datasets without degradation.
- Interactions shall feel responsive and immediate.

### Date and Number Formatting

#### Dates

| Context | Format | Example |
|---------|--------|---------|
| Date only | `DD-MM-YYYY` | `22-03-2026` |
| Date + time | `DD.MM.YYYY HH:mm` | `22.03.2026 14:30` |
| Sort value | ISO `YYYY-MM-DD` via `sortAccessor` | `2026-03-22` |

#### Numbers

- Use German locale: `toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })`
- Decimal separator: comma (`,`)
- Thousands separator: period (`.`)
- Always exactly 2 decimal places — e.g. `1.234,56`, `42,00`

---

### Data Tables

All `DataTable` instances shall follow these conventions unless a page-specific spec states otherwise.

#### Layout and Sizing

| Prop | Required | Purpose |
|------|----------|---------|
| `fullWidth` | ✔ | Stretch the table to the available container width |
| `resizable` | ✔ | Allow users to drag-resize column widths |

- Specify both `width` (initial) and `minWidth` (minimum) per column where appropriate.
  - **Text-heavy columns** (Name, Description): larger initial widths for readability.
  - **Fixed-format columns** (ISIN, Date): fixed widths matching content size.
  - **Numeric columns**: narrower widths, right-aligned.

#### Sorting and Filtering

- Enable the `sortable` prop on every table.
- Define a sensible `defaultSortBy` (e.g. most recent date first).
- When the display format differs from the sort value (e.g. formatted dates), provide a `sortAccessor` that returns the raw sortable value.

#### Pagination

- Large datasets shall be paginated by default.
- Default page size: **10** rows.
- Selectable page sizes: `[10, 20, 50, 100]`.
- A **Show All / Paginate** toggle button (top-right of the table area) switches between paginated and unpaginated views.
- Implementation: `<DataTablePagination defaultPageSize={10} pageSizeOptions={[10, 20, 50, 100]} />`

#### Export

- Each table shall offer **Export CSV** and **Export Excel** buttons, positioned top-right next to the Show All / Paginate toggle.
- **Formats:**
  - **CSV** — semicolon-separated, UTF-8 with BOM. Numeric values formatted with German locale (comma decimal separator, 2 decimal places).
  - **Excel** — `.xlsx`. Numeric columns must be written as **real numbers** (not text), with a 2-decimal-place cell format so that Excel recognises them as numeric and allows sorting, summing, charting, etc.
- **Scope:** Exports shall respect the currently applied filters **and** the current sort order (field + direction), and shall include column headers matching the visible columns.
- **Sort forwarding:** Every page shall track its active sort state (initialised from `defaultSortBy`) and pass `sortField` and `sortDir` to the export endpoint so the exported file matches the on-screen order.
- **Backend-driven generation:** The frontend sends a request to a backend export endpoint, passing the active sort field, sort direction, and all applied filters. The backend generates the file and returns it as a downloadable response. This keeps large-dataset exports off the client.

#### Row Count Display

- **Filtered:** `"N of M items"` (e.g. `"12 of 50 stocks"`).
- **Unfiltered:** `"M items"` (e.g. `"50 stocks"`).
- The count shall update in real time as filters change.

---

### Testing

- Verify a seamless experience across major browsers and device types.
- Validate that the interface is intuitive through usability testing.
