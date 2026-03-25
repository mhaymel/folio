import { useEffect, useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable, DataTablePagination } from '@dynatrace/strato-components/tables';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { IsinNameDto } from '../types';

const columns = [
  { id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const, width: 140, minWidth: 140 },
  { id: 'name', header: 'Name', accessor: 'name', sortType: 'text' as const, width: 400, minWidth: 200 },
];

export default function IsinNames() {
  const [data, setData] = useState<IsinNameDto[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.get<IsinNameDto[]>('/isin-names')
      .then(r => setData(r.data))
      .finally(() => setLoading(false));
  }, []);

  const tableData = useMemo(() => data, [data]);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>ISIN Names</Heading>

      {loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading ISIN names" size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
        </Flex>
      ) : (
        <>
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
            {data.length} ISIN name mappings
          </Paragraph>
          <DataTable
          data={tableData}
          columns={columns}
          sortable
          resizable
          fullWidth
          defaultSortBy={[{ id: 'name', desc: false }]}
        >
          <DataTablePagination defaultPageSize={10} pageSizeOptions={[10, 20, 50, 100]} />
          </DataTable>
        </>
      )}
    </Flex>
  );
}

