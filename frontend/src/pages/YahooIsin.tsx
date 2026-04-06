import { useState, useMemo } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { YahooIsinFetchResult, YahooIsinWithTickerItem, YahooIsinWithoutTickerItem } from '../types';
import IsinCell from '../components/IsinCell';
import PaginationControls from '../components/PaginationControls';

const DEFAULT_PAGE_SIZE = 10;

function usePagination<T>(data: T[]) {
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);

  const totalPages = pageSize === -1 ? 1 : Math.max(1, Math.ceil(data.length / pageSize));
  const safePage = Math.min(page, totalPages);

  const pageData = useMemo(() => {
    if (pageSize === -1) return data;
    const start = (safePage - 1) * pageSize;
    return data.slice(start, start + pageSize);
  }, [data, safePage, pageSize]);

  const handlePageChange = (p: number) => setPage(p);
  const handlePageSizeChange = (size: number) => { setPageSize(size); setPage(1); };
  const handleShowAll = () => { setPageSize(s => s === -1 ? DEFAULT_PAGE_SIZE : -1); setPage(1); };

  return { pageData, page: safePage, pageSize, totalPages, handlePageChange, handlePageSizeChange, handleShowAll };
}

const withTickerColumns = [
  {
    id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const,
    alignment: 'left' as const, width: 140, minWidth: 140,
    cell: ({ rowData }: { rowData: YahooIsinWithTickerItem }) => <IsinCell isin={rowData.isin} />,
  },
  { id: 'tickerSymbol', header: 'Ticker Symbol', accessor: 'tickerSymbol', sortType: 'text' as const, alignment: 'left' as const, width: 140, minWidth: 100 },
  { id: 'name', header: 'Name', accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 300, minWidth: 200 },
];

const withoutTickerColumns = [
  {
    id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const,
    alignment: 'left' as const, width: 140, minWidth: 140,
    cell: ({ rowData }: { rowData: YahooIsinWithoutTickerItem }) => <IsinCell isin={rowData.isin} />,
  },
  { id: 'name', header: 'Name', accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 300, minWidth: 200 },
];

export default function YahooIsin() {
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState('');
  const [withTicker, setWithTicker] = useState<YahooIsinWithTickerItem[]>([]);
  const [withoutTicker, setWithoutTicker] = useState<YahooIsinWithoutTickerItem[]>([]);
  const [fetched, setFetched] = useState(false);

  const wt = usePagination(withTicker);
  const wot = usePagination(withoutTicker);

  const handleFetch = async () => {
    setLoading(true);
    setStatus('');
    try {
      const r = await api.post<YahooIsinFetchResult>('/yahoo-isin/fetch');
      setWithTicker(r.data.withTicker);
      setWithoutTicker(r.data.withoutTicker);
      setFetched(true);
      setStatus(`${r.data.withTicker.length} found, ${r.data.withoutTicker.length} not found.`);
    } catch {
      setStatus('Fetch failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleClear = () => {
    setWithTicker([]);
    setWithoutTicker([]);
    setFetched(false);
    setStatus('');
  };

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Yahoo ISIN</Heading>

      <Flex alignItems="center" gap={12}>
        <Button variant="emphasized" onClick={handleFetch} disabled={loading}>
          {loading ? 'Fetching...' : 'Fetch from Yahoo'}
        </Button>
        <Button variant="emphasized" onClick={handleClear} disabled={loading}>
          Clear
        </Button>
        {loading && <ProgressCircle aria-label="Fetching" size="small" />}
        {status && <Paragraph>{status}</Paragraph>}
      </Flex>

      {fetched && (
        <>
          <Heading level={2}>ISINs with Ticker Symbol</Heading>

          <Flex alignItems="center" justifyContent="space-between">
            <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>{withTicker.length} ISINs with ticker symbol</Paragraph>
            <Button variant="emphasized" onClick={wt.handleShowAll}>
              {wt.pageSize === -1 ? 'Paginate' : 'Show All'}
            </Button>
          </Flex>

          <DataTable data={wt.pageData} columns={withTickerColumns} sortable resizable fullWidth />
          {wt.pageSize !== -1 && (
            <PaginationControls page={wt.page} totalPages={wt.totalPages} pageSize={wt.pageSize}
              onPageChange={wt.handlePageChange} onPageSizeChange={wt.handlePageSizeChange} />
          )}

          <Heading level={2}>ISINs without Ticker Symbol</Heading>

          <Flex alignItems="center" justifyContent="space-between">
            <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>{withoutTicker.length} ISINs without ticker symbol</Paragraph>
            <Button variant="emphasized" onClick={wot.handleShowAll}>
              {wot.pageSize === -1 ? 'Paginate' : 'Show All'}
            </Button>
          </Flex>

          <DataTable data={wot.pageData} columns={withoutTickerColumns} sortable resizable fullWidth />
          {wot.pageSize !== -1 && (
            <PaginationControls page={wot.page} totalPages={wot.totalPages} pageSize={wot.pageSize}
              onPageChange={wot.handlePageChange} onPageSizeChange={wot.handlePageSizeChange} />
          )}
        </>
      )}
    </Flex>
  );
}
