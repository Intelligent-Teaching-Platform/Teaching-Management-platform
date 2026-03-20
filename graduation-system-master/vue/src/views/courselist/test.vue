<template>
  <div class="discussion-container" >
    <!-- 头部区域 -->
    <div class="header" style="display: flex;justify-content: space-between">
      <el-button type="primary" style="display: flex;margin-left:0px;font-size:14px;box-shadow: 4px 4px 6px rgba(0, 0, 0, 0.1);padding:14px" round  @click="focusTopicEditor">+ 新建话题</el-button>
      <div class="xsearchInput">
        <input type="text" placeholder="搜索">
        <el-icon class="search"><Search /></el-icon>
      </div>
    </div>
    <div class="header-min">
      <!-- 使用 el-select 和 el-option -->
      <el-select v-model="selectedClass" class="class-select" placeholder="请选择">
        <el-option
            v-for="item in classOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value">
        </el-option>
      </el-select>

      <div class="date-range">
        <text style="font-size:16px;color:darkgrey;margin-left:30px"> 发布话题的时间：</text>
<!--        <input type="text" placeholder="开始时间" />-->
<!--        <span>—</span>-->
<!--        <input type="text" placeholder="结束时间" style="margin-left:5px;" />-->
        <el-date-picker
            v-model="startTime"
            type="date"
            placeholder="选择开始时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"          style="width: 150px;"
        />

        <span>—</span>
        <el-date-picker
            v-model="endTime"
            type="date"
            placeholder="选择结束时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"          style="width: 150px; margin-left: 5px;"
        />
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="main-content">
      <div class="tabs">
        <span class="active-tab">全部</span>
        <span style="color:#409EFF;">+ 新建讨论区</span>
      </div>
      <div class="topics-list">
        <div v-for="topic in topics" :key="topic.id" class="topic-card">
          <h4> {{ topic.title }}</h4>
          <p>{{ topic.content }}</p>
          <small style="margin-left:auto">{{ topic.time }}</small>
        </div>
      </div>
      <div class="content-area" v-if="topics.length === 0" >
        <p class="empty-message">本班还没有话题哦，快去发布一个吧～</p>
      </div>
    </div>
  </div>
    <!-- 新建话题区域 -->
    <div class="new-topic-area" style="margin:20px">
      <div><h3>新建话题</h3></div>
      <div class="publish-to" style="margin-bottom: 10px">
        <span style="margin-right:20px">发布给</span>
        <!-- 使用 el-select 和 el-option，添加 multiple 属性 -->
        <el-select v-model="selectedPublishClass" class="publish-select" style="width:150px" placeholder="请选择班级" >
          <el-option
              v-for="item in classOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
          </el-option>
        </el-select>
      </div>
      <div class="topic-form">
        <input type="text"  v-model="topicTitle" placeholder="请输入标题" class="topic-title" />
        <div class="topic-editor">
          <textarea  v-model="topicContent" placeholder="请输入话题正文"  ref="topicTextarea"></textarea>
        </div>
        <div class="editor-toolbar">
          <i class="icon-bold"></i>
          <i class="icon-italic"></i>
          <!-- 更多工具栏按钮 -->
        </div>
        <div class="footer">
          <button class="publish-btn" @click="publishTopic" >发布</button>
        </div>
      </div>
    </div>

</template>

<script setup>
import { ref } from 'vue'
// 数据定义
const searchQuery = ref('');
const selectedClass = ref('all');
const classOptions = ref([
  { value: 'all', label: '全部班级' },
  { value: 'default', label: '默认班级' }
]);
// const publishTime = ref('');
const startTime = ref(null)
const endTime = ref(null)
const selectedPublishClass = ref(['all', 'default']); // 默认选择全部班级和默认班级
const topicTitle = ref('');
const topicContent = ref('');
const topicTextarea = ref(null)

const focusTopicEditor = () => {
  if (topicTextarea.value) {
    topicTextarea.value.focus()
  }
}

