import { useEffect, useState, useCallback, useMemo } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { TextInput, Select } from '@dynatrace/strato-components/forms';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { TransactionDto, TransactionPaginatedResponse, TransactionFiltersDto } from '../types';
import useServerTable from '../hooks/useServerTable';
import ExportButtons from '../components/ExportButtons';
import PaginationControls from '../components/PaginationControls';

const fmtNum2 = (v: number) => v.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const fmtNum3 = (v: number) => v.toLocaleString('de-DE', { minimumFractionDigits: 3, maximumFractionDigits: 3 });

export default function Transactions() {
  const [filteredCount, setFilteredCount] = useState(0);
  const [sumCount, setSumCount] = useState(0);
  const [isinFilter, setIsinFilter] = useState('');
  const [nameFilter, setNameFilter] = useState('');
  const [depotFilter, setDepotFilter] = useState('');
  const [depotOptions, setDepotOptions] = useState<string[]>([]);

  const extraParams = useMemo(() => {
    const p: Record<string, string> = {};
    if (isinFilter) p.isin = isinFilter;
    if (nameFilter) p.name = nameFilter;
    if (depotFilter) p.depot = depotFilter;
    return p;
  }, [isinFilter, nameFilter, depotFilter]);

  const table = useServerTable<TransactionDto, TransactionPaginatedResponse>({
    endpoint: '/transactions',
    defaultSortField: 'date',
    defaultSortDir: 'desc',
    extraParams,
    responseMapper: (resp) => {
      setFilteredCount(resp.filteredCount);
      setSumCount(resp.sumCount);
    },
  });

  const loadFilters = useCallback(async () => {
    try {
      const r = await api.get<TransactionFiltersDto>('/transactions/filters');
      setDepotOptions(r.data.depots);
    } catch { /* ignore */ }
  }, []);

  useEffect(() => { loadFilters(); }, [loadFilters]);

  const handleCellDoubleClick = (field: string, value: string) => {
    if (field === 'isin') { setIsinFilter(value); table.setPage(1); }
    else if (field === 'name') { setNameFilter(value); table.setPage(1); }
    else if (field === 'depot') { setDepotFilter(value); table.setPage(1); }
  };

  const columns = [
    { id: 'date', header: 'Date', accessor: 'date', sortType: 'text' as const, alignment: 'center' as const, width: 105, minWidth: 105 },
    {
      id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const, alignment: 'left' as const, width: 140, minWidth: 140,
      cell: ({ rowData }: { rowData: TransactionDto }) => (
        <span onDoubleClick={() => handleCellDoubleClick('isin', rowData.isin)} style={{ display: 'flex', alignItems: 'center', height: '100%', cursor: 'pointer' }}>{rowData.isin}</span>
      ),
    },
    {
      id: 'name', header: 'Name', accessor: (row: TransactionDto) => row.name ?? '', sortType: 'text' as const, alignment: 'left' as const, width: 240, minWidth: 120,
      cell: ({ rowData }: { rowData: TransactionDto }) => (
        <span onDoubleClick={() => rowData.name && handleCellDoubleClick('name', rowData.name)} style={{ display: 'flex', alignItems: 'center', height: '100%', cursor: 'pointer' }}>{rowData.name ?? ''}</span>
      ),
    },
    {
      id: 'depot', header: 'Depot', accessor: 'depot', sortType: 'text' as const, alignment: 'left' as const, width: 100, minWidth: 80,
      cell: ({ rowData }: { rowData: TransactionDto }) => (
        <span onDoubleClick={() => handleCellDoubleClick('depot', rowData.depot)} style={{ display: 'flex', alignItems: 'center', height: '100%', cursor: 'pointer' }}>{rowData.depot}</span>
      ),
    },
    {
      id: 'count', header: 'Count', accessor: (row: TransactionDto) => fmtNum3(row.count), sortType: 'number' as const, alignment: 'right' as const, width: 100, minWidth: 80,
      cell: ({ rowData }: { rowData: TransactionDto }) => (
        <span style={{ color: rowData.count < 0 ? 'red' : rowData.count > 0 ? 'green' : undefined }}>{fmtNum3(rowData.count)}</span>
      ),
    },
    { id: 'sharePrice', header: 'Share Price', accessor: (row: TransactionDto) => fmtNum2(row.sharePrice), sortType: 'number' as const, alignment: 'right' as const, width: 110, minWidth: 100 },
  ];

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Transactions</Heading>

      <Flex gap={16} alignItems="center" flexWrap="wrap">
        <TextInput placeholder="Filter ISIN..." value={isinFilter}
          onChange={(v: string) => { setIsinFilter(v); table.setPage(1); }} />
        <TextInput placeholder="Filter Name..." value={nameFilter}
          onChange={(v: string) => { setNameFilter(v); table.setPage(1); }} />
        <Select value={depotFilter || '__all'} onChange={(v: string) => { setDepotFilter(v === '__all' ? '' : v); table.setPage(1); }}>
          <Select.Content>
            <Select.Option value="__all">All depots</Select.Option>
            {depotOptions.map(d => <Select.Option key={d} value={d}>{d}</Select.Option>)}
          </Select.Content>
        </Select>
        <Button variant="default" onClick={() => { setIsinFilter(''); setNameFilter(''); setDepotFilter(''); table.setPage(1); }}>Clear</Button>
      </Flex>

      <Flex alignItems="center" justifyContent="space-between">
        <Flex gap={16} alignItems="center">
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
            {filteredCount} transactions
          </Paragraph>
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
            Sum: <span style={{ color: sumCount < 0 ? 'red' : sumCount > 0 ? 'green' : undefined }}>
              {fmtNum3(sumCount)}
            </span>
          </Paragraph>
        </Flex>
        <Flex gap={8} alignItems="center">
          <ExportButtons endpoint="/transactions/export" params={table.exportParams} />
          <Button variant="default" onClick={table.handleShowAll}>
            {table.pageSize === -1 ? 'Paginate' : 'Show All'}
          </Button>
        </Flex>
      </Flex>

      {table.loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading transactions" size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
        </Flex>
      ) : (
        <>
          <DataTable data={table.data} columns={columns} sortable={{ manualSort: true }} resizable fullWidth
            sortBy={table.sortBy} onSortByChange={table.handleSortChange} />
          {table.pageSize !== -1 && (
            <PaginationControls page={table.page} totalPages={table.totalPages} pageSize={table.pageSize}
              onPageChange={table.setPage} onPageSizeChange={table.handlePageSizeChange} />
          )}
        </>
      )}
    </Flex>
  );
}
