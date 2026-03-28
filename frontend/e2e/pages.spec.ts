import { test, expect } from '@playwright/test';

test.describe('Dashboard page', () => {
  test('displays KPI cards', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByText('Total Portfolio Value')).toBeVisible();
    await expect(page.getByText('Stocks')).toBeVisible();
    await expect(page.getByText('Dividend Ratio')).toBeVisible();
  });

  test('displays top 5 sections', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByText('Top 5 Holdings')).toBeVisible();
    await expect(page.getByText('Top 5 Dividend Sources')).toBeVisible();
  });

  test('displays last updated info', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByText(/Last updated:/)).toBeVisible();
  });
});

test.describe('Countries page', () => {
  test('loads and shows heading', async ({ page }) => {
    await page.goto('/countries');
    await expect(page.locator('h1')).toContainText('Countries');
  });

  test('shows export buttons', async ({ page }) => {
    await page.goto('/countries');
    await expect(page.getByText('Export CSV')).toBeVisible();
    await expect(page.getByText('Export Excel')).toBeVisible();
  });

  test('shows Show All toggle', async ({ page }) => {
    await page.goto('/countries');
    await expect(page.getByText('Show All')).toBeVisible();
  });
});

test.describe('Stocks page', () => {
  test('loads and shows heading', async ({ page }) => {
    await page.goto('/stocks');
    await expect(page.locator('h1')).toContainText('Stocks');
  });

  test('shows filter dropdowns', async ({ page }) => {
    await page.goto('/stocks');
    await expect(page.getByText('All countries')).toBeVisible();
    await expect(page.getByText('All branches')).toBeVisible();
  });

  test('shows Refresh button', async ({ page }) => {
    await page.goto('/stocks');
    await expect(page.getByText('Refresh')).toBeVisible();
  });
});

test.describe('Settings page', () => {
  test('loads and shows heading', async ({ page }) => {
    await page.goto('/settings');
    await expect(page.locator('h1')).toContainText('Settings');
  });

  test('shows quote fetching section', async ({ page }) => {
    await page.goto('/settings');
    await expect(page.getByText('Quote Fetching')).toBeVisible();
  });

  test('shows Fetch Now button', async ({ page }) => {
    await page.goto('/settings');
    await expect(page.getByText('Fetch Now')).toBeVisible();
  });
});

test.describe('Import page', () => {
  test('loads and shows heading', async ({ page }) => {
    await page.goto('/import');
    await expect(page.locator('h1')).toContainText('Import');
  });
});

test.describe('Analytics page', () => {
  test('country analysis loads', async ({ page }) => {
    await page.goto('/analytics/countries');
    await expect(page.locator('h1')).toBeVisible();
  });

  test('branch analysis loads', async ({ page }) => {
    await page.goto('/analytics/branches');
    await expect(page.locator('h1')).toBeVisible();
  });
});
