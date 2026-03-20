<template>
  <div class="container">
    <div class="header">
      <div class="setting">
        <div class="more-setting" @click="toggleSetting">批量设置</div>
        <el-input class="input" v-model="input" placeholder="搜索"/>
      </div>
      <div class="time">
        <div class="time_lime">定时开放任务点提醒</div>
        <el-switch v-model="value1" class="icon"></el-switch>
      </div>
    </div>

    <div class="main">
      <div class="content">
        <!-- 单元列表 -->
        <div v-for="(unit, index) in units" :key="unit.id" class="unit">
          <div class="unit-header" @click="toggleUnit(unit)">
            <el-switch v-model="unit.open" class="unit-switch"></el-switch>
            <span class="unit-name">{{ unit.name }}</span>
            <i :class="['el-icon-caret-bottom', { 'is-active': unit.expanded }]"></i>
          </div>
          <!-- 课时列表 -->
          <transition name="fade">
            <div v-if="unit.expanded" class="lessons">
              <div v-for="(lesson, lessonIndex) in unit.lessons" :key="lesson.id" class="lesson">
                <div class="lesson-name" @click="goToLessonDetail(lesson.id)">{{ lesson.name }}</div>
                <div class="lesson-settings">
                  <el-button type="text" @click=" openSetting(lesson)">设置</el-button>
                </div>
              </div>
            </div>
          </transition>
      </div>
    </div>
      <!-- 右侧内容 -->
      <div   class="right-content">
        <div class="right-header">

          <span class="right-item-progress">开放状态</span>
          <span class="right-item-progress">章节进度</span>

        </div>
        <div class="right-items"  >
          <div v-for="(unit, unitIndex) in units" :key="unit.id" class="right-unit">
            <div v-if="unit.expanded" class="right-item" v-for="(lesson, lessonIndex) in unit.lessons" :key="lesson.id">
              <span class="right-item-setting">{{ rightItems[unitIndex * unit.lessons.length + lessonIndex].setting }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

<!--    弹窗内容-->
    <el-dialog v-model="dialogVisible" title="章节设置" width="50%">
      <el-tabs v-model="activeName">
        <el-tab-pane label="学生开放设置" name="studentOpenSettings">
          <div class="open-settings">
            <div class="setting-item" v-for="(option, index) in openOptions" :key="index">
              <el-radio v-model="selectedOption" :label="option.value">{{ option.label }}</el-radio>
              <span class="description">{{ option.description }}</span>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="生效班级" name="effectiveClass">
          <div class="effective-classes">
            <div class="class-item">
              <el-checkbox v-model="allClassesChecked" @change="handleAllClassesChange">全部班级</el-checkbox>
            </div>
            <div class="class-item">
              <el-checkbox v-model="defaultClassChecked" @change=" handleDefaultClassChange">默认班级</el-checkbox>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancel">取消</el-button>
        <el-button type="primary" @click="confirm">确定</el-button>
      </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
const input = ref('');
const router = useRouter()
const value1 = ref(false);
const dialogVisible = ref(false);
const activeName = ref('studentOpenSettings');
const selectedOption = ref('open');
const allClassesChecked = ref(false)
const defaultClassChecked = ref(true)

const openOptions = [
  {
    value: 'open',
    label: '开放',
    description: '该章节学生可见，可学习'
  },
  {
    value: 'levelMode',
    label: '闯关模式',
    description: '该章节学生可见，完成任务点后解锁下一章节'
  },
  {
    value: 'timedOpen',
    label: '定时开放',
    description: '该章节学生可见，在规定时间内可学习'
  },
  {
    value: 'hiddenMode',
    label: '隐藏模式',
    description: '隐藏后的章节学生不可见，学生端的章节目录序号会重新编号。同时学生已学习的章节内容，产生的成绩将会同时被隐藏，不纳入成绩计算。'
  },
  {
    value: 'reviewMode',
    label: '复习模式',
    description: '该章节学生可见，学生复习过程中产生的学习记录将不纳入统计，且无法完成任务点。（章节的任务点数量仍将计入总任务点数）'
  }
];

const units = ref([
  {
    id: 1,
    name: '第一单元',
    open: true,
    expanded: true,
    lessons: [
      { id: 1, name: '1.1 第一课时' },
      { id: 2, name: '1.2 第二课时' },
      { id: 3, name: '1.3 第三课时' },
      { id: 4, name: '1.4 第四课时' },
      { id: 5, name: '1.5 第五课时' }
    ]
  }
])
const rightItems = ref([
  {  setting:'开放' },
  {  setting:'开放'  },
  {  setting:'开放' },
  {   setting:'开放' },
  { setting:'开放' }
]);
const cancel = () => {
  dialogVisible.value = false;
};
const handleAllClassesChange = (value) => {
  if(value) {
    defaultClassChecked.value = false
  }
}

const handleDefaultClassChange = (value) => {
  if(value) {
    allClassesChecked.value = false
  }
}
const currentLessonIndex = ref(null) //用于记录当前正在设置的课时索引
const openSetting = (lesson) => {
  dialogVisible.value = true;
  for (let unitIndex = 0; unitIndex < units.value.length; unitIndex++) {
    const unit = units.value[unitIndex];
    for (let lessonIndex = 0; lessonIndex < unit.lessons.length; lessonIndex++) {
      if (unit.lessons[lessonIndex].id === lesson.id) {
        currentLessonIndex.value = unitIndex * unit.lessons.length + lessonIndex;
        break;
      }
    }
  }
};
const confirm = () => {
  //获取当前选中的选项
  const selectedSetting = openOptions.find(option => option.value === selectedOption.value)?.label;
  // 更新rightItems中对应课时的setting
  if (currentLessonIndex.value !== null && selectedSetting) {
    rightItems.value[currentLessonIndex.value].setting = selectedSetting;
  }

  // 关闭弹窗
  dialogVisible.value = false;
  currentLessonIndex.value = null;
};
const toggleUnit = (unit) => {
  unit.expanded = !unit.expanded;
  rightContentVisible.value = unit.expanded;
};
const toggleSetting = () => {
  console.log('批量设置');
};
const goToLessonDetail = (lessonId) => {
  router.push({name:'lessonDetail',params:{id:lessonId}});
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: auto;
  background: #FFFFFF;
  border-radius: 8px;
}
.header {
  height: 77px;
  display: flex;
  border-bottom: 1px solid #E5E5E5;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  box-shadow: 0px 4px 4px rgba(0, 0, 0, 0.05);
}
.setting {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-right: 30px;
  margin-left:30px;
}
.more-setting {
  margin-right: 15px;
  margin-top: 30px;
  color: #2d53d0;
  cursor: pointer;
}


.input {
  width: 240px;
  height: 30px;
  margin-top: 25px;
  border-radius: 20px;
}

.time {
  display: flex;
  margin-right: 30px;
}

.time_lime {
  margin-top: 30px;
  margin-right: 10px;
}
.icon {
  width: 40px;
  height: 30px;
  margin-top: 25px;
}
.main {
  margin-top: 10px;
  display: flex;
  box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);

}


