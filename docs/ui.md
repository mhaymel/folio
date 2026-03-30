# General UI Requirements

This document defines the baseline UI conventions that apply across all pages. Page-specific requirements (e.g., `dashboard.md`, `transactions.md`) take precedence when they conflict with these general rules.

---

## Core Principles

- **Thin frontend / fat backend** — The frontend shall contain as little logic as possible. All filtering, sorting, aggregation, and data derivation must happen on the backend. The frontend sends user criteria (filter values, sort field/direction) to the backend via REST query parameters and renders the response as-is. This minimises frontend code and keeps a single source of truth for business logic.
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

**Backend-formatted:** All date values are formatted by the backend before being sent to the frontend. The backend returns date-only fields as `DD-MM-YYYY` strings and date-time fields as `DD.MM.YYYY HH:mm` strings. The frontend renders these strings as-is — no `sortAccessor`, no `isoDate`/`fmtDate` helpers, no `new Date()` parsing. Sorting is handled by the backend on the underlying `LocalDateTime` column before formatting.

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

Every column shall specify both `width` (initial) and `minWidth` (minimum). Choose the smallest values that still display the content in full — no truncation, no wasted space.

| Column type | Sizing rule | Alignment |
|-------------|-------------|-----------|
| **Date** (`DD-MM-YYYY`) | `width: 105`, `minWidth: 105` — exactly fits the 10-character date string | centre |
| **Text** (Name, ISIN, Country, …) | Set `width` and `minWidth` to the smallest values that show the longest expected value without truncation | left |
| **Number** (Count, Price, %, …) | Set `width` and `minWidth` to the smallest values that show the longest expected formatted number without truncation | right |

#### Visual Consistency

- Use a clean, consistent visual style across all tables.
- Column spacing and row padding shall be uniform; avoid overly dense or overly spacious layouts.
- Font size and style must be legible and consistent across all table pages.
- Colour usage shall follow the Strato design-system tokens for text, backgrounds, and accents.
- Tables must be responsive: content shall remain readable and accessible on different screen sizes and devices without horizontal overflow or layout breakage.

#### ISIN Column Conventions

All tables that contain an ISIN column shall follow these conventions:

- **Monospace font:** ISIN values shall be rendered in a monospace (`fontFamily: monospace`) font for readability and alignment.
- **Copy to clipboard icon:** A small copy icon (`CopyIcon` from `@dynatrace/strato-icons`, 14 px) is displayed to the right of the ISIN text. Clicking it copies the ISIN to the system clipboard via `navigator.clipboard.writeText()` and shows a Strato toast notification (`showToast` from `@dynatrace/strato-components/notifications`, type `success`) with the text "ISIN &lt;value&gt; copied to clipboard" (e.g. "ISIN DE000BASF111 copied to clipboard"). The toast auto-dismisses after 2 seconds. The icon has a tooltip "Copy ISIN to clipboard".
- **Filter icon (optional):** On pages that have an ISIN filter input, a small filter icon (`FilterIcon` from `@dynatrace/strato-icons`, 14 px) is displayed next to the copy icon. Clicking it copies the ISIN into the filter input and triggers a table refetch. The icon has a tooltip "Filter by ISIN". Pages without an ISIN filter input (e.g. Dashboard, ISIN Names, Ticker Symbols) show only the copy icon.
- **Icon visibility:** Both icons are shown at 50 % opacity by default and full opacity on hover.
- The shared `IsinCell` component (`src/components/IsinCell.tsx`) implements all of the above and is used across all pages with ISIN columns.

#### Sorting and Filtering

- **Server-side sorting**: All sorting must be performed by the backend. When the user clicks a column header, the frontend must:
  1. Capture the new sort field and direction from the `onSortByChange` callback.
  2. Send a new API request with the updated `sortField` and `sortDir` query params (and reset `page` to `1`).
  3. Replace the table data with the backend's response.
  The DataTable `sortable` prop is enabled only for UI affordance (sort indicator arrows). The DataTable must **never** reorder rows client-side — the backend is the single source of truth for row ordering. If the DataTable component performs its own client-side sort in addition to the server request, this must be suppressed (e.g. by treating all columns as pre-sorted data that the table merely displays).
