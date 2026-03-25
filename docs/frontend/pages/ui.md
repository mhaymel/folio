# General UI Requirements

This document contains general UI requirements that apply to the overall design and functionality of the user interface. These requirements serve as a baseline for the overall user experience and can be overridden by specific requirements for individual pages.

## Core Principles

- **User-centered design**: The UI shall prioritize ease of use and efficiency in completing tasks
- **Consistency**: The UI shall be consistent with the overall branding and design system of the application
- **Accessibility**: The UI shall be accessible to all users, regardless of their abilities or devices

## Design Requirements

### Visual Design
- The UI shall be visually appealing and modern
- The UI shall maintain a clean and uncluttered layout
- The UI shall use the Strato design system components and tokens consistently

### Responsiveness
- The UI shall work well on both desktop and mobile devices
- The UI shall adapt to different screen sizes and orientations
- Layout components shall respond appropriately to viewport changes

### Intuitiveness
- Navigation shall be clear and easy to understand
- Interactive elements shall provide appropriate visual feedback
- Error messages shall be user-friendly and actionable

## Functional Requirements

### Performance
- Pages shall load efficiently without unnecessary delays
- Data tables shall handle large datasets without performance degradation
- User interactions shall feel responsive and immediate

### Date and Number Formatting
- **Dates**: Display format shall be `DD-MM-YYYY` (e.g., "22-03-2026")
  - For timestamps with time: `DD.MM.YYYY HH:mm` (e.g., "22.03.2026 14:30")
  - For sorting: Use ISO format `YYYY-MM-DD` as `sortAccessor` to ensure chronological ordering
- **Numbers (decimals)**: Use German locale formatting
  - Format: `toLocaleString('de-DE', {minimumFractionDigits: 2, maximumFractionDigits: 2})`
  - Decimal separator: comma (`,`)
  - Thousands separator: period (`.`)
  - Always show exactly 2 decimal places
  - Examples: `1.234,56` or `42,00`

### Data Tables
All data tables in the application shall follow these conventions unless explicitly overridden in page-specific requirements:

#### Layout and Sizing
- **Full width**: Tables shall use the `fullWidth` prop to stretch to the available container width
- **Resizable columns**: Tables shall use the `resizable` prop to allow users to adjust column widths
- **Column widths**: Columns shall specify both `width` (initial) and `minWidth` (minimum) values where appropriate
  - Text-heavy columns (e.g., Name, Description): larger initial widths for readability
  - Fixed-format columns (e.g., ISIN, Date): fixed widths matching content size
  - Numeric columns: narrower widths, right-aligned

#### Sorting and Filtering
- **Sortable**: Tables shall use the `sortable` prop to enable column-based sorting
- **Default sort**: Tables should specify a sensible `defaultSortBy` (e.g., most recent date first)
- **Sort accessors**: When display format differs from sort value (e.g., dates), use `sortAccessor` to provide the raw sortable value

#### Pagination
- **Default behavior**: Large datasets shall use pagination by default
- **Page size**: Default page size shall be 10 rows
- **Page size options**: User shall be able to select from `[10, 20, 50, 100]` rows per page
- **Show All toggle**: Tables may provide a "Show All / Paginate" toggle for viewing all rows without pagination
- **Implementation**: Use `DataTablePagination` component with `defaultPageSize={10}` and `pageSizeOptions={[10, 20, 50, 100]}`

#### Row Count Display
- **Filtered count**: When filters are active, display `"N of M items"` format
- **Unfiltered count**: When no filters are active, display `"M items"` format
- The count should update in real-time as filters are applied

### Testing
- The UI shall be tested thoroughly to ensure it meets the needs of the target audience
- Testing shall verify a seamless user experience across different browsers and devices
- Usability testing shall validate that the interface is intuitive for the target users

## Overrides

Specific page requirements documented in individual page specification files (e.g., `dashboard.md`, `transactions.md`) take precedence over these general requirements when conflicts arise.
