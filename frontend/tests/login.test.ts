import { test, expect } from '@playwright/test'

test.describe('登录功能完整测试', () => {
  test('登录页面加载验证', async ({ page }) => {
    await page.goto('/login')
    await expect(page.locator('h2')).toContainText('IT运维')
    await expect(page.locator('input[placeholder="用户名"]')).toBeVisible()
    await expect(page.locator('input[placeholder="密码"]')).toBeVisible()
    await expect(page.locator('button:has-text("登录")')).toBeVisible()
  })

  test('登录成功流程', async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[placeholder="用户名"]', 'admin')
    await page.fill('input[placeholder="密码"]', 'password')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    await expect(page).toHaveURL('/dashboard')
  })

  test('未登录访问受保护页面重定向', async ({ page }) => {
    await page.goto('/asset')
    await expect(page).toHaveURL(/\/login/)
  })

  test('已登录访问登录页面重定向', async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[placeholder="用户名"]', 'admin')
    await page.fill('input[placeholder="密码"]', 'password')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    await page.goto('/login')
    await expect(page).toHaveURL('/dashboard')
  })
})
