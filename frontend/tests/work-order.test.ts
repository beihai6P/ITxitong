import { test, expect } from '@playwright/test'

test.describe('工单管理功能完整测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[placeholder="用户名"]', 'admin')
    await page.fill('input[placeholder="密码"]', 'password')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    await page.goto('/work-order')
    await page.waitForLoadState('networkidle')
  })

  test('工单管理页面加载验证', async ({ page }) => {
    await page.waitForSelector('.work-order-container', { timeout: 10000 })
    await expect(page.locator('.work-order-container')).toBeVisible()
    await expect(page.locator('button:has-text("查询")')).toBeVisible()
    await expect(page.locator('button:has-text("提交报修")')).toBeVisible()
  })

  test('提交报修弹窗', async ({ page }) => {
    await page.waitForSelector('button:has-text("提交报修")', { timeout: 10000 })
    await page.click('button:has-text("提交报修")')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('提交故障报修工单')
    await page.click('.el-dialog button:has-text("取消")')
    await expect(page.locator('.el-dialog')).not.toBeVisible()
  })

  test('查看工单详情弹窗', async ({ page }) => {
    await page.waitForSelector('.el-table', { timeout: 10000 })
    const viewButtons = await page.locator('button:has-text("查看/日志")').count()
    if (viewButtons > 0) {
      await page.locator('button:has-text("查看/日志")').first().click()
      await expect(page.locator('.el-dialog')).toBeVisible()
      await expect(page.locator('.el-dialog__title')).toContainText('工单详情')
      await page.click('.el-dialog button:has-text("关闭")')
      await expect(page.locator('.el-dialog')).not.toBeVisible()
    }
  })

  test('抢单功能', async ({ page }) => {
    await page.waitForSelector('.el-table', { timeout: 10000 })
    const grabButtons = await page.locator('button:has-text("抢单")').count()
    if (grabButtons > 0) {
      await page.locator('button:has-text("抢单")').first().click()
      await expect(page.locator('.el-message-box')).toBeVisible()
    }
  })

  test('转单功能', async ({ page }) => {
    await page.waitForSelector('.el-table', { timeout: 10000 })
    const transferButtons = await page.locator('button:has-text("转单")').count()
    if (transferButtons > 0) {
      await page.locator('button:has-text("转单")').first().click()
      await expect(page.locator('.el-dialog')).toBeVisible()
      await expect(page.locator('.el-dialog__title')).toContainText('工单转派')
      await page.click('.el-dialog button:has-text("取消")')
      await expect(page.locator('.el-dialog')).not.toBeVisible()
    }
  })

  test('批量派单按钮禁用状态', async ({ page }) => {
    const batchAssignBtn = page.locator('button:has-text("批量派单")')
    await expect(batchAssignBtn).toBeDisabled()
  })

  test('批量关闭按钮禁用状态', async ({ page }) => {
    const batchCloseBtn = page.locator('button:has-text("批量关闭")')
    await expect(batchCloseBtn).toBeDisabled()
  })
})
