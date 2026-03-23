import { useEffect, useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { TextInput } from '@dynatrace/strato-components/forms';
import api from '../api/client';
import type { TransactionDto } from '../types';

const fmt = (n: number) =>
  n.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const columns = [
  { id: 'date', header: 'Date', accessor: (r: TransactionDto) => new Date(r.date).toLocaleDateString('de-DE'), sortType: 'text' as const },
  { id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const },
  { id: 'name', header: 'Name', accessor: 'name', sortType: 'text' as const },
  { id: 'depot', header: 'Depot', accessor: 'depot', sortType: 'text' as const },
  { id: 'count', header: 'Count', accessor: (r: TransactionDto) => fmt(r.count), sortType: 'number' as const, alignment: 'right' as const },
  { id: 'sharePrice', header: 'Share Price', accessor: (r: TransactionDto) => fmt(r.sharePrice), sortType: 'number' as const, alignment: 'right' as const },
];

export default function Transactions() {
  const [txns, setTxns] = useState<TransactionDto[]>([]);
  const [isinFilter, setIsinFilter] = useState('');
  const [depotFilter, setDepotFilter] = useState('');

  const load = () => {
    const params: Record<string, string> = {};
    if (isinFilter) params.isin = isinFilter;
    if (depotFilter) params.depot = depotFilter;
    api.get<TransactionDto[]>('/transactions', { params }).then(r => setTxns(r.data));
  };

  useEffect(() => { load(); }, []);

  const data = useMemo(() => txns, [txns]);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Transactions</Heading>
      <div className="filter-bar">
        <TextInput
          placeholder="Filter by ISIN"
          value={isinFilter}
          onChange={val => setIsinFilter(val)}
          onKeyDown={e => e.key === 'Enter' && load()}
        />
        <TextInput
          placeholder="Filter by Depot"
          value={depotFilter}
          onChange={val => setDepotFilter(val)}
          onKeyDown={e => e.key === 'Enter' && load()}
        />
        <Button onClick={load} variant="default">Search</Button>
      </div>
      <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>{txns.length} transactions</Paragraph>
      <DataTable data={data} columns={columns} sortable fullWidth />
    </Flex>
  );
}