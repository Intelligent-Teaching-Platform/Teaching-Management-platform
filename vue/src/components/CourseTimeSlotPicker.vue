<template>
  <div class="time-slot-picker">
    <el-popover
      :visible="popoverVisible"
      trigger="click"
      placement="bottom-start"
      :show-arrow="true"
      :offset="8"
      popper-class="course-time-slot-popover-card"
      @update:visible="(v) => (popoverVisible = v)"
      @show="onPopoverShow"
    >
      <template #reference>
        <el-input
          class="time-slot-input"
          :model-value="modelValue"
          readonly
          placeholder="点击选择上课时间"
          clearable
          @clear.stop="clear"
        >
          <template #suffix>
            <el-icon class="input-suffix-icon"><Calendar /></el-icon>
          </template>
        </el-input>
      </template>

      <div class="popover-card-inner">
        <div class="popover-card-title">上课时间</div>

        <el-radio-group v-model="mode" class="mode-radio" size="small">
          <el-radio-button label="weekly">每周固定</el-radio-button>
          <el-radio-button label="specific">特定时间</el-radio-button>
        </el-radio-group>

        <div v-if="mode === 'weekly'" class="section">
          <div class="section-label">星期</div>
          <div class="week-chips">
            <button
              v-for="(label, di) in DAY_LABELS"
              :key="'w-' + di"
              type="button"
              class="chip chip-day"
              :class="{ active: weeklyDay === di }"
              @click="weeklyDay = di"
            >
              周{{ label }}
            </button>
          </div>
        </div>

        <div v-else class="section">
          <div class="section-label">日期</div>
          <el-date-picker
            v-model="specificDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择上课日期"
            class="date-field"
            :disabled-date="disablePastDate"
          />
        </div>

        <div class="section">
          <div class="section-label">时段（两小时）</div>
          <div class="time-strip">
            <button
              v-for="sh in SLOT_START_HOURS"
              :key="'t-' + sh"
              type="button"
              class="chip chip-time"
              :class="{ active: selectedStartHour === sh }"
              @click="onPickTime(sh)"
            >
              <span class="time-main">{{ compactSlotLabel(sh) }}</span>
              <span class="time-sub">{{ pad2(sh) }}:00-{{ pad2(sh + 2) }}:00</span>
            </button>
          </div>
        </div>

        <div class="popover-footer">
          <el-button size="small" text type="danger" @click="clear">清空</el-button>
          <el-button size="small" type="primary" :disabled="!canApply" @click="applyAndClose">确定</el-button>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Calendar } from '@element-plus/icons-vue'
import {
  DAY_LABELS,
  SLOT_START_HOURS,
  formatCourseTimeSlot,
  formatSpecificCourseTime,
  parseCourseTime,
  matchStandardSlot,
  compactSlotLabel,
} from '@/utils/courseSchedule'

