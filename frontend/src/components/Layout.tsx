import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { AppHeader, Page } from '@dynatrace/strato-components/layouts';

const NAV_ITEMS = [
  { label: 'Dashboard', to: '/', end: true },
  { label: 'Transactions', to: '/transactions' },
  { label: 'Stocks', to: '/stocks' },
  { label: 'Stocks per Depot', to: '/stocks-per-depot' },
  { label: 'Countries', to: '/countries' },
  { label: 'Branches', to: '/branches' },
  { label: 'Depots', to: '/depots' },
  { label: 'Currencies', to: '/currencies' },
  { label: 'Ticker Symbols', to: '/ticker-symbols' },
  { label: 'ISIN Names', to: '/isin-names' },
  { label: 'Dividend Payments', to: '/dividend-payments' },
  { label: 'Country Analysis', to: '/analytics/countries' },
  { label: 'Branch Analysis', to: '/analytics/branches' },
  { label: 'Yahoo ISIN', to: '/yahoo-isin' },
  { label: 'Yahoo Quotes', to: '/yahoo-quotes' },
  { label: 'Import', to: '/import' },
  { label: 'Settings', to: '/settings' },
];

function SidebarNavItem({ label, to, end }: { label: string; to: string; end?: boolean }) {
  const location = useLocation();
  const navigate = useNavigate();
  const isSelected = end ? location.pathname === to : location.pathname.startsWith(to);
  return (
    <div
      className={`sidebar-nav-item${isSelected ? ' sidebar-nav-item--selected' : ''}`}
      onClick={() => navigate(to)}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => e.key === 'Enter' && navigate(to)}
    >
      {label}
    </div>
  );
}

export default function Layout() {
  return (
    <Page>
      <Page.Header>
        <AppHeader>
          <AppHeader.Navigation>
            <AppHeader.AppNavLink href="/">Folio</AppHeader.AppNavLink>
          </AppHeader.Navigation>
        </AppHeader>
      </Page.Header>
      <Page.Sidebar>
        <nav className="sidebar-nav">
          {NAV_ITEMS.map(item => (
            <SidebarNavItem key={item.to} label={item.label} to={item.to} end={item.end} />
          ))}
        </nav>
      </Page.Sidebar>
      <Page.Main>
        <div className="page-content">
          <Outlet />
        </div>
      </Page.Main>
    </Page>
  );
}