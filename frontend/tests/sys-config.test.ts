import { test, expect } from '@playwright/test'

test.describe('参数配置功能完整测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[placeholder="用户名"]', 'admin')
    await page.fill('input[placeholder="密码"]', 'password')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    await page.click('.el-sub-menu:has-text("系统管理")')
    await page.click('.el-menu-item:has-text("参数配置")')
    await page.waitForLoadState('networkidle')
  })

  test('参数配置页面加载验证', async ({ page }) => {
    await page.waitForSelector('.sys-config-container', { timeout: 10000 })
    await expect(page.locator('.sys-config-container')).toBeVisible()
    await expect(page.locator('input[placeholder="请输入参数名称"]')).toBeVisible()
    await expect(page.locator('input[placeholder="请输入参数键名"]')).toBeVisible()
    await expect(page.locator('button:has-text("查询")')).toBeVisible()
    await expect(page.locator('button:has-text("重置")')).toBeVisible()
    await expect(page.locator('button:has-text("新增配置")')).toBeVisible()
  })

  test('搜索参数配置功能', async ({ page }) => {
    await page.fill('input[placeholder="请输入参数名称"]', '测试')
    await page.fill('input[placeholder="请输入参数键名"]', 'test')
    await page.click('button:has-text("查询")')
    await page.waitForTimeout(500)
  })

  test('重置搜索功能', async ({ page }) => {
    await page.fill('input[placeholder="请输入参数名称"]', '测试')
    await page.fill('input[placeholder="请输入参数键名"]', 'test')
    await page.click('button:has-text("重置")')
    await expect(page.locator('input[placeholder="请输入参数名称"]')).toHaveValue('')
    await expect(page.locator('input[placeholder="请输入参数键名"]')).toHaveValue('')
  })

  test('新增参数配置弹窗', async ({ page }) => {
    await page.waitForSelector('button:has-text("新增配置")', { timeout: 10000 })
    await page.click('button:has-text("新增配置")')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('新增参数配置')
    await expect(page.locator('input[placeholder="请输入参数名称"]').nth(1)).toBeVisible()
    await expect(page.locator('input[placeholder="请输入参数键名"]').nth(1)).toBeVisible()
    await expect(page.locator('input[placeholder="请输入参数键值"]')).toBeVisible()
    await expect(page.locator('textarea[placeholder="请输入备注"]')).toBeVisible()
    await page.click('.el-dialog button:has-text("取消")')
    await expect(page.locator('.el-dialog')).not.toBeVisible()
  })

  test('编辑参数配置弹窗', async ({ page }) => {
    await page.waitForSelector('.el-table', { timeout: 10000 })
    const editButtons = await page.locator('button:has-text("编辑")').count()
    if (editButtons > 0) {
      await page.click('button:has-text("编辑")').first()
      await expect(page.locator('.el-dialog')).toBeVisible()
      await expect(page.locator('.el-dialog__title')).toContainText('编辑参数配置')
      await page.click('.el-dialog button:has-text("取消")')
      await expect(page.locator('.el-dialog')).not.toBeVisible()
    }
  })

  test('删除参数配置确认框', async ({ page }) => {
    await page.waitForSelector('.el-table', { timeout: 10000 })
    const deleteButtons = await page.locator('button:has-text("删除")').count()
    if (deleteButtons > 0) {
      await page.click('button:has-text("删除")').first()
      await expect(page.locator('.el-message-box')).toBeVisible()
      await page.click('.el-message-box button:has-text("取消")')
      await expect(page.locator('.el-message-box')).not.toBeVisible()
    }
  })

  test('分页组件验证', async ({ page }) => {
    await expect(page.locator('.el-pagination')).toBeVisible()
  })
})
