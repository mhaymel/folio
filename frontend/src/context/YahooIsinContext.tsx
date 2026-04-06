import { createContext, useContext, useState } from 'react';
import type { ReactNode } from 'react';
import type { YahooIsinDuplicateTickerItem, YahooIsinWithTickerItem, YahooIsinWithoutTickerItem } from '../types';

interface YahooIsinState {
  withTicker: YahooIsinWithTickerItem[];
  withoutTicker: YahooIsinWithoutTickerItem[];
  duplicateTickers: YahooIsinDuplicateTickerItem[];
  fetched: boolean;
  status: string;
  setWithTicker: (v: YahooIsinWithTickerItem[]) => void;
  setWithoutTicker: (v: YahooIsinWithoutTickerItem[]) => void;
  setDuplicateTickers: (v: YahooIsinDuplicateTickerItem[]) => void;
  setFetched: (v: boolean) => void;
  setStatus: (v: string) => void;
  clear: () => void;
}

const YahooIsinContext = createContext<YahooIsinState | null>(null);

export function YahooIsinProvider({ children }: { children: ReactNode }) {
  const [withTicker, setWithTicker] = useState<YahooIsinWithTickerItem[]>([]);
  const [withoutTicker, setWithoutTicker] = useState<YahooIsinWithoutTickerItem[]>([]);
  const [duplicateTickers, setDuplicateTickers] = useState<YahooIsinDuplicateTickerItem[]>([]);
  const [fetched, setFetched] = useState(false);
  const [status, setStatus] = useState('');

  const clear = () => {
    setWithTicker([]);
    setWithoutTicker([]);
    setDuplicateTickers([]);
    setFetched(false);
    setStatus('');
  };

  return (
    <YahooIsinContext.Provider value={{
      withTicker, withoutTicker, duplicateTickers, fetched, status,
      setWithTicker, setWithoutTicker, setDuplicateTickers, setFetched, setStatus,
      clear,
    }}>
      {children}
    </YahooIsinContext.Provider>
  );
}

export function useYahooIsin(): YahooIsinState {
  const ctx = useContext(YahooIsinContext);
  if (!ctx) throw new Error('useYahooIsin must be used within YahooIsinProvider');
  return ctx;
}
