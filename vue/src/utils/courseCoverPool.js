/** 课程封面图池（Unsplash，按课程 id 稳定映射） */
export const COURSE_COVER_POOL = [
  'https://images.unsplash.com/photo-1503676260728-1c00da094a0b?auto=format&fit=crop&w=960&q=80',
  'https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=960&q=80',
  'https://images.unsplash.com/photo-1434030216411-0b793f4b4173?auto=format&fit=crop&w=960&q=80',
  'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?auto=format&fit=crop&w=960&q=80',
  'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=960&q=80',
  'https://images.unsplash.com/photo-1516979187457-6377994c4df1?auto=format&fit=crop&w=960&q=80',
  'https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=960&q=80',
  'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?auto=format&fit=crop&w=960&q=80',
]

export function coverIndexForCourse(id) {
  if (id == null || id === '') return 0
  const n =
    typeof id === 'number' && Number.isFinite(id)
      ? id
      : String(id).split('').reduce((acc, ch) => acc + ch.charCodeAt(0), 0)
  return Math.abs(n) % COURSE_COVER_POOL.length
}

export function getCoverUrlForCourse(id) {
  return COURSE_COVER_POOL[coverIndexForCourse(id)]
}
