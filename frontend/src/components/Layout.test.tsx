import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithRouter, userEvent } from '../test/test-utils';
import Layout from './Layout';
import { Route, Routes } from 'react-router-dom';

function renderLayout(initialRoute = '/') {
  return renderWithRouter(
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<div>Dashboard Page</div>} />
        <Route path="/transactions" element={<div>Transactions Page</div>} />
        <Route path="/stocks" element={<div>Stocks Page</div>} />
        <Route path="/countries" element={<div>Countries Page</div>} />
        <Route path="/branches" element={<div>Branches Page</div>} />
        <Route path="/depots" element={<div>Depots Page</div>} />
        <Route path="/currencies" element={<div>Currencies Page</div>} />
        <Route path="/ticker-symbols" element={<div>Ticker Symbols Page</div>} />
        <Route path="/isin-names" element={<div>ISIN Names Page</div>} />
        <Route path="/dividend-payments" element={<div>Dividend Payments Page</div>} />
        <Route path="/analytics/:type" element={<div>Analytics Page</div>} />
        <Route path="/import" element={<div>Import Page</div>} />
        <Route path="/settings" element={<div>Settings Page</div>} />
      </Route>
    </Routes>,
    { routerProps: { initialEntries: [initialRoute] } },
  );
}

describe('Layout', () => {
  it('renders the app header with "Folio" title', () => {
    renderLayout();
    expect(screen.getByText('Folio')).toBeInTheDocument();
  });

  it('renders all 14 sidebar navigation items', () => {
    renderLayout();
    const expectedItems = [
      'Dashboard', 'Transactions', 'Stocks', 'Countries', 'Branches',
      'Depots', 'Currencies', 'Ticker Symbols', 'ISIN Names',
      'Dividend Payments', 'Country Analysis', 'Branch Analysis', 'Import', 'Settings',
    ];
    for (const item of expectedItems) {
      expect(screen.getByText(item)).toBeInTheDocument();
    }
  });

  it('renders dashboard content at /', () => {
    renderLayout('/');
    expect(screen.getByText('Dashboard Page')).toBeInTheDocument();
  });

  it('highlights the active nav item with selected class', () => {
    renderLayout('/transactions');
    const txNav = screen.getByText('Transactions');
    expect(txNav.className).toContain('sidebar-nav-item--selected');
  });

  it('navigates to another page when clicking a nav item', async () => {
    const user = userEvent.setup();
    renderLayout('/');
    expect(screen.getByText('Dashboard Page')).toBeInTheDocument();

    await user.click(screen.getByText('Stocks'));
    expect(screen.getByText('Stocks Page')).toBeInTheDocument();
  });

  it('navigates via keyboard Enter on nav items', async () => {
    const user = userEvent.setup();
    renderLayout('/');

    const settingsNav = screen.getByText('Settings');
    settingsNav.focus();
    await user.keyboard('{Enter}');
    expect(screen.getByText('Settings Page')).toBeInTheDocument();
  });
});