// 方法定义
const chooseForum = () => {
  // 选择讨论区的逻辑
  console.log('选择讨论区');
};
const topics = ref([])
// 发布话题方法
const publishTopic = () => {
  if (!topicTitle.value.trim() || !topicContent.value.trim()) {
    alert('请输入标题和内容')
    return
  }

  const newTopic = {
    id: Date.now(), // 使用时间戳作为唯一ID
    title: topicTitle.value,
    content: topicContent.value,
    time: new Date().toLocaleString()
  }

  topics.value.unshift(newTopic) // 插入到最前面

  // 清空输入框
  topicTitle.value = ''
  topicContent.value = ''
}
</script>

<style scoped>
.discussion-container {
  //width: 100%;
  margin: 20px;
  padding: 20px;
  box-sizing: border-box;
  background-color: white; /* 背景颜色 */
  border-radius: 8px; /* 圆角 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); /* 阴影效果 */
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.xsearchInput {
  display: inline-block;
  position: relative;
  margin-right: 20px;
}

.xsearchInput input {
  width: 200px;
  height: 36px;
  line-height: 34px;
  border-radius: 50px;
  border: 1px solid #D4D6D9;
  box-sizing: border-box;
  padding: 0 44px 0 14px;
  font-size: 14px;
  color: #474C59;
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
}

.search {
  position: absolute;
  top: 10px;
  right: 14px;
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.header-min {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  //justify-content: space-between;
}
.topics-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}

.topic-card {
  //background-color: #f9f9f9;
  //padding: 16px;
  //border-radius: 8px;
  //box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  //transition: all 0.2s ease-in-out;
}



.topic-card h4 {
  margin-bottom: 8px;
  font-size: 16px;
  color: #333;
}

.topic-card p {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.topic-card small {
  color: #999;
  font-size: 12px;
}

.class-select {
  padding: 7px 35px;
  border-radius: 6px;
  border-color: #DADFE6;
  font-size: 14px;
  width:200px;
}

.date-range input, .search input {
  padding: 8px;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  margin-right: 5px;

}

/* 点击输入框时的样式 */
.date-range input:focus {
  //border-color: #409EFF; /* 浅蓝色边框 */
  outline: none; /* 移除默认的outline*/
  /* box-shadow: 0 0 5px rgba(64, 158, 255, 0.5); */ /* 添加阴影效果，增强视觉效果 */
}

.main-content {
  border-radius: 4px;
  margin-bottom: 20px;
}

.tabs {
  display: flex;
  border-bottom: 1px solid #DCDFE6;
  margin-bottom: 20px;
}

.tabs span {
  padding: 10px 20px;
  cursor: pointer;
}

.active-tab {
  border-bottom: 2px solid #409EFF;
}

.content-area {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-message {
  color: #999;
}

.new-topic-area {
  //border-radius: 6px;
  font-size: 16px;
  //box-shadow: rgba(0.1, 0.1, 0.1, 0.1) 4px 4px 6px;
  padding: 18px;
  border: 1px solid #DCDFE6;
  //margin-top: 20px;
  margin: 0 auto;
  box-sizing: border-box;
  background-color: white; /* 背景颜色 */
  border-radius: 8px; /* 圆角 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); /* 阴影效果 */
}

.topic-form .topic-title {
  width: 100%;
  padding: 10px;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  margin-bottom: 10px;
}
.topic-form .topic-title:focus {
  //border-color: #409EFF; /* 浅蓝色边框 */
  outline: none; /* 移除默认的outline*/
  //box-shadow: 0 0 5px rgba(64, 158, 255, 0.5);
}
.topic-editor :focus {
  //border-color: #409EFF; /* 浅蓝色边框 */
  outline: none; /* 移除默认的outline*/
  //box-shadow: 0 0 5px rgba(64, 158, 255, 0.5);
}

.topic-editor textarea {
  width: 100%;
  height: 200px;
  padding: 10px;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  margin-bottom: 10px;
  resize: none;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.editor-toolbar i {
  font-size: 16px;
  margin-right: 10px;
  cursor: pointer;
}

.editor-toolbar i:hover {
  color: #409EFF;
}

.footer {
  display: flex;
  justify-content: flex-end;
}

.choose-forum-btn, .publish-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 30px;
  border-radius: 4px;
  cursor: pointer;
  border-radius: 20px;
}

.settings {
  margin-right: 10px;
}
</style>
