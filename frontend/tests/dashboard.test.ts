import { test, expect } from '@playwright/test';

test.describe('仪表盘功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'password');
    await page.click('button:has-text("登录")');
    await page.waitForURL('/dashboard', { timeout: 10000 });
  });

  test('测试仪表盘页面加载', async ({ page }) => {
    await page.waitForSelector('.tech-dashboard', { timeout: 10000 });
    await expect(page.locator('.title')).toContainText('ITOMS');
  });

  test('测试刷新数据功能', async ({ page }) => {
    await page.waitForSelector('.metrics-value', { timeout: 10000 });
    await page.click('button:has-text("刷新数据")');
    await expect(page.locator('.metrics-value').first()).toBeVisible();
  });

  test('测试工单趋势图表', async ({ page }) => {
    await page.waitForSelector('.chart-box', { timeout: 10000 });
    await expect(page.locator('.chart-box').first()).toBeVisible();
  });
});
