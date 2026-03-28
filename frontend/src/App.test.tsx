import { describe, it, expect, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';

// Mock all page components to simple stubs
vi.mock('./pages/Dashboard', () => ({ default: () => <div>Dashboard Stub</div> }));
vi.mock('./pages/Transactions', () => ({ default: () => <div>Transactions Stub</div> }));
vi.mock('./pages/Stocks', () => ({ default: () => <div>Stocks Stub</div> }));
vi.mock('./pages/Countries', () => ({ default: () => <div>Countries Stub</div> }));
vi.mock('./pages/Branches', () => ({ default: () => <div>Branches Stub</div> }));
vi.mock('./pages/Depots', () => ({ default: () => <div>Depots Stub</div> }));
vi.mock('./pages/Currencies', () => ({ default: () => <div>Currencies Stub</div> }));
vi.mock('./pages/TickerSymbols', () => ({ default: () => <div>TickerSymbols Stub</div> }));
vi.mock('./pages/IsinNames', () => ({ default: () => <div>IsinNames Stub</div> }));
vi.mock('./pages/Analytics', () => ({ default: () => <div>Analytics Stub</div> }));
vi.mock('./pages/Import', () => ({ default: () => <div>Import Stub</div> }));
vi.mock('./pages/Settings', () => ({ default: () => <div>Settings Stub</div> }));

// Import the actual page mocks after mocking
import Dashboard from './pages/Dashboard';
import Transactions from './pages/Transactions';
import Stocks from './pages/Stocks';
import Countries from './pages/Countries';
import Branches from './pages/Branches';
import Depots from './pages/Depots';
import Currencies from './pages/Currencies';
import TickerSymbols from './pages/TickerSymbols';
import IsinNames from './pages/IsinNames';
import Analytics from './pages/Analytics';
import Import from './pages/Import';
import Settings from './pages/Settings';

function renderApp(route: string) {
  return render(
    <MemoryRouter initialEntries={[route]}>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/transactions" element={<Transactions />} />
          <Route path="/stocks" element={<Stocks />} />
          <Route path="/countries" element={<Countries />} />
          <Route path="/branches" element={<Branches />} />
          <Route path="/depots" element={<Depots />} />
          <Route path="/currencies" element={<Currencies />} />
          <Route path="/ticker-symbols" element={<TickerSymbols />} />
          <Route path="/isin-names" element={<IsinNames />} />
          <Route path="/analytics/:type" element={<Analytics />} />
          <Route path="/import" element={<Import />} />
          <Route path="/settings" element={<Settings />} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe('App routing', () => {
  const routes: [string, string][] = [
    ['/', 'Dashboard Stub'],
    ['/transactions', 'Transactions Stub'],
    ['/stocks', 'Stocks Stub'],
    ['/countries', 'Countries Stub'],
    ['/branches', 'Branches Stub'],
    ['/depots', 'Depots Stub'],
    ['/currencies', 'Currencies Stub'],
    ['/ticker-symbols', 'TickerSymbols Stub'],
    ['/isin-names', 'IsinNames Stub'],
    ['/analytics/countries', 'Analytics Stub'],
    ['/analytics/branches', 'Analytics Stub'],
    ['/import', 'Import Stub'],
    ['/settings', 'Settings Stub'],
  ];

  it.each(routes)('route %s renders %s', async (path, expectedText) => {
    renderApp(path);
    await waitFor(() => {
      expect(screen.getByText(expectedText)).toBeInTheDocument();
    });
  });
});
