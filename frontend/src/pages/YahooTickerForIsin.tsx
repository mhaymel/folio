import { useState, useMemo } from 'react';
import { useYahooIsin } from '../context/YahooIsinContext';
import { Flex } from '@dynatrace/strato-components/layouts';
import { Heading, Paragraph } from '@dynatrace/strato-components/typography';
import { DataTable } from '@dynatrace/strato-components/tables';
import { Button } from '@dynatrace/strato-components/buttons';
import { ProgressCircle } from '@dynatrace/strato-components/content';
import { Modal } from '@dynatrace/strato-components/overlays';
import api from '../api/client';
import type { YahooIsinDuplicateTickerItem, YahooIsinFetchResult, YahooIsinSaveResult, YahooIsinWithTickerItem, YahooIsinWithoutTickerItem } from '../types';
import IsinCell from '../components/IsinCell';
import PaginationControls from '../components/PaginationControls';
import LabeledInput from '../components/LabeledInput';

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

const duplicateTickerColumns = [
  { id: 'tickerSymbol', header: 'Ticker Symbol', accessor: 'tickerSymbol', sortType: 'text' as const, alignment: 'left' as const, width: 140, minWidth: 100 },
  {
    id: 'isin', header: 'ISIN', accessor: 'isin', sortType: 'text' as const,
    alignment: 'left' as const, width: 140, minWidth: 140,
    cell: ({ rowData }: { rowData: YahooIsinDuplicateTickerItem }) => <IsinCell isin={rowData.isin} />,
  },
  { id: 'name', header: 'Name', accessor: 'name', sortType: 'text' as const, alignment: 'left' as const, width: 300, minWidth: 200 },
];

