import { Flex } from '@dynatrace/strato-components/layouts';
import { Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import useServerTable from '../hooks/useServerTable';
import type { PaginatedResponse } from '../types';
import ExportButtons from './ExportButtons';
import PaginationControls from './PaginationControls';

interface ServerTableProps<T> {
  endpoint: string;
  exportEndpoint: string;
  columns: any[];
  defaultSortField: string;
  defaultSortDir?: 'asc' | 'desc';
  itemLabel: string;
}

export default function ServerTable<T>({
  endpoint,
  exportEndpoint,
  columns,
  defaultSortField,
  defaultSortDir,
  itemLabel,
}: ServerTableProps<T>) {
  const table = useServerTable<T, PaginatedResponse<T>>({
    endpoint,
    defaultSortField,
    defaultSortDir,
  });

  if (table.loading) {
    return (
      <Flex alignItems="center" gap={12}>
        <ProgressCircle aria-label={`Loading ${itemLabel}`} size="small" />
        <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
      </Flex>
    );
  }

  return (
    <>
      <Flex alignItems="center" justifyContent="space-between">
        <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
          {table.totalItems} {itemLabel}
        </Paragraph>
        <Flex gap={8} alignItems="center">
          <ExportButtons endpoint={exportEndpoint} params={table.exportParams} />
          <Button variant="emphasized" onClick={table.handleShowAll}>
            {table.pageSize === -1 ? 'Paginate' : 'Show All'}
          </Button>
        </Flex>
      </Flex>
      <DataTable
        data={table.data}
        columns={columns}
        sortable={{ manualSort: true }}
        resizable
        fullWidth
        sortBy={table.sortBy}
        onSortByChange={table.handleSortChange}
      />
      {table.pageSize !== -1 && (
        <PaginationControls
          page={table.page}
          totalPages={table.totalPages}
          pageSize={table.pageSize}
          onPageChange={table.setPage}
          onPageSizeChange={table.handlePageSizeChange}
        />
      )}
    </>
  );
}
