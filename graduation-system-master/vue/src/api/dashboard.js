import request from '@/utils/request'

// -------- 管理员大屏 mainscreen --------
export const mainLeft1 = () => request.get('/mainscreen/left1', { _ignoreNotFound: true })
export const mainLeft2 = () => request.get('/mainscreen/left2', { _ignoreNotFound: true })
export const mainLeft3 = () => request.get('/mainscreen/left3', { _ignoreNotFound: true })
export const mainMid1 = () => request.get('/mainscreen/mid1', { _ignoreNotFound: true })
export const mainMid2 = () => request.get('/mainscreen/mid2', { _ignoreNotFound: true })
export const mainMid3 = (courseId) =>
  request.get('/mainscreen/mid3', {
    params: courseId ? { courseId } : {},
    _ignoreNotFound: true,
  })
export const mainRight1 = () => request.get('/mainscreen/right1', { _ignoreNotFound: true })
export const mainRight2 = () => request.get('/mainscreen/right2', { _ignoreNotFound: true })
export const mainRight3 = () => request.get('/mainscreen/right3', { _ignoreNotFound: true })

// -------- 教师看板 screen --------
export const screenLeft1 = (teacherId) =>
  request.get('/screen/left1', { params: { teacherId }, _ignoreNotFound: true })
export const screenLeft2 = (teacherId, courseId) =>
  request.get('/screen/left2', { params: { teacherId, courseId }, _ignoreNotFound: true })
export const screenLeft3 = (teacherId, courseId) =>
  request.get('/screen/left3', { params: { teacherId, courseId }, _ignoreNotFound: true })
export const screenMid1 = (teacherId) =>
  request.get('/screen/mid1', { params: { teacherId }, _ignoreNotFound: true })
export const screenMid2 = (teacherId) =>
  request.get('/screen/mid2', { params: { teacherId }, _ignoreNotFound: true })
export const screenMid3 = (teacherId, courseId) =>
  request.get('/screen/mid3', { params: { teacherId, courseId }, _ignoreNotFound: true })
export const screenRight1 = (courseId) =>
  request.get('/screen/right1', { params: { courseId }, _ignoreNotFound: true })
export const screenRight2 = (teacherId, courseId) =>
  request.get('/screen/right2', { params: { teacherId, courseId }, _ignoreNotFound: true })
export const screenRight3 = (teacherId, courseId) =>
  request.get('/screen/right3', { params: { teacherId, courseId }, _ignoreNotFound: true })