export default function YahooTickerForIsin() {
  const {
    withTicker, setWithTicker,
    withoutTicker, setWithoutTicker,
    duplicateTickers, setDuplicateTickers,
    fetched, setFetched,
    status, setStatus,
    clear,
  } = useYahooIsin();

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState(false);
  const [isinFilter, setIsinFilter] = useState('');
  const [tickerFilter, setTickerFilter] = useState('');
  const [nameFilter, setNameFilter] = useState('');

  const filteredWithTicker = useMemo(() => {
    let data = withTicker;
    if (isinFilter) data = data.filter(r => r.isin.toLowerCase().includes(isinFilter.toLowerCase()));
    if (tickerFilter) data = data.filter(r => r.tickerSymbol.toLowerCase().includes(tickerFilter.toLowerCase()));
    if (nameFilter) data = data.filter(r => r.name?.toLowerCase().includes(nameFilter.toLowerCase()));
    return data;
  }, [withTicker, isinFilter, tickerFilter, nameFilter]);

  const filteredWithoutTicker = useMemo(() => {
    let data = withoutTicker;
    if (isinFilter) data = data.filter(r => r.isin.toLowerCase().includes(isinFilter.toLowerCase()));
    if (nameFilter) data = data.filter(r => r.name?.toLowerCase().includes(nameFilter.toLowerCase()));
    return data;
  }, [withoutTicker, isinFilter, nameFilter]);

  const filteredDuplicateTickers = useMemo(() => {
    let data = duplicateTickers;
    if (isinFilter) data = data.filter(r => r.isin.toLowerCase().includes(isinFilter.toLowerCase()));
    if (tickerFilter) data = data.filter(r => r.tickerSymbol.toLowerCase().includes(tickerFilter.toLowerCase()));
    if (nameFilter) data = data.filter(r => r.name?.toLowerCase().includes(nameFilter.toLowerCase()));
    return data;
  }, [duplicateTickers, isinFilter, tickerFilter, nameFilter]);

  const wt = usePagination(filteredWithTicker);
  const wot = usePagination(filteredWithoutTicker);
  const dt = usePagination(filteredDuplicateTickers);

  const clearFilters = () => { setIsinFilter(''); setTickerFilter(''); setNameFilter(''); };

  const handleFetch = async () => {
    setLoading(true);
    setStatus('');
    clearFilters();
    try {
      const r = await api.post<YahooIsinFetchResult>('/yahoo-ticker-for-isin/fetch');
      setWithTicker(r.data.withTicker);
      setWithoutTicker(r.data.withoutTicker);
      setDuplicateTickers(r.data.duplicateTickers);
      setFetched(true);
      setStatus(`${r.data.withTicker.length} found, ${r.data.withoutTicker.length} not found.`);
    } catch {
      setStatus('Fetch failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    setStatus('');
    try {
      const r = await api.post<YahooIsinSaveResult>('/yahoo-ticker-for-isin/save', withTicker);
      setStatus(`${r.data.created} created, ${r.data.updated} updated.`);
    } catch {
      setSaveError(true);
    } finally {
      setSaving(false);
    }
  };

  const handleClear = () => { clear(); clearFilters(); };

  return (
    <Flex flexDirection="column" gap={16}>
      <Modal
        show={saveError}
        title="Save failed"
        onDismiss={() => setSaveError(false)}
        footer={<Button onClick={() => setSaveError(false)}>Close</Button>}
      >
        <Paragraph>Ticker symbols could not be saved. Please try again.</Paragraph>
      </Modal>

      <Heading level={1}>Yahoo Ticker for ISIN</Heading>

      <Flex alignItems="center" gap={12}>
        <Button variant="emphasized" onClick={handleFetch} disabled={loading || saving}>
          {loading ? 'Fetching...' : 'Fetch from Yahoo'}
        </Button>
        <Button variant="emphasized" onClick={handleSave} disabled={!fetched || saving || loading}>
          {saving ? 'Saving...' : 'Save'}
        </Button>
        <Button variant="emphasized" onClick={handleClear} disabled={loading || saving}>
          Clear
        </Button>
        {(loading || saving) && <ProgressCircle aria-label="Loading" size="small" />}
        {status && <Paragraph>{status}</Paragraph>}
      </Flex>

      {fetched && (
        <>
          <Flex gap={16} alignItems="flex-end" flexWrap="wrap">
            <LabeledInput label="ISIN" value={isinFilter} onChange={setIsinFilter} />
            <LabeledInput label="Ticker Symbol" value={tickerFilter} onChange={setTickerFilter} />
            <LabeledInput label="Name" value={nameFilter} onChange={setNameFilter} />
            <Button variant="emphasized" onClick={clearFilters}>Clear Filters</Button>
          </Flex>

          <Heading level={2}>ISINs with Ticker Symbol</Heading>

          <Flex alignItems="center" justifyContent="space-between">
            <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>{filteredWithTicker.length} ISINs with ticker symbol</Paragraph>
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
            <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>{filteredWithoutTicker.length} ISINs without ticker symbol</Paragraph>
            <Button variant="emphasized" onClick={wot.handleShowAll}>
              {wot.pageSize === -1 ? 'Paginate' : 'Show All'}
            </Button>
          </Flex>

          <DataTable data={wot.pageData} columns={withoutTickerColumns} sortable resizable fullWidth />
          {wot.pageSize !== -1 && (
            <PaginationControls page={wot.page} totalPages={wot.totalPages} pageSize={wot.pageSize}
              onPageChange={wot.handlePageChange} onPageSizeChange={wot.handlePageSizeChange} />
          )}

          {duplicateTickers.length > 0 && (
            <>
              <Heading level={2}>Duplicate Ticker Symbols</Heading>

              <Flex alignItems="center" justifyContent="space-between">
                <Paragraph style={{ color: 'var(--dt-color-text-subdued)' }}>{filteredDuplicateTickers.length} rows — ticker symbols assigned to more than one ISIN</Paragraph>
                <Button variant="emphasized" onClick={dt.handleShowAll}>
                  {dt.pageSize === -1 ? 'Paginate' : 'Show All'}
                </Button>
              </Flex>

              <DataTable data={dt.pageData} columns={duplicateTickerColumns} sortable resizable fullWidth />
              {dt.pageSize !== -1 && (
                <PaginationControls page={dt.page} totalPages={dt.totalPages} pageSize={dt.pageSize}
                  onPageChange={dt.handlePageChange} onPageSizeChange={dt.handlePageSizeChange} />
              )}
            </>
          )}
        </>
      )}
    </Flex>
  );
}
