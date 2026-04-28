import { test, expect } from '@playwright/test'

test.describe('日志管理功能完整测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[placeholder="用户名"]', 'admin')
    await page.fill('input[placeholder="密码"]', 'password')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    await page.click('.el-sub-menu:has-text("系统管理")')
    await page.click('.el-menu-item:has-text("日志管理")')
    await page.waitForLoadState('networkidle')
  })

  test('日志管理页面加载验证', async ({ page }) => {
    await page.waitForSelector('.log-container', { timeout: 10000 })
    await expect(page.locator('.log-container')).toBeVisible()
    await expect(page.locator('.el-card:has-text("日志管理")')).toBeVisible()
    await expect(page.locator('input[placeholder="搜索操作人员"]')).toBeVisible()
    await expect(page.locator('button:has-text("搜索")')).toBeVisible()
    await expect(page.locator('button:has-text("重置")')).toBeVisible()
  })

  test('搜索日志功能', async ({ page }) => {
    await page.fill('input[placeholder="搜索操作人员"]', 'admin')
    await page.click('button:has-text("搜索")')
    await page.waitForTimeout(500)
  })

  test('重置搜索功能', async ({ page }) => {
    await page.fill('input[placeholder="搜索操作人员"]', 'admin')
    await page.click('button:has-text("重置")')
    await expect(page.locator('input[placeholder="搜索操作人员"]')).toHaveValue('')
  })

  test('日志表格显示验证', async ({ page }) => {
    await expect(page.locator('.el-table')).toBeVisible()
    await expect(page.locator('th:has-text("序号")')).toBeVisible()
    await expect(page.locator('th:has-text("操作模块")')).toBeVisible()
    await expect(page.locator('th:has-text("操作类型")')).toBeVisible()
    await expect(page.locator('th:has-text("操作描述")')).toBeVisible()
    await expect(page.locator('th:has-text("操作人员")')).toBeVisible()
    await expect(page.locator('th:has-text("IP地址")')).toBeVisible()
    await expect(page.locator('th:has-text("状态")')).toBeVisible()
    await expect(page.locator('th:has-text("操作时间")')).toBeVisible()
  })

  test('分页组件验证', async ({ page }) => {
    await expect(page.locator('.el-pagination')).toBeVisible()
  })
})
