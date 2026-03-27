import React, { useEffect, useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable, DataTablePagination } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { TextInput, Select } from '@dynatrace/strato-components/forms';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { TransactionDto } from '../types';
import ExportButtons from '../components/ExportButtons';

const fmtPrice = (n: number) =>
  n.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const fmtCount = (n: number) =>
  n.toLocaleString('de-DE', { minimumFractionDigits: 3, maximumFractionDigits: 3 });

// ISO date → DD-MM-YYYY (avoids timezone shifts; raw ISO used for sort)
const isoDate = (s: string) => s.substring(0, 10);
const fmtDate = (s: string) => {
  const d = isoDate(s);
  return `${d.substring(8, 10)}-${d.substring(5, 7)}-${d.substring(0, 4)}`;
};


export default function Transactions() {
  const [allTxns, setAllTxns] = useState<TransactionDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [isinFilter, setIsinFilter] = useState('');
  const [nameFilter, setNameFilter] = useState('');
  const [depotFilter, setDepotFilter] = useState('');
  const [showAll, setShowAll] = useState(false);
  const [sortField, setSortField] = useState('date');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc');

  const cellStyle: React.CSSProperties = { display: 'flex', alignItems: 'center', height: '100%', cursor: 'pointer' };

  const columns = useMemo(() => [
    { id: 'date', header: 'Date', accessor: (r: TransactionDto) => fmtDate(r.date), sortAccessor: (r: TransactionDto) => isoDate(r.date), sortType: 'text' as const, width: 105, minWidth: 105 },
    { id: 'isin', header: 'ISIN', accessor: (r: TransactionDto) => r.isin, cell: (info: { value: string }) => (
      <span style={cellStyle} onDoubleClick={() => setIsinFilter(info.value)} title="Double-click to filter by this ISIN">{info.value}</span>
    ), sortType: 'text' as const, width: 140, minWidth: 140 },
    { id: 'name', header: 'Name', accessor: (r: TransactionDto) => r.name ?? '', cell: (info: { value: string }) => (
      <span style={cellStyle} onDoubleClick={() => setNameFilter(info.value)} title="Double-click to filter by this name">{info.value}</span>
    ), sortType: 'text' as const, width: 240, minWidth: 120 },
    { id: 'depot', header: 'Depot', accessor: 'depot', sortType: 'text' as const, width: 100, minWidth: 80 },
    { id: 'count', header: 'Count', accessor: (r: TransactionDto) => r.count, cell: (info: { value: number }) => (
      <span style={{ display: 'flex', alignItems: 'center', height: '100%', justifyContent: 'flex-end', color: info.value < 0 ? '#ff6b6b' : info.value > 0 ? '#51cf66' : undefined }}>{fmtCount(info.value)}</span>
    ), sortType: 'number' as const, alignment: 'right' as const, minWidth: 80 },
    { id: 'sharePrice', header: 'Share Price', accessor: (r: TransactionDto) => fmtPrice(r.sharePrice), sortType: 'number' as const, alignment: 'right' as const, minWidth: 100 },
  // eslint-disable-next-line react-hooks/exhaustive-deps
  ], []);

  const load = async () => {
    setLoading(true);
    try {
      const r = await api.get<TransactionDto[]>('/transactions');
      setAllTxns(r.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const depots = useMemo(
    () => [...new Set(allTxns.map(t => t.depot))].sort(),
    [allTxns]
  );

  const filteredTxns = useMemo(() => allTxns.filter(t => {
    const matchesIsin = !isinFilter || t.isin.toLowerCase().includes(isinFilter.toLowerCase());
    const matchesName = !nameFilter || (t.name ?? '').toLowerCase().includes(nameFilter.toLowerCase());
    const matchesDepot = !depotFilter || t.depot === depotFilter;
    return matchesIsin && matchesName && matchesDepot;
  }), [allTxns, isinFilter, nameFilter, depotFilter]);

  const exportParams = useMemo(() => ({
    isin: isinFilter || undefined,
    name: nameFilter || undefined,
    depot: depotFilter || undefined,
    sortField,
    sortDir,
  }), [isinFilter, nameFilter, depotFilter, sortField, sortDir]);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Transactions</Heading>

      <Flex gap={8} alignItems="center">
        <TextInput
          placeholder="Filter by ISIN"
          value={isinFilter}
          onChange={val => setIsinFilter(val ?? '')}
        />
        {isinFilter && (
          <Button variant="default" onClick={() => setIsinFilter('')}>Clear</Button>
        )}
        <TextInput
          placeholder="Filter by name"
          value={nameFilter}
          onChange={val => setNameFilter(val ?? '')}
        />
        {nameFilter && (
          <Button variant="default" onClick={() => setNameFilter('')}>Clear</Button>
        )}
        <Select<string> value={depotFilter || null} onChange={val => setDepotFilter(val ?? '')}>
          <Select.Content>
            <Select.Option value="">All depots</Select.Option>
            {depots.map(d => (
              <Select.Option key={d} value={d}>{d}</Select.Option>
            ))}
          </Select.Content>
        </Select>
        <Button onClick={load} variant="default">Refresh</Button>
      </Flex>

      {loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading transactions" size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading…</Paragraph>
        </Flex>
      ) : (
        <>
          <Flex alignItems="center" justifyContent="space-between">
            <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
              {filteredTxns.length !== allTxns.length
                ? `${filteredTxns.length} of ${allTxns.length} transactions`
                : `${allTxns.length} transactions`}
            </Paragraph>
            <Flex gap={8} alignItems="center">
              <ExportButtons endpoint="/transactions/export" params={exportParams} />
              <Button variant="default" onClick={() => setShowAll(s => !s)}>
                {showAll ? 'Paginate' : 'Show All'}
              </Button>
            </Flex>
          </Flex>
          <DataTable data={filteredTxns} columns={columns} sortable resizable fullWidth
            defaultSortBy={[{ id: 'date', desc: true }]}
            onSortByChange={(s: any) => { if (s?.[0]) { setSortField(s[0].id); setSortDir(s[0].desc ? 'desc' : 'asc'); } }}>
            {!showAll && (
              <DataTablePagination defaultPageSize={10} pageSizeOptions={[10, 20, 50, 100]} />
            )}
          </DataTable>
        </>
      )}
    </Flex>
  );
}