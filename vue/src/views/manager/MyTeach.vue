<template>
  <div class="course-container">
    <!-- 搜索和操作区域 -->
    <div class="search-area card">
      <el-input
          v-model="data.name"
          placeholder="请输入课程名称查询"
          class="search-input"
      />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
      <el-button
          type="success"
          v-if="data.user.role === 'TEACHER'"
          @click="handleAdd"
      >
        新增课程
      </el-button>
    </div>

    <!-- 学期筛选区域 -->
    <div class="term-filter card">
      <div class="term-header">
        <h2>2024-2025第一学期</h2>
        <div class="term-actions">
          <el-button type="text">切换学期</el-button>
          <el-button type="text">查看全部课程</el-button>
        </div>
      </div>
      <p class="term-tip">
        当前展示的是2024-2025第一学期的课程，可切换学期筛选课程或查看全部课程
      </p>
    </div>

    <!-- 操作按钮区域 -->
    <div class="action-buttons card">
      <el-button type="text" class="action-btn">移动到</el-button>
      <el-button type="text" class="action-btn">删除</el-button>
      <el-button type="text" class="action-btn">导出成绩</el-button>
    </div>

    <!-- 课程卡片列表 -->
    <div class="course-list">
      <div
          class="course-card"
          v-for="course in data.tableData"
          :key="course.id"
          @click="goToCourseDetail(course)"
      >
        <div class="course-header">
          <h3 class="course-title">{{ course.name }}</h3>
          <div class="course-actions" v-if="data.user.role === 'ADMIN'">
            <el-button
                type="primary"
                size="small"
                @click.stop="handleEdit(course)"
            >
              编辑
            </el-button>
            <el-button
                type="danger"
                size="small"
                @click.stop="handleDelete(course.id)"
            >
              删除
            </el-button>
          </div>
        </div>
        <div class="course-info">
          <p class="info-item">课程编号：{{ course.id }} | 教师姓名：{{ course.teacherName }}</p>
          <p class="info-item">院校：{{ course.collegeName }}</p>
          <p class="info-item">学期：{{ course.term }}</p>
        </div>
        <div class="course-details">
          <p class="detail-item">
            <span class="detail-label">课程学分：</span>
            <span class="detail-value">{{ course.score }}</span>
          </p>
          <p class="detail-item">
            <span class="detail-label">开班人数：</span>
            <span class="detail-value">{{ course.num }}</span>
          </p>
          <p class="detail-item">
            <span class="detail-label">已选人数：</span>
            <span class="detail-value">{{ course.alreadyNum }}</span>
          </p>
          <p class="detail-item">
            <span class="detail-label">上课时间：</span>
            <span class="detail-value">{{ course.time }}</span>
          </p>
          <p class="detail-item">
            <span class="detail-label">上课地点：</span>
            <span class="detail-value">{{ course.location }}</span>
          </p>
          <p class="detail-item">
            <span class="detail-label">班级：</span>
            <span class="detail-value">{{ course.className }}</span>
          </p>
        </div>
      </div>
    </div>

    <!-- 分页组件 -->
    <div class="pagination-container card">
      <el-pagination
          background
          layout="prev, pager, next"
          :page-size="data.pageSize"
          :current-page="data.pageNum"
          :total="data.total"
          @current-change="changePage"
      />
    </div>

    <!-- 课程表单对话框 -->
    <el-dialog
        :title="data.form.id ? '编辑课程' : '新增课程'"
        v-model="data.formVisible"
        width="40%"
        :close-on-click-modal="false"
        destroy-on-close
    >
      <el-form :model="data.form" label-width="100px">
        <el-form-item label="课程名称">
          <el-input v-model="data.form.name" />
        </el-form-item>
        <el-form-item label="课程介绍">
          <el-input v-model="data.form.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="课程学分">
          <el-input v-model="data.form.score" />
        </el-form-item>
        <el-form-item label="授课教师">
          <el-select v-model="data.form.teacherId" placeholder="请选择授课教师">
            <el-option
                v-for="item in data.teacherData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开班人数">
          <el-input v-model="data.form.num" />
        </el-form-item>
        <el-form-item label="上课时间">
          <el-input v-model="data.form.time" />
        </el-form-item>
        <el-form-item label="上课地点">
          <el-input v-model="data.form.location" />
        </el-form-item>
        <el-form-item label="选择班级">
          <el-select v-model="data.form.classId" placeholder="请选择班级">
            <el-option
                v-for="item in data.classData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属学院">
          <el-select v-model="data.form.collegeId" placeholder="请选择所属学院">
            <el-option
                v-for="item in data.collegeData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  name: '',
  tableData: [],
  formVisible: false,
  form: {},
  teacherData: [],
  collegeData: [],
  classData: [],
  user: JSON.parse(localStorage.getItem('system-user') || '{}')
})