- **Two-state sort toggle**: Column header clicks shall only cycle between ascending and descending — there is no "unsorted" third state. When the `onSortByChange` callback receives an empty sort array (TanStack Table's removal state), the handler shall toggle the current direction instead of clearing the sort. This ensures there is always an active sort field and direction.
- **Date columns must be sorted by their logical value** (the underlying `LocalDateTime`), **not alphanumerically** by the formatted string. Since sorting is server-side, the backend sorts on the raw `LocalDateTime` column before formatting — no `sortAccessor` or client-side date parsing is needed.
- Define a sensible default sort (e.g. most recent date first) and send it on the initial request.

#### Pagination

Pagination is **server-side**. The backend slices the sorted/filtered result set and returns a paginated envelope. The frontend does **not** use Strato's `<DataTablePagination>` component.

- **Default page size:** 10 rows.
- **Selectable page sizes:** `[10, 20, 50, 100]`.
- **Query params:** `page` (1-based, default `1`) and `pageSize` (default `10`; `-1` = all items).
- A **Show All / Paginate** toggle button (top-right of the table area) switches between paginated (`pageSize=10`) and unpaginated (`pageSize=-1`) views.
- **Paginated response envelope** (all list endpoints):
  ```json
  {
    "items": [ … ],
    "page": 1,
    "pageSize": 10,
    "totalItems": 142,
    "totalPages": 15
  }
  ```
- The frontend renders a pagination bar below the table: page navigation (first/previous/next/last), current page indicator (`"Page 1 of 15"`), and a page-size selector.
- The **page-size selector** shall use a Strato `Select` component (not a native `<select>`) with options `10`, `20`, `50`, `100`. Changing the page size resets `page` to `1` and triggers a refetch.
- When filters or sort change, `page` resets to `1`.

#### Export

- Each table shall offer **Export CSV** and **Export Excel** buttons, positioned top-right next to the Show All / Paginate toggle.
- **Formats:**
  - **CSV** — semicolon-separated, UTF-8 with BOM. Numeric values formatted with German locale (comma decimal separator, 2 decimal places).
  - **Excel** — `.xlsx`. Numeric columns must be written as **real numbers** (not text), with a 2-decimal-place cell format so that Excel recognises them as numeric and allows sorting, summing, charting, etc.
- **Scope:** Exports shall respect the currently applied filters **and** the current sort order (field + direction), and shall include column headers matching the visible columns.
- **Sort forwarding:** Every page shall track its active sort state (initialised from `defaultSortBy`) and pass `sortField` and `sortDir` to the export endpoint so the exported file matches the on-screen order.
- **Backend-driven generation:** The frontend sends a request to a backend export endpoint, passing the active sort field, sort direction, and all applied filters. The backend generates the file and returns it as a downloadable response. This keeps large-dataset exports off the client.

#### Filter Input Focus Retention

- **Filter inputs must always remain mounted**, even while data is loading. The loading indicator (spinner) shall only replace the table/data area, never the filter bar. This prevents focus loss when the user is typing in a filter field and a refetch is triggered.

#### Row Count Display

- **Filtered:** `"N of M items"` (e.g. `"12 of 50 stocks"`).
- **Unfiltered:** `"M items"` (e.g. `"50 stocks"`).
- The count shall update in real time as filters change.
- To support the "N of M" format, paginated response envelopes on endpoints that support filtering must include both `filteredCount` (count after filtering) and `totalCount` (count of all items regardless of filters).

#### Debounce on Text Filter Inputs

- All free-text filter inputs (e.g. ISIN, Name) shall be **debounced with a 300 ms delay** before triggering an API refetch. This avoids excessive requests on every keystroke.

#### Refresh Button

- Every page with a data table shall include a **Refresh** button that reloads all data from the backend.

#### Button Visibility

- All buttons must be visually distinguishable from the background. Buttons shall have a visible border and background colour that contrasts with the page background, ensuring they are discoverable without hovering. Button text must be readable at all times.

#### Table Cell Vertical Alignment and Padding

- All table cells (`<td>` and `<th>`) shall be vertically aligned to **center** (`vertical-align: middle`).
- All table cells shall have horizontal padding (at least 8 px left and right) to ensure text does not touch the cell border.

#### State Preservation Across Navigation

- Filter values, sort field/direction, current page, and page size shall be preserved when the user navigates away from a page and returns. The state is stored in `sessionStorage` keyed by endpoint. This ensures the user sees the same view they left, without resetting to defaults on every navigation.

#### Multi-Select Dropdown Filters

- Dropdown filters for categorical values (e.g. Depot, Country, Branch) shall be **multi-select**: the user can select zero or more options. When no options are selected, all items are shown (equivalent to "All"). The backend accepts comma-separated values for these filter parameters and filters using an `IN` clause (or set membership check).

---

### Testing

See [testing.md](testing.md) for all testing conventions, scope, and infrastructure.
