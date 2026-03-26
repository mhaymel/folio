import { useEffect, useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable, DataTablePagination } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { Currency } from '../types';

const columns = [
  { id: 'name', header: 'Currency', accessor: 'name', sortType: 'text' as const },
];

export default function Currencies() {
  const [currencies, setCurrencies] = useState<Currency[]>([]);
  const [loading, setLoading] = useState(false);
  const [showAll, setShowAll] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.get<Currency[]>('/currencies')
      .then(r => setCurrencies(r.data))
      .finally(() => setLoading(false));
  }, []);

  const data = useMemo(() => currencies, [currencies]);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Currencies</Heading>

      {loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading currencies" size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
        </Flex>
      ) : (
        <>
          <Flex alignItems="center" justifyContent="space-between">
            <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
              {currencies.length} currencies
            </Paragraph>
            <Button variant="default" onClick={() => setShowAll(s => !s)}>
              {showAll ? 'Paginate' : 'Show All'}
            </Button>
          </Flex>
          <DataTable data={data} columns={columns} sortable resizable fullWidth defaultSortBy={[{ id: 'name', desc: false }]}>
            {!showAll && (
              <DataTablePagination defaultPageSize={10} pageSizeOptions={[10, 20, 50, 100]} />
            )}
          </DataTable>
        </>
      )}
    </Flex>
  );
}