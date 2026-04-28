import { test, expect } from '@playwright/test'

test.describe('字典管理功能完整测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[placeholder="用户名"]', 'admin')
    await page.fill('input[placeholder="密码"]', 'password')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    await page.click('.el-sub-menu:has-text("系统管理")')
    await page.click('.el-menu-item:has-text("字典管理")')
    await page.waitForLoadState('networkidle')
  })

  test('字典管理页面加载验证', async ({ page }) => {
    await page.waitForSelector('.sys-dict-container', { timeout: 10000 })
    await expect(page.locator('.sys-dict-container')).toBeVisible()
    await expect(page.locator('.el-card:has-text("字典类型")')).toBeVisible()
    await expect(page.locator('.el-card:has-text("字典数据")')).toBeVisible()
    await expect(page.locator('button:has-text("新增")').first()).toBeVisible()
  })

  test('新增字典类型弹窗', async ({ page }) => {
    await page.waitForSelector('button:has-text("新增")', { timeout: 10000 })
    await page.locator('button:has-text("新增")').first().click()
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('新增字典类型')
    await expect(page.locator('input[placeholder="请输入字典名称"]')).toBeVisible()
    await expect(page.locator('input[placeholder="请输入字典类型"]')).toBeVisible()
    await expect(page.locator('textarea[placeholder="请输入备注"]')).toBeVisible()
    await page.click('.el-dialog button:has-text("取消")')
    await expect(page.locator('.el-dialog')).not.toBeVisible()
  })

  test('编辑字典类型弹窗', async ({ page }) => {
    await page.waitForSelector('.el-table', { timeout: 10000 })
    const editButtons = await page.locator('button:has-text("编辑")').first().count()
    if (editButtons > 0) {
      await page.locator('button:has-text("编辑")').first().click()
      await expect(page.locator('.el-dialog')).toBeVisible()
      await expect(page.locator('.el-dialog__title')).toContainText('编辑字典类型')
      await page.click('.el-dialog button:has-text("取消")')
      await expect(page.locator('.el-dialog')).not.toBeVisible()
    }
  })

  test('删除字典类型确认框', async ({ page }) => {
    await page.waitForSelector('.el-table', { timeout: 10000 })
    const deleteButtons = await page.locator('button:has-text("删除")').first().count()
    if (deleteButtons > 0) {
      await page.locator('button:has-text("删除")').first().click()
      await expect(page.locator('.el-message-box')).toBeVisible()
      await page.click('.el-message-box button:has-text("取消")')
      await expect(page.locator('.el-message-box')).not.toBeVisible()
    }
  })

  test('新增字典数据按钮禁用状态', async ({ page }) => {
    await page.waitForSelector('.sys-dict-container', { timeout: 10000 })
    const addDataButtons = await page.locator('button:has-text("新增")').nth(1).count()
    if (addDataButtons > 0) {
      await expect(page.locator('button:has-text("新增")').nth(1)).toBeDisabled()
    }
  })
})
