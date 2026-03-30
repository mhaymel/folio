import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Transactions from './pages/Transactions';
import Stocks from './pages/Stocks';
import StocksPerDepot from './pages/StocksPerDepot';
import Countries from './pages/Countries';
import Branches from './pages/Branches';
import Depots from './pages/Depots';
import Currencies from './pages/Currencies';
import TickerSymbols from './pages/TickerSymbols';
import IsinNames from './pages/IsinNames';
import Analytics from './pages/Analytics';
import Import from './pages/Import';
import DividendPayments from './pages/DividendPayments';
import Settings from './pages/Settings';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/transactions" element={<Transactions />} />
          <Route path="/stocks" element={<Stocks />} />
          <Route path="/stocks-per-depot" element={<StocksPerDepot />} />
          <Route path="/countries" element={<Countries />} />
          <Route path="/branches" element={<Branches />} />
          <Route path="/depots" element={<Depots />} />
          <Route path="/currencies" element={<Currencies />} />
          <Route path="/ticker-symbols" element={<TickerSymbols />} />
          <Route path="/isin-names" element={<IsinNames />} />
          <Route path="/dividend-payments" element={<DividendPayments />} />
          <Route path="/analytics/:type" element={<Analytics />} />
          <Route path="/import" element={<Import />} />
          <Route path="/settings" element={<Settings />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
