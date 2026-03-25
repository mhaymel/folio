import { useEffect, useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable, DataTablePagination } from '@dynatrace/strato-components/tables';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { Depot } from '../types';

const columns = [
  { id: 'name', header: 'Depot', accessor: 'name', sortType: 'text' as const },
];

export default function Depots() {
  const [depots, setDepots] = useState<Depot[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.get<Depot[]>('/depots')
      .then(r => setDepots(r.data))
      .finally(() => setLoading(false));
  }, []);

  const data = useMemo(() => depots, [depots]);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Depots</Heading>

      {loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading depots" size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
        </Flex>
      ) : (
        <>
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
            {depots.length} depots
          </Paragraph>
          <DataTable data={data} columns={columns} sortable resizable fullWidth defaultSortBy={[{ id: 'name', desc: false }]}>
            <DataTablePagination defaultPageSize={10} pageSizeOptions={[10, 20, 50, 100]} />
          </DataTable>
        </>
      )}
    </Flex>
  );
}