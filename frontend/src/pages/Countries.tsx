import { useEffect, useMemo, useState } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable, DataTablePagination } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { Country } from '../types';
import ExportButtons from '../components/ExportButtons';

const columns = [
  { id: 'name', header: 'Country', accessor: 'name', sortType: 'text' as const },
];

export default function Countries() {
  const [countries, setCountries] = useState<Country[]>([]);
  const [loading, setLoading] = useState(false);
  const [showAll, setShowAll] = useState(false);
  const [sortField, setSortField] = useState('name');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');

  useEffect(() => {
    setLoading(true);
    api.get<Country[]>('/countries')
      .then(r => setCountries(r.data))
      .finally(() => setLoading(false));
  }, []);

  const data = useMemo(() => countries, [countries]);

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Countries</Heading>

      {loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading countries" size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
        </Flex>
      ) : (
        <>
          <Flex alignItems="center" justifyContent="space-between">
            <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>
              {countries.length} countries
            </Paragraph>
            <Flex gap={8} alignItems="center">
              <ExportButtons endpoint="/countries/export" params={{ sortField, sortDir }} />
              <Button variant="default" onClick={() => setShowAll(s => !s)}>
                {showAll ? 'Paginate' : 'Show All'}
              </Button>
            </Flex>
          </Flex>
          <DataTable data={data} columns={columns} sortable resizable fullWidth defaultSortBy={[{ id: 'name', desc: false }]}
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