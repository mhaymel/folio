import { useEffect, useState, useCallback } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { Branch, PaginatedResponse } from '../types';
import ExportButtons from '../components/ExportButtons';
import PaginationControls from '../components/PaginationControls';

const columns = [
  { id: 'name', header: 'Branch', accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 300, minWidth: 200 },
];

export default function Branches() {
  const [data, setData] = useState<Branch[]>([]);
  const [totalItems, setTotalItems] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [sortField, setSortField] = useState('name');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const r = await api.get<PaginatedResponse<Branch>>('/branches', {
        params: { sortField, sortDir, page, pageSize },
      });
      setData(r.data.items);
      setTotalItems(r.data.totalItems);
      setTotalPages(r.data.totalPages);
    } finally {
      setLoading(false);
    }
  }, [sortField, sortDir, page, pageSize]);

  useEffect(() => { load(); }, [load]);

  const handleShowAll = () => {
    if (pageSize === -1) {
      setPageSize(10);
      setPage(1);
    } else {
      setPageSize(-1);
    }
  };

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Branches</Heading>

      {loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading branches" size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
        </Flex>
      ) : (
        <>
          <Flex alignItems="center" justifyContent="space-between">
            <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
              {totalItems} branches
            </Paragraph>
            <Flex gap={8} alignItems="center">
              <ExportButtons endpoint="/branches/export" params={{ sortField, sortDir }} />
              <Button variant="default" onClick={handleShowAll}>
                {pageSize === -1 ? 'Paginate' : 'Show All'}
              </Button>
            </Flex>
          </Flex>
          <DataTable data={data} columns={columns} sortable={{ manualSort: true }} resizable fullWidth sortBy={[{ id: sortField, desc: sortDir === 'desc' }]}
            onSortByChange={(s: any) => { if (s?.[0]) { setSortField(s[0].id); setSortDir(s[0].desc ? 'desc' : 'asc'); } else { setSortDir(d => d === 'asc' ? 'desc' : 'asc'); } setPage(1); }}>
          </DataTable>
          {pageSize !== -1 && (
            <PaginationControls page={page} totalPages={totalPages} pageSize={pageSize}
              onPageChange={setPage} onPageSizeChange={(size) => { setPageSize(size); setPage(1); }} />
          )}
        </>
      )}
    </Flex>
  );
}