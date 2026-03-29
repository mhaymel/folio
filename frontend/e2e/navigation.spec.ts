import { test, expect } from '@playwright/test';

test.describe('Navigation', () => {
  test('loads the dashboard on root URL', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('h1')).toContainText('Dashboard');
  });

  test('sidebar contains all navigation items', async ({ page }) => {
    await page.goto('/');
    const sidebar = page.locator('[class*="sidebar-nav"]');
    const navItems = [
      'Dashboard', 'Transactions', 'Stocks', 'Stocks per Depot', 'Countries', 'Branches',
      'Depots', 'Currencies', 'Ticker Symbols', 'ISIN Names',
      'Country Analysis', 'Branch Analysis', 'Import', 'Settings',
    ];
    for (const item of navItems) {
      await expect(sidebar.getByText(item, { exact: true })).toBeVisible();
    }
  });

  test('navigates to Transactions page via sidebar', async ({ page }) => {
    await page.goto('/');
    await page.getByText('Transactions', { exact: true }).click();
    await expect(page).toHaveURL('/transactions');
    await expect(page.locator('h1')).toContainText('Transactions');
  });

  test('navigates to Stocks page via sidebar', async ({ page }) => {
    await page.goto('/');
    await page.getByText('Stocks', { exact: true }).click();
    await expect(page).toHaveURL('/stocks');
    await expect(page.locator('h1')).toContainText('Stocks');
  });

  test('navigates to Stocks per Depot page via sidebar', async ({ page }) => {
    await page.goto('/');
    await page.getByText('Stocks per Depot', { exact: true }).click();
    await expect(page).toHaveURL('/stocks-per-depot');
    await expect(page.locator('h1')).toContainText('Stocks per Depot');
  });

  test('navigates to Countries page via sidebar', async ({ page }) => {
    await page.goto('/');
    await page.getByText('Countries', { exact: true }).click();
    await expect(page).toHaveURL('/countries');
    await expect(page.locator('h1')).toContainText('Countries');
  });

  test('navigates to Import page via sidebar', async ({ page }) => {
    await page.goto('/');
    await page.getByText('Import', { exact: true }).click();
    await expect(page).toHaveURL('/import');
    await expect(page.locator('h1')).toContainText('Import');
  });

  test('navigates to Settings page via sidebar', async ({ page }) => {
    await page.goto('/');
    await page.getByText('Settings', { exact: true }).click();
    await expect(page).toHaveURL('/settings');
    await expect(page.locator('h1')).toContainText('Settings');
  });

  test('highlights active nav item', async ({ page }) => {
    await page.goto('/stocks');
    const stocksNav = page.locator('[class*="sidebar-nav-item--selected"]');
    await expect(stocksNav).toContainText('Stocks');
  });

  test('app header shows Folio title', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByText('Folio')).toBeVisible();
  });
});
