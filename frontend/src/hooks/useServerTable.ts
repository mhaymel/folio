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
  sortBy: { id: string; desc: boolean }[];
  exportParams: Record<string, string>;
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
  } = options;

  const [data, setData] = useState<T[]>([]);
  const [totalItems, setTotalItems] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(defaultPageSize);
  const [sortField, setSortField] = useState(defaultSortField);
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>(defaultSortDir);

  // Stable reference for extraParams to avoid infinite re-render loops
  const extraParamsKey = JSON.stringify(extraParams ?? {});
  const extraParamsRef = useRef(extraParams);
  extraParamsRef.current = extraParams;

  const load = useCallback(async () => {
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

  useEffect(() => { load(); }, [load]);

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
    sortBy,
    exportParams,
  };
}