.chapter {
  height: 20px;
  line-height: 20px;
  font-size: 14px;
  color: #A8A8B3;
  position: relative;
  margin: 10px 30px 0 20px;
}

.chapter-th-open {
  right: 208px;
  position: absolute;
  top: 0;
}

.chapter-th-time {
  right: 70px;
  position: absolute;
  top: 0;
}

.content {
  flex: 1;
  margin: 10px 30px 0 20px;
  position: relative;
  width: 80%;
}

.unit {
  margin-bottom: 10px;
}

.unit-header {
  display: flex;
  align-items: center;
  cursor: pointer;
  background-color: #f5f7fa;
  padding: 10px;
}

.unit-switch {
  margin-right: 10px;
}

.unit-name {
  flex: 1;
}

.is-active {
  transform: rotate(180deg);
}

.lessons {
  width: 100%
}

.lesson {
  display: flex;
  align-items: center;
  padding-left:20px;
  padding-top: 10px;
  padding-bottom: 10px;
  width: 100%;
  &:hover {
    background-color: #DADFE6; // 悬停时的背景色
    .lesson-name {
      color: #409EFF; // 悬停时的文本颜色
    }
  }
}

.lesson-name {
  flex: 1;
}

.lesson-settings {
  margin-left: 10px;
  margin-right:50px;
}

.right-content {
  width: 300px;
  padding-right:20px;
  padding-left:20px;
  margin-top:19px;
  padding-top:10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
margin-right:20px;
  //box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  margin-bottom: 10px;
}

.right-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.right-item-progress {
  color: #909399;
}
.right-items {
  display: flex;
  flex-direction: column;
  flex:1;
}

.right-item {
  display: flex;
  align-items: center;
  padding-top:10px;
  padding-bottom: 10px;

}

.right-item-setting {
  color: #909399; // 设置颜色
  font-size: 14px; // 设置字体大小
  padding:6px;
}


.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter, .fade-leave-to {
  opacity: 0;
}
.open-settings{
  padding:20px;
}
.setting-item{
  magin-bottom:20px;
  align-content: center;
}

.description {
  color:#999;
  margin-top:5px;
}

.effective-classes {
  padding: 20px;
  .class-item {
    margin-bottom: 10px;
  }
}

</style>