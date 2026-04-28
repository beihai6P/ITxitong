import { defineStore } from 'pinia'
import request from '@/utils/request'

export interface MenuItem {
  id: number
  parentId: number
  menuName: string
  path: string
  component: string
  icon: string
  sortOrder: number
  menuType: string
  perms: string
  isExternal: number
  isHidden: number
  status: number
  visible: string
  keepAlive: string
  alwaysShow: string
  children?: MenuItem[]
  checked?: boolean
}

export interface RoleItem {
  id: number
  roleCode: string
  roleName: string
  description: string
  status: number
  roleSort: number
  dataScope: string
  menuIds?: number[]
  createTime?: string
  updateTime?: string
}

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    menus: [] as MenuItem[],
    permissions: [] as string[],
    roleMenus: [] as MenuItem[],
    allMenus: [] as MenuItem[],
  }),

  actions: {
    async fetchUserMenus() {
      try {
        const res: any = await request.get('/menu/enabled')
        this.allMenus = res.data || []
        return this.allMenus
      } catch (error) {
        console.error('Failed to fetch menus:', error)
        return []
      }
    },

    async fetchMenuTree() {
      try {
        const res: any = await request.get('/menu/tree')
        this.menus = res.data || []
        return this.menus
      } catch (error) {
        console.error('Failed to fetch menu tree:', error)
        return []
      }
    },

    async fetchRoleMenus(roleId: number) {
      try {
        const res: any = await request.get(`/role/${roleId}/menus`)
        this.roleMenus = res.data || []
        return this.roleMenus
      } catch (error) {
        console.error('Failed to fetch role menus:', error)
        return []
      }
    },

    async assignMenusToRole(roleId: number, menuIds: number[]) {
      try {
        await request.put(`/role/${roleId}/menus`, menuIds)
        return true
      } catch (error) {
        console.error('Failed to assign menus:', error)
        return false
      }
    },

    setPermissions(permissions: string[]) {
      this.permissions = permissions
    },

    hasPermission(perm: string): boolean {
      if (this.permissions.includes('*:*:*')) {
        return true
      }
      return this.permissions.includes(perm)
    },

    filterMenusByPermission(menus: MenuItem[]): MenuItem[] {
      return menus.filter(menu => {
        if (menu.perms && !this.hasPermission(menu.perms)) {
          return false
        }
        if (menu.children && menu.children.length > 0) {
          menu.children = this.filterMenusByPermission(menu.children)
        }
        return true
      })
    }
  }
})
