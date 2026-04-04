/** 课表：每周固定 / 特定日期 + 两小时段 */

export const DAY_LABELS = ['一', '二', '三', '四', '五', '六', '日']

export const SLOT_START_HOURS = [8, 10, 12, 14, 16]

export const SCHEDULE_START_HOUR = 8
export const SCHEDULE_END_HOUR = 18

export function dayLabelToIndex(label) {
  const i = DAY_LABELS.indexOf(label)
  return i >= 0 ? i : -1
}

function pad2(n) {
  return String(n).padStart(2, '0')
}

/**
 * 解析 YYYY-MM-DD
 * @returns {Date | null} 本地 0 点
 */
export function parseYmd(ymd) {
  if (!ymd || typeof ymd !== 'string') return null
  const m = ymd.trim().match(/^(\d{4})-(\d{2})-(\d{2})$/)
  if (!m) return null
  const d = new Date(Number(m[1]), Number(m[2]) - 1, Number(m[3]), 0, 0, 0, 0)
  return Number.isNaN(d.getTime()) ? null : d
}

export function sameCalendarDay(a, b) {
  if (!a || !b) return false
  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  )
}

/**
 * @returns {{
 *   mode: 'weekly' | 'specific',
 *   dayIndex: number,
 *   specificDate?: string,
 *   startH: number,
 *   startM: number,
 *   endH: number,
 *   endM: number
 * } | null}
 */
export function parseCourseTime(str) {
  if (!str || typeof str !== 'string') return null
  const s = str.trim()

  let mode = 'weekly'
  let dayIndex = -1
  let specificDate = null

  const spec = s.match(/^(\d{4}-\d{2}-\d{2})\s+(\d{1,2})\s*[:：]\s*(\d{2})\s*[-~至到]\s*(\d{1,2})\s*[:：]\s*(\d{2})/)
  if (spec) {
    mode = 'specific'
    specificDate = spec[1]
    const d = parseYmd(specificDate)
    if (d) {
      const dow = d.getDay()
      dayIndex = dow === 0 ? 6 : dow - 1
    }
    const startH = Number(spec[2])
    const startM = Number(spec[3])
    const endH = Number(spec[4])
    const endM = Number(spec[5])
    if (Number.isNaN(startH) || Number.isNaN(endH)) return null
    return { mode, dayIndex, specificDate, startH, startM, endH, endM }
  }

  const dm = s.match(/周([一二三四五六日天])/)
  if (dm) {
    const ch = dm[1] === '天' ? '日' : dm[1]
    dayIndex = dayLabelToIndex(ch)
  }

  const tm = s.match(/(\d{1,2})\s*[:：]\s*(\d{2})\s*[-~至到]\s*(\d{1,2})\s*[:：]\s*(\d{2})/)
  if (!tm) return dayIndex >= 0 ? { mode: 'weekly', dayIndex, startH: 8, startM: 0, endH: 10, endM: 0 } : null

  const startH = Number(tm[1])
  const startM = Number(tm[2])
  const endH = Number(tm[3])
  const endM = Number(tm[4])
  if (Number.isNaN(startH) || Number.isNaN(endH)) return null

  return { mode: 'weekly', dayIndex, specificDate: null, startH, startM, endH, endM }
}

export function formatCourseTimeSlot(dayIndex, startHour) {
  if (dayIndex < 0 || dayIndex > 6) return ''
  const endHour = startHour + 2
  return `周${DAY_LABELS[dayIndex]} ${pad2(startHour)}:00-${pad2(endHour)}:00`
}

export function formatSpecificCourseTime(ymd, startHour) {
  if (!ymd || startHour == null) return ''
  const endHour = startHour + 2
  return `${ymd} ${pad2(startHour)}:00-${pad2(endHour)}:00`
}

export function matchStandardSlot(parsed) {
  if (!parsed) return null
  const startTotal = parsed.startH * 60 + parsed.startM
  for (const h of SLOT_START_HOURS) {
    const slotStart = h * 60
    const slotEnd = (h + 2) * 60
    if (startTotal >= slotStart - 15 && startTotal < slotEnd && parsed.endH * 60 + parsed.endM <= slotEnd + 15) {
      return { dayIndex: parsed.dayIndex, startHour: h, specificDate: parsed.specificDate || null }
    }
  }
  return {
    dayIndex: parsed.dayIndex,
    startHour: SLOT_START_HOURS.includes(parsed.startH) ? parsed.startH : SLOT_START_HOURS[0],
    specificDate: parsed.specificDate || null,
  }
}

/**
 * @param {Date} columnDate 该列对应自然日 0 点
 */
export function courseBelongsToWeekColumn(course, columnDayIndex, columnDate) {
  const p = parseCourseTime(course?.time)
  if (!p) return false
  if (p.mode === 'specific' && p.specificDate && columnDate) {
    const cd = parseYmd(p.specificDate)
    return cd ? sameCalendarDay(cd, columnDate) : false
  }
  if (p.dayIndex < 0) return false
  return p.dayIndex === columnDayIndex
}

export function layoutBlockPercent(parsed) {
  if (!parsed) return null
  const startMin = parsed.startH * 60 + parsed.startM
  const endMin = parsed.endH * 60 + parsed.endM
  const rangeMin = (SCHEDULE_END_HOUR - SCHEDULE_START_HOUR) * 60
  const top = ((startMin - SCHEDULE_START_HOUR * 60) / rangeMin) * 100
  const height = ((endMin - startMin) / rangeMin) * 100
  if (height <= 0 || top < -5 || top > 105) return null
  return {
    top: Math.max(0, top),
    height: Math.min(100 - Math.max(0, top), height),
  }
}

export function hourTicks() {
  const ticks = []
  for (let h = SCHEDULE_START_HOUR; h <= SCHEDULE_END_HOUR; h++) ticks.push(h)
  return ticks
}

/** 紧凑展示用：如 8-10 */
export function compactSlotLabel(startHour) {
  return `${startHour}-${startHour + 2}`
}
