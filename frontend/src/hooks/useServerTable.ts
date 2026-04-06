import { useEffect, useState, useCallback, useRef } from 'react';
import api from '../api/client';
import type { PaginatedResponse } from '../types';

export interface UseServerTableOptions<T, R extends PaginatedResponse<T> = PaginatedResponse<T>> {
  endpoint: string;
  defaultSortField: string;
  defaultSortDir?: 'asc' | 'desc';
  defaultPageSize?: number;
  extraParams?: Record<string, string | number>;
  responseMapper?: (response: R) => void;
  loadOnMount?: boolean;
}

export interface UseServerTableReturn<T> {
  data: T[];
  totalItems: number;
  totalPages: number;
  loading: boolean;
  page: number;
  pageSize: number;
  sortField: string;
  sortDir: 'asc' | 'desc';
  setPage: (p: number) => void;
  setPageSize: (s: number) => void;
  handleSortChange: (s: any) => void;
  handleShowAll: () => void;
  handlePageSizeChange: (size: number) => void;
  reload: () => void;
  clear: () => void;
  sortBy: { id: string; desc: boolean }[];
  exportParams: Record<string, string>;
}

function loadPersistedState(key: string) {
  try {
    const raw = sessionStorage.getItem(`table_state_${key}`);
    if (raw) return JSON.parse(raw);
  } catch { /* ignore */ }
  return null;
}

function persistState(key: string, state: Record<string, unknown>) {
  try {
    sessionStorage.setItem(`table_state_${key}`, JSON.stringify(state));
  } catch { /* ignore */ }
}

export default function useServerTable<T, R extends PaginatedResponse<T> = PaginatedResponse<T>>(
  options: UseServerTableOptions<T, R>,
): UseServerTableReturn<T> {
  const {
    endpoint,
    defaultSortField,
    defaultSortDir = 'asc',
    defaultPageSize = 10,
    extraParams,
    responseMapper,
    loadOnMount = true,
  } = options;

  const persisted = useRef(loadPersistedState(endpoint));

  const [data, setData] = useState<T[]>([]);
  const [totalItems, setTotalItems] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(persisted.current?.page ?? 1);
  const [pageSize, setPageSize] = useState(persisted.current?.pageSize ?? defaultPageSize);
  const [sortField, setSortField] = useState(persisted.current?.sortField ?? defaultSortField);
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>(persisted.current?.sortDir ?? defaultSortDir);

  // Stable reference for extraParams to avoid infinite re-render loops
  const extraParamsKey = JSON.stringify(extraParams ?? {});
  const extraParamsRef = useRef(extraParams);
  extraParamsRef.current = extraParams;

  // Track whether a load has been triggered at least once (used when loadOnMount=false)
  const hasLoadedRef = useRef(loadOnMount);

  // Persist state on changes
  useEffect(() => {
    persistState(endpoint, { page, pageSize, sortField, sortDir });
  }, [endpoint, page, pageSize, sortField, sortDir]);

  const load = useCallback(async () => {
    hasLoadedRef.current = true;
    setLoading(true);
    try {
      const params: Record<string, string | number> = { sortField, sortDir, page, pageSize };
      if (extraParamsRef.current) {
        Object.assign(params, extraParamsRef.current);
      }
      const r = await api.get<R>(endpoint, { params });
      setData(r.data.items);
      setTotalItems(r.data.totalItems);
      setTotalPages(r.data.totalPages);
      if (responseMapper) {
        responseMapper(r.data);
      }
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [endpoint, sortField, sortDir, page, pageSize, extraParamsKey]);

  useEffect(() => { if (hasLoadedRef.current) load(); }, [load]);

  const handleSortChange = useCallback((s: any) => {
    if (s?.[0]) {
      setSortField(s[0].id);
      setSortDir(s[0].desc ? 'desc' : 'asc');
    } else {
      setSortDir(d => d === 'asc' ? 'desc' : 'asc');
    }
    setPage(1);
  }, []);

  const handleShowAll = useCallback(() => {
    if (pageSize === -1) {
      setPageSize(defaultPageSize);
      setPage(1);
    } else {
      setPageSize(-1);
    }
  }, [pageSize, defaultPageSize]);

  const handlePageSizeChange = useCallback((size: number) => {
    setPageSize(size);
    setPage(1);
  }, []);

  const clear = useCallback(() => {
    hasLoadedRef.current = false;
    setData([]);
    setTotalItems(0);
    setTotalPages(1);
  }, []);

  const sortBy = [{ id: sortField, desc: sortDir === 'desc' }];

  const exportParams: Record<string, string> = { sortField, sortDir };
  if (extraParams) {
    for (const [key, val] of Object.entries(extraParams)) {
      if (val !== undefined && val !== '' && val !== 0) {
        exportParams[key] = String(val);
      }
    }
  }

  return {
    data,
    totalItems,
    totalPages,
    loading,
    page,
    pageSize,
    sortField,
    sortDir,
    setPage,
    setPageSize,
    handleSortChange,
    handleShowAll,
    handlePageSizeChange,
    reload: load,
    clear,
    sortBy,
    exportParams,
  };
}
