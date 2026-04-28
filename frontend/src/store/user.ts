import { defineStore } from 'pinia'
import request from '@/utils/request'
import type { RoleItem } from './permission'

export interface UserItem {
  id?: number
  username: string
  password?: string
  realName?: string
  phone?: string
  email?: string
  departmentId?: number
  role?: string
  status?: number
  roleIds?: number[]
  roleNames?: string[]
  createTime?: string
}

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null as UserItem | null,
    userList: [] as UserItem[],
    total: 0,
    roles: [] as RoleItem[],
  }),

  actions: {
    async fetchCurrentUser() {
      try {
        const res: any = await request.get('/auth/current')
        if (res.data) {
          this.currentUser = res.data
        }
        return this.currentUser
      } catch (error) {
        console.error('Failed to fetch current user:', error)
        return null
      }
    },

    async fetchUserList(params: {
      current?: number
      size?: number
      username?: string
      realName?: string
    }) {
      try {
        const res: any = await request.get('/api/v1/users', { params })
        this.userList = res.data?.records || res.data || []
        this.total = res.data?.total || res.total || 0
        return this.userList
      } catch (error) {
        console.error('Failed to fetch user list:', error)
        return []
      }
    },

    async fetchAllRoles() {
      try {
        const res: any = await request.get('/role/all')
        this.roles = res.data || []
        return this.roles
      } catch (error) {
        console.error('Failed to fetch roles:', error)
        return []
      }
    },

    async getUserById(id: number) {
      try {
        const res: any = await request.get(`/api/v1/users/${id}`)
        return res.data as UserItem
      } catch (error) {
        console.error('Failed to get user:', error)
        return null
      }
    },

    async createUser(user: UserItem) {
      try {
        await request.post('/api/v1/users', user)
        return true
      } catch (error) {
        console.error('Failed to create user:', error)
        return false
      }
    },

    async updateUser(id: number, user: UserItem) {
      try {
        await request.put(`/api/v1/users/${id}`, user)
        return true
      } catch (error) {
        console.error('Failed to update user:', error)
        return false
      }
    },

    async deleteUser(id: number) {
      try {
        await request.delete(`/api/v1/users/${id}`)
        return true
      } catch (error) {
        console.error('Failed to delete user:', error)
        return false
      }
    },

    async assignRoles(userId: number, roleIds: number[]) {
      try {
        await request.put(`/api/v1/users/${userId}/roles`, roleIds)
        return true
      } catch (error) {
        console.error('Failed to assign roles:', error)
        return false
      }
    }
  }
})