// 跳转到课程详情（通过 query 传递 id 和 courseName，便于子页如课程资料使用）
const goToCourseDetail = (course) => {
  router.push({
    path: '/course/courseDetail',
    query: { id: course.id, courseName: course.name }
  })
}

// 加载课程列表
const load = async () => {
  try {
    const params = {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name
    }

    if (data.user.role === 'TEACHER') {
      params.teacherId = data.user.id
    }

    const res = await request.get('/course/selectPage', { params })
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total || 0
    } else {
      ElMessage.error(res.msg || '获取课程列表失败')
    }
  } catch (error) {
    ElMessage.error('请求失败: ' + error.message)
  }
}

// 加载教师、学院、班级数据
const loadOptions = async () => {
  try {
    const [teachers, colleges, classes] = await Promise.all([
      request.get('/teacher/selectAll'),
      request.get('/college/selectAll'),
      request.get('/class/selectAll')
    ])

    if (teachers.code === '200') data.teacherData = teachers.data || []
    if (colleges.code === '200') data.collegeData = colleges.data || []
    if (classes.code === '200') data.classData = classes.data || []
  } catch (error) {
    ElMessage.error('加载选项数据失败: ' + error.message)
  }
}

// 新增课程
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

// 编辑课程
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 保存课程
const save = async () => {
  try {
    const url = data.form.id ? '/course/update' : '/course/add'
    const method = data.form.id ? 'put' : 'post'

    const res = await request[method](url, data.form)
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('请求失败: ' + error.message)
  }
}

// 删除课程
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除此课程吗?', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete(`/course/delete/${id}`)
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('请求失败: ' + error.message)
    }
  }).catch(() => {})
}

// 重置搜索
const reset = () => {
  data.name = ''
  data.pageNum = 1
  load()
}

// 分页变化
const changePage = (pageNum) => {
  data.pageNum = pageNum
  load()
}

// 初始化加载
onMounted(() => {
  load()
  loadOptions()
})
</script>

<style scoped>
.course-container {
  padding: 20px;
}

/* 卡片基础样式 */
.card {
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 15px;
}

/* 搜索区域 */
.search-area {
  padding: 15px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.search-input {
  width: 300px;
}

/* 学期筛选 */
.term-filter {
  padding: 15px;
}

.term-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.term-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.term-actions .el-button {
  margin-left: 10px;
}

.term-tip {
  margin: 0;
  color: #666;
  font-size: 14px;
}

/* 操作按钮 */
.action-buttons {
  padding: 10px 15px;
}

.action-btn {
  margin-right: 15px;
}

/* 课程列表 */
.course-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 15px;
  margin-bottom: 15px;
}

/* 课程卡片 */
.course-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  background: #fff;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  cursor: pointer;
}

.course-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.course-title {
  margin: 0;
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.course-actions .el-button {
  margin-left: 8px;
}

/* 课程信息 */
.course-info {
  margin-bottom: 12px;
}

.info-item {
  margin: 6px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

/* 课程详情 */
.course-details {
  font-size: 13px;
}

.detail-item {
  display: flex;
  margin: 5px 0;
  line-height: 1.5;
}

.detail-label {
  color: #999;
  width: 80px;
  flex-shrink: 0;
}

.detail-value {
  color: #666;
  flex-grow: 1;
}

/* 分页 */
.pagination-container {
  padding: 10px;
  display: flex;
  justify-content: center;
}
</style>