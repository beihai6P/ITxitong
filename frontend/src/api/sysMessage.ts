import request from '@/utils/request'

export interface SysMessage {
  id: number;
  title: string;
  content: string;
  type: string;
  isRead: boolean;
  createTime: string;
}

export function getUnreadCount() {
  return request({
    url: '/api/sys-message/unread-count',
    method: 'get'
  })
}

export function getMessageList(params: { isRead?: boolean; page?: number; size?: number }) {
  return request({
    url: '/api/sys-message/list',
    method: 'get',
    params
  })
}

export function markAsRead(id?: number) {
  return request({
    url: '/api/sys-message/read',
    method: 'post',
    data: { id }
  })
}