const props = defineProps({
  modelValue: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue'])

const popoverVisible = ref(false)
const mode = ref('weekly')
const weeklyDay = ref(0)
const specificDate = ref('')
const selectedStartHour = ref(null)

function pad2(n) {
  return String(n).padStart(2, '0')
}

function disablePastDate(d) {
  const t = new Date()
  t.setHours(0, 0, 0, 0)
  return d.getTime() < t.getTime()
}

const canApply = computed(() => {
  if (selectedStartHour.value == null) return false
  if (mode.value === 'weekly') return weeklyDay.value >= 0 && weeklyDay.value <= 6
  return !!specificDate.value
})

function onPickTime(sh) {
  selectedStartHour.value = sh
}

function applyAndClose() {
  if (!canApply.value) return
  const sh = selectedStartHour.value
  if (mode.value === 'weekly') {
    emit('update:modelValue', formatCourseTimeSlot(weeklyDay.value, sh))
  } else {
    emit('update:modelValue', formatSpecificCourseTime(specificDate.value, sh))
  }
  popoverVisible.value = false
}

function onPopoverShow() {
  syncFromModel()
}

function clear() {
  weeklyDay.value = 0
  specificDate.value = ''
  selectedStartHour.value = null
  mode.value = 'weekly'
  emit('update:modelValue', '')
}

function syncFromModel() {
  const raw = props.modelValue?.trim()
  if (!raw) {
    mode.value = 'weekly'
    weeklyDay.value = 0
    specificDate.value = ''
    selectedStartHour.value = null
    return
  }

  const p = parseCourseTime(raw)
  if (!p) return

  if (p.mode === 'specific' && p.specificDate) {
    mode.value = 'specific'
    specificDate.value = p.specificDate
  } else {
    mode.value = 'weekly'
    specificDate.value = ''
    weeklyDay.value = p.dayIndex >= 0 ? p.dayIndex : 0
  }

  const m = matchStandardSlot(p)
  selectedStartHour.value = m?.startHour ?? SLOT_START_HOURS[0]
}

watch(
  () => props.modelValue,
  () => {
    if (!popoverVisible.value) syncFromModel()
  },
)

</script>

<style scoped>
.time-slot-picker {
  width: 100%;
}

.time-slot-input :deep(.el-input__wrapper) {
  cursor: pointer;
}

.time-slot-input :deep(.el-input__inner) {
  cursor: pointer;
}

.input-suffix-icon {
  color: var(--el-text-color-placeholder);
  font-size: 16px;
}

.popover-card-inner {
  width: min(92vw, 380px);
  box-sizing: border-box;
  background-color: #ffffff;
  border-radius: 8px;
}

.popover-card-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  margin-bottom: 10px;
  letter-spacing: -0.02em;
}

.mode-radio {
  width: 100%;
  margin-bottom: 12px;
  display: flex;
}

.mode-radio :deep(.el-radio-button) {
  flex: 1;
}

.mode-radio :deep(.el-radio-button__inner) {
  width: 100%;
}

.section {
  margin-bottom: 12px;
}

.section-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
  font-weight: 500;
}

.week-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.time-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  margin: 0;
  border: 1px solid var(--el-border-color);
  background: var(--el-fill-color-blank);
  border-radius: 8px;
  cursor: pointer;
  transition:
    border-color 0.15s ease,
    background 0.15s ease,
    color 0.15s ease;
}

.chip-day {
  padding: 5px 10px;
  font-size: 12px;
  color: var(--el-text-color-regular);
}

.chip-day:hover {
  border-color: var(--el-color-primary-light-5);
  color: var(--el-color-primary);
}

.chip-day.active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: 600;
}

.chip-time {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  padding: 6px 8px;
  gap: 2px;
}

.chip-time:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}

.chip-time.active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-8);
  box-shadow: inset 0 0 0 1px var(--el-color-primary-light-5);
}

.time-main {
  font-size: 13px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--el-text-color-primary);
  line-height: 1;
}

.chip-time.active .time-main {
  color: var(--el-color-primary);
}

.time-sub {
  font-size: 10px;
  color: var(--el-text-color-secondary);
  font-variant-numeric: tabular-nums;
  line-height: 1;
}

.date-field {
  width: 100%;
}

.popover-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}
</style>

<style>
/* Teleport 到 body，必须纯色底：勿用 --el-bg-color-overlay（常为半透明导致透底） */
.course-time-slot-popover-card.el-popper,
.course-time-slot-popover-card.el-popper.is-light,
.course-time-slot-popover-card.el-popper.is-dark {
  padding: 12px 14px 10px !important;
  border-radius: 12px !important;
  border: 1px solid var(--el-border-color-lighter) !important;
  box-shadow:
    0 4px 6px -1px rgba(15, 23, 42, 0.06),
    0 18px 42px -12px rgba(15, 23, 42, 0.16) !important;
  background-color: #ffffff !important;
  background-image: none !important;
  --el-bg-color-overlay: #ffffff;
}

.course-time-slot-popover-card.el-popper .el-popover__content,
.course-time-slot-popover-card.el-popper .el-popper__content {
  background-color: #ffffff !important;
  padding: 0 !important;
  margin: 0 !important;
}

/* 选项卡未选中时默认透明，在 Dialog/复杂底上会透字 */
.course-time-slot-popover-card .el-radio-button__inner {
  background-color: #ffffff !important;
}

.course-time-slot-popover-card .el-radio-button.is-active .el-radio-button__inner {
  background-color: var(--el-color-primary) !important;
  border-color: var(--el-color-primary) !important;
  color: #ffffff !important;
}
</style>
