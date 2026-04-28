import { test, expect } from '@playwright/test'

test.describe('资产管理功能完整测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[placeholder="用户名"]', 'admin')
    await page.fill('input[placeholder="密码"]', 'password')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    await page.goto('/asset')
    await page.waitForLoadState('networkidle')
  })

  test('资产管理页面加载验证', async ({ page }) => {
    await page.waitForSelector('.asset-container', { timeout: 10000 })
    await expect(page.locator('.asset-container')).toBeVisible()
    await expect(page.locator('input[placeholder="请输入名称"]')).toBeVisible()
    await expect(page.locator('button:has-text("查询")')).toBeVisible()
    await expect(page.locator('button:has-text("重置")')).toBeVisible()
    await expect(page.locator('button:has-text("新增资产")')).toBeVisible()
    await expect(page.locator('button:has-text("扫码解析")')).toBeVisible()
  })

  test('搜索资产功能', async ({ page }) => {
    await page.waitForSelector('input[placeholder="请输入名称"]', { timeout: 10000 })
    await page.fill('input[placeholder="请输入名称"]', '测试')
    await page.click('button:has-text("查询")')
    await page.waitForTimeout(500)
  })

  test('重置搜索功能', async ({ page }) => {
    await page.fill('input[placeholder="请输入名称"]', '测试')
    await page.click('button:has-text("重置")')
    await expect(page.locator('input[placeholder="请输入名称"]')).toHaveValue('')
  })

  test('新增资产弹窗', async ({ page }) => {
    await page.waitForSelector('button:has-text("新增资产")', { timeout: 10000 })
    await page.click('button:has-text("新增资产")')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('新增资产')
    await page.click('.el-dialog button:has-text("取消")')
    await expect(page.locator('.el-dialog')).not.toBeVisible()
  })

  test('编辑资产弹窗', async ({ page }) => {
    await page.waitForSelector('.el-table', { timeout: 10000 })
    const editButtons = await page.locator('button:has-text("编辑")').count()
    if (editButtons > 0) {
      await page.locator('button:has-text("编辑")').first().click()
      await expect(page.locator('.el-dialog')).toBeVisible()
      await expect(page.locator('.el-dialog__title')).toContainText('编辑资产')
      await page.click('.el-dialog button:has-text("取消")')
      await expect(page.locator('.el-dialog')).not.toBeVisible()
    }
  })

  test('删除资产确认框', async ({ page }) => {
    await page.waitForSelector('.el-table', { timeout: 10000 })
    const deleteButtons = await page.locator('button:has-text("删除")').count()
    if (deleteButtons > 0) {
      await page.locator('button:has-text("删除")').first().click()
      await expect(page.locator('.el-message-box')).toBeVisible()
      await page.click('.el-message-box button:has-text("取消")')
      await expect(page.locator('.el-message-box')).not.toBeVisible()
    }
  })

  test('扫码解析弹窗', async ({ page }) => {
    await page.waitForSelector('button:has-text("扫码解析")', { timeout: 10000 })
    await page.click('button:has-text("扫码解析")')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('资产扫码解析')
    await page.click('.el-dialog button:has-text("关闭")')
    await expect(page.locator('.el-dialog')).not.toBeVisible()
  })

  test('批量分配按钮禁用状态', async ({ page }) => {
    const batchAssignBtn = page.locator('button:has-text("批量分配")')
    await expect(batchAssignBtn).toBeDisabled()
  })

  test('批量报废按钮禁用状态', async ({ page }) => {
    const batchCloseBtn = page.locator('button:has-text("批量报废")')
    await expect(batchCloseBtn).toBeDisabled()
  })

  test('分页组件验证', async ({ page }) => {
    await expect(page.locator('.el-pagination')).toBeVisible()
  })
})
