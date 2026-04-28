import { test, expect } from '@playwright/test'

test.describe('菜单管理功能完整测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[placeholder="用户名"]', 'admin')
    await page.fill('input[placeholder="密码"]', 'password')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    await page.click('.el-sub-menu:has-text("系统管理")')
    await page.click('.el-menu-item:has-text("菜单管理")')
    await page.waitForLoadState('networkidle')
  })

  test('菜单管理页面加载验证', async ({ page }) => {
    await page.waitForSelector('.menu-container', { timeout: 10000 })
    await expect(page.locator('.menu-container')).toBeVisible()
    await expect(page.locator('.el-card:has-text("菜单管理")')).toBeVisible()
    await expect(page.locator('button:has-text("新增菜单")')).toBeVisible()
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('新增菜单弹窗', async ({ page }) => {
    await page.waitForSelector('button:has-text("新增菜单")', { timeout: 10000 })
    await page.click('button:has-text("新增菜单")')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('新增菜单')
    await expect(page.locator('input[placeholder="请输入菜单名称"]')).toBeVisible()
    await expect(page.locator('input[placeholder="请输入图标名"]')).toBeVisible()
    await expect(page.locator('input[placeholder="请输入路由路径"]')).toBeVisible()
    await expect(page.locator('input[placeholder="请输入组件路径"]')).toBeVisible()
    await expect(page.locator('.el-switch')).toBeVisible()
    await page.click('.el-dialog button:has-text("取消")')
    await expect(page.locator('.el-dialog')).not.toBeVisible()
  })

  test('编辑菜单弹窗', async ({ page }) => {
    await page.waitForSelector('.el-table', { timeout: 10000 })
    const editButtons = await page.locator('button:has-text("编辑")').count()
    if (editButtons > 0) {
      await page.locator('button:has-text("编辑")').first().click()
      await expect(page.locator('.el-dialog')).toBeVisible()
      await expect(page.locator('.el-dialog__title')).toContainText('编辑菜单')
      await page.click('.el-dialog button:has-text("取消")')
      await expect(page.locator('.el-dialog')).not.toBeVisible()
    }
  })

  test('删除菜单确认框', async ({ page }) => {
    await page.waitForSelector('.el-table', { timeout: 10000 })
    const deleteButtons = await page.locator('button:has-text("删除")').count()
    if (deleteButtons > 0) {
      await page.locator('button:has-text("删除")').first().click()
      await expect(page.locator('.el-message-box')).toBeVisible()
      await expect(page.locator('.el-message-box__title')).toContainText('提示')
      await page.click('.el-message-box button:has-text("取消")')
      await expect(page.locator('.el-message-box')).not.toBeVisible()
    }
  })
})
