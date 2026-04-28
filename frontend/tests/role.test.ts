import { test, expect } from '@playwright/test'

test.describe('角色管理功能完整测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[placeholder="用户名"]', 'admin')
    await page.fill('input[placeholder="密码"]', 'password')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    await page.click('.el-sub-menu:has-text("系统管理")')
    await page.click('.el-menu-item:has-text("角色管理")')
    await page.waitForLoadState('networkidle')
  })

  test('角色管理页面加载验证', async ({ page }) => {
    await page.waitForSelector('.role-container', { timeout: 10000 })
    await expect(page.locator('.role-container')).toBeVisible()
    await expect(page.locator('.el-card:has-text("角色管理")')).toBeVisible()
    await expect(page.locator('button:has-text("新增角色")')).toBeVisible()
  })

  test('新增角色弹窗', async ({ page }) => {
    await page.waitForSelector('button:has-text("新增角色")', { timeout: 10000 })
    await page.click('button:has-text("新增角色")')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('新增角色')
    await expect(page.locator('input[placeholder="请输入角色名称"]')).toBeVisible()
    await expect(page.locator('input[placeholder="请输入角色编码"]')).toBeVisible()
    await expect(page.locator('textarea[placeholder="请输入描述"]')).toBeVisible()
    await expect(page.locator('.el-dialog .el-switch')).toBeVisible()
    await page.click('.el-dialog button:has-text("取消")')
    await expect(page.locator('.el-dialog')).not.toBeVisible()
  })

  test('编辑角色弹窗', async ({ page }) => {
    await page.waitForSelector('.el-table', { timeout: 10000 })
    const editButtons = page.locator('button:has-text("编辑")')
    const count = await editButtons.count()
    if (count === 0) {
      return
    }
    const isFirstEnabled = await editButtons.first().isEnabled()
    if (!isFirstEnabled) {
      return
    }
    await editButtons.first().click()
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('编辑角色')
    await page.click('.el-dialog button:has-text("取消")')
    await expect(page.locator('.el-dialog')).not.toBeVisible()
  })

  test('删除角色确认框', async ({ page }) => {
    await page.waitForSelector('.el-table', { timeout: 10000 })
    const deleteButtons = page.locator('button:has-text("删除")')
    const count = await deleteButtons.count()
    if (count === 0) {
      return
    }
    const isFirstEnabled = await deleteButtons.first().isEnabled()
    if (!isFirstEnabled) {
      return
    }
    await deleteButtons.first().click()
    await expect(page.locator('.el-message-box')).toBeVisible()
    await expect(page.locator('.el-message-box__title')).toContainText('提示')
    await page.click('.el-message-box button:has-text("取消")')
    await expect(page.locator('.el-message-box')).not.toBeVisible()
  })

  test('分页组件验证', async ({ page }) => {
    await expect(page.locator('.el-pagination')).toBeVisible()
  })
})
