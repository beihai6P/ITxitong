import { test, expect } from '@playwright/test'

test.describe('导航功能完整测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[placeholder="用户名"]', 'admin')
    await page.fill('input[placeholder="密码"]', 'password')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/dashboard', { timeout: 10000 })
  })

  test('侧边栏导航到监控大屏', async ({ page }) => {
    await page.click('.el-menu-item:has-text("监控大屏")')
    await expect(page).toHaveURL('/dashboard')
  })

  test('侧边栏导航到资产管理', async ({ page }) => {
    await page.click('.el-menu-item:has-text("资产管理")')
    await expect(page).toHaveURL('/asset')
  })

  test('侧边栏导航到工单报修', async ({ page }) => {
    await page.click('.el-menu-item:has-text("工单管理")')
    await expect(page).toHaveURL('/work-order')
  })

  test('展开系统管理菜单', async ({ page }) => {
    await page.click('.el-sub-menu:has-text("系统管理")')
    await expect(page.locator('.el-menu-item:has-text("角色管理")')).toBeVisible()
  })

  test('导航到角色管理', async ({ page }) => {
    await page.click('.el-sub-menu:has-text("系统管理")')
    await page.click('.el-menu-item:has-text("角色管理")')
    await expect(page).toHaveURL('/system/role')
  })

  test('导航到菜单管理', async ({ page }) => {
    await page.click('.el-sub-menu:has-text("系统管理")')
    await page.click('.el-menu-item:has-text("菜单管理")')
    await expect(page).toHaveURL('/system/menu')
  })

  test('导航到字典管理', async ({ page }) => {
    await page.click('.el-sub-menu:has-text("系统管理")')
    await page.click('.el-menu-item:has-text("字典管理")')
    await expect(page).toHaveURL('/system/dict')
  })

  test('导航到参数配置', async ({ page }) => {
    await page.click('.el-sub-menu:has-text("系统管理")')
    await page.click('.el-menu-item:has-text("参数配置")')
    await expect(page).toHaveURL('/system/config')
  })

  test('导航到日志管理', async ({ page }) => {
    await page.click('.el-sub-menu:has-text("系统管理")')
    await page.click('.el-menu-item:has-text("日志管理")')
    await expect(page).toHaveURL('/system/log')
  })

  test('消息通知抽屉', async ({ page }) => {
    await page.click('.message-icon')
    await expect(page.locator('.el-drawer')).toBeVisible()
  })

  test('用户下拉菜单', async ({ page }) => {
    await page.click('span:has-text("Admin")')
    await expect(page.locator('.el-dropdown-menu')).toBeVisible()
  })

  test('退出登录', async ({ page }) => {
    await page.click('span:has-text("Admin")')
    await page.waitForSelector('.el-dropdown-menu', { timeout: 5000 })
    await page.locator('.el-dropdown-menu').getByText('退出登录').click()
    await expect(page).toHaveURL('/login', { timeout: 10000 })
  })
})
