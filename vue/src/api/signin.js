// src/api/signin.js
import request from '@/utils/request' // 假设你使用了axios封装

export function createSignIn(data) {
  return request({
    url: '/signIn/create',
    method: 'post',
    data
  })
}

export function recordSignIn(data) {
  return request({
    url: '/signIn/record',
    method: 'post',
    data
  })
}

// 按课程获取最新一次签到统计（人数 + 姓名列表）
export function selectAll(courseId) {
  return request({
    url: '/signIn/selectAll',
    method: 'get',
    params: { courseId }
  })
}

export function selectByName(name) {
  return request({
    url: '/signIn/selectByName',
    method: 'get',
    params: { name: name.trim() },
  })
}

export function selectAllStudents(courseId) {
    return request({
        url: '/student/selectByCourseId', // 修改为正确的接口路径
        method: 'get',
        params: {
            courseId // 使用courseId作为参数
        }
    })
}