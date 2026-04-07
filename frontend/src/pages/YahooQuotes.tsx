import { useState, useMemo } from 'react';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import api from '../api/client';
import type { YahooFetchResultDto, YahooQuoteWithQuoteDto, YahooQuoteWithoutQuoteDto } from '../types';
import useServerTable from '../hooks/useServerTable';
import ExportButtons from '../components/ExportButtons';
import PaginationControls from '../components/PaginationControls';
import LabeledInput from '../components/LabeledInput';
import IsinCell from '../components/IsinCell';

const fmtPrice = (v: number | null) =>
  v != null ? v.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '\u2014';

export default function YahooQuotes() {
  const [fetchStatus, setFetchStatus] = useState(() => sessionStorage.getItem('yahoo_fetch_status') ?? '');
  const [loading, setLoading] = useState(false);

  const saveFetchStatus = (msg: string) => {
    setFetchStatus(msg);
    try { sessionStorage.setItem('yahoo_fetch_status', msg); } catch { /* ignore */ }
  };

  // With-quote filters
  const [wqIsin, setWqIsin] = useState('');
  const [wqName, setWqName] = useState('');
  const [wqTicker, setWqTicker] = useState('');
  const [wqCurrency, setWqCurrency] = useState('');

  // Without-quote filters
  const [woqIsin, setWoqIsin] = useState('');
  const [woqName, setWoqName] = useState('');
  const [woqTicker, setWoqTicker] = useState('');

  const wqExtraParams = useMemo(() => {
    const p: Record<string, string> = {};
    if (wqIsin) p.isin = wqIsin;
    if (wqName) p.name = wqName;
    if (wqTicker) p.ticker = wqTicker;
    if (wqCurrency) p.currency = wqCurrency;
    return p;
  }, [wqIsin, wqName, wqTicker, wqCurrency]);

  const woqExtraParams = useMemo(() => {
    const p: Record<string, string> = {};
    if (woqIsin) p.isin = woqIsin;
    if (woqName) p.name = woqName;
    if (woqTicker) p.ticker = woqTicker;
    return p;
  }, [woqIsin, woqName, woqTicker]);

  const wqTable = useServerTable<YahooQuoteWithQuoteDto>({
    endpoint: '/yahoo-quotes/with-quote',
    defaultSortField: 'isin',
    extraParams: wqExtraParams,
    loadOnMount: false,
  });

  const woqTable = useServerTable<YahooQuoteWithoutQuoteDto>({
    endpoint: '/yahoo-quotes/without-quote',
    defaultSortField: 'isin',
    extraParams: woqExtraParams,
    loadOnMount: false,
  });

  const handleFetch = async () => {
    setLoading(true);
    saveFetchStatus('');
    wqTable.clear();
    woqTable.clear();
    try {
      const r = await api.post<YahooFetchResultDto>('/yahoo-quotes/fetch');
      const { fetched, total, noTicker, noQuote } = r.data;
      const parts = [`${fetched} of ${total} quotes fetched`];
      if (noTicker > 0) parts.push(`${noTicker} no ticker`);
      if (noQuote > 0) parts.push(`${noQuote} no quote`);
      saveFetchStatus(parts.join(', ') + '.');
      wqTable.reload();
      woqTable.reload();
    } catch {
      saveFetchStatus('Fetch failed.');
    } finally {
      setLoading(false);
    }
  };

  const withQuoteColumns = [
    {
      id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const,
      alignment: 'left' as const, width: 140, minWidth: 140,
      cell: ({ rowData }: { rowData: YahooQuoteWithQuoteDto }) => <IsinCell isin={rowData.isin} />,
    },
    { id: 'name', header: 'Name', accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 260, minWidth: 160 },
    { id: 'tickerSymbol', header: 'Ticker', accessor: 'tickerSymbol', sortType: 'text' as const, alignment: 'left' as const, width: 80, minWidth: 60 },
    {
      id: 'price', header: 'Price', accessor: (r: YahooQuoteWithQuoteDto) => fmtPrice(r.price),
      sortType: 'number' as const, alignment: 'right' as const, width: 100, minWidth: 80,
    },
    { id: 'currency', header: 'Currency', accessor: 'currency', sortType: 'text' as const, alignment: 'left' as const, width: 80, minWidth: 60 },
    { id: 'provider', header: 'Provider', accessor: 'provider', sortType: 'text' as const, alignment: 'left' as const, width: 140, minWidth: 100 },
    { id: 'fetchedAt', header: 'Fetched At', accessor: 'fetchedAt', sortType: 'text' as const, alignment: 'left' as const, width: 160, minWidth: 120 },
  ];

  const withoutQuoteColumns = [
    {
      id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const,
      alignment: 'left' as const, width: 140, minWidth: 140,
      cell: ({ rowData }: { rowData: YahooQuoteWithoutQuoteDto }) => <IsinCell isin={rowData.isin} />,
    },
    { id: 'name', header: 'Name', accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 300, minWidth: 200 },
    { id: 'tickerSymbol', header: 'Ticker', accessor: 'tickerSymbol', sortType: 'text' as const, alignment: 'left' as const, width: 100, minWidth: 60 },
  ];

  return (
    <Flex flexDirection="column" gap={16}>
      <Heading level={1}>Yahoo Quotes</Heading>

      <Flex alignItems="center" gap={12}>
        <Button variant="emphasized" onClick={handleFetch} disabled={loading}>
          {loading ? 'Fetching...' : 'Fetch Quotes'}
        </Button>
        {fetchStatus && <Paragraph>{fetchStatus}</Paragraph>}
      </Flex>

      <Heading level={2}>ISINs with Quote</Heading>

      <Flex gap={16} alignItems="flex-end" flexWrap="wrap">
        <LabeledInput label="ISIN" value={wqIsin} onChange={(v: string) => { setWqIsin(v); wqTable.setPage(1); }} />
        <LabeledInput label="Name" value={wqName} onChange={(v: string) => { setWqName(v); wqTable.setPage(1); }} />
        <LabeledInput label="Ticker" value={wqTicker} onChange={(v: string) => { setWqTicker(v); wqTable.setPage(1); }} />
        <LabeledInput label="Currency" value={wqCurrency} onChange={(v: string) => { setWqCurrency(v); wqTable.setPage(1); }} />
        <Button variant="emphasized" onClick={() => { setWqIsin(''); setWqName(''); setWqTicker(''); setWqCurrency(''); wqTable.setPage(1); }}>Clear</Button>
      </Flex>

      <Flex alignItems="center" justifyContent="space-between">
        <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>{wqTable.totalItems} held ISINs with quote</Paragraph>
        <Flex gap={8} alignItems="center">
          <ExportButtons endpoint="/yahoo-quotes/with-quote/export" params={wqTable.exportParams} />
          <Button variant="emphasized" onClick={wqTable.handleShowAll}>
            {wqTable.pageSize === -1 ? 'Paginate' : 'Show All'}
          </Button>
        </Flex>
      </Flex>

      {wqTable.loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading" size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
        </Flex>
      ) : (
        <>
          <DataTable data={wqTable.data} columns={withQuoteColumns} sortable={{ manualSort: true }} resizable fullWidth
            sortBy={wqTable.sortBy} onSortByChange={wqTable.handleSortChange} />
          {wqTable.pageSize !== -1 && (
            <PaginationControls page={wqTable.page} totalPages={wqTable.totalPages} pageSize={wqTable.pageSize}
              onPageChange={wqTable.setPage} onPageSizeChange={wqTable.handlePageSizeChange} />
          )}
        </>
      )}

      <Heading level={2}>ISINs without Quote</Heading>

      <Flex gap={16} alignItems="flex-end" flexWrap="wrap">
        <LabeledInput label="ISIN" value={woqIsin} onChange={(v: string) => { setWoqIsin(v); woqTable.setPage(1); }} />
        <LabeledInput label="Name" value={woqName} onChange={(v: string) => { setWoqName(v); woqTable.setPage(1); }} />
        <LabeledInput label="Ticker" value={woqTicker} onChange={(v: string) => { setWoqTicker(v); woqTable.setPage(1); }} />
        <Button variant="emphasized" onClick={() => { setWoqIsin(''); setWoqName(''); setWoqTicker(''); woqTable.setPage(1); }}>Clear</Button>
      </Flex>

      <Flex alignItems="center" justifyContent="space-between">
        <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>{woqTable.totalItems} held ISINs without quote</Paragraph>
        <Flex gap={8} alignItems="center">
          <ExportButtons endpoint="/yahoo-quotes/without-quote/export" params={woqTable.exportParams} />
          <Button variant="emphasized" onClick={woqTable.handleShowAll}>
            {woqTable.pageSize === -1 ? 'Paginate' : 'Show All'}
          </Button>
        </Flex>
      </Flex>

      {woqTable.loading ? (
        <Flex alignItems="center" gap={12}>
          <ProgressCircle aria-label="Loading" size="small" />
          <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>Loading...</Paragraph>
        </Flex>
      ) : (
        <>
          <DataTable data={woqTable.data} columns={withoutQuoteColumns} sortable={{ manualSort: true }} resizable fullWidth
            sortBy={woqTable.sortBy} onSortByChange={woqTable.handleSortChange} />
          {woqTable.pageSize !== -1 && (
            <PaginationControls page={woqTable.page} totalPages={woqTable.totalPages} pageSize={woqTable.pageSize}
              onPageChange={woqTable.setPage} onPageSizeChange={woqTable.handlePageSizeChange} />
          )}
        </>
      )}
    </Flex>
  );
}
