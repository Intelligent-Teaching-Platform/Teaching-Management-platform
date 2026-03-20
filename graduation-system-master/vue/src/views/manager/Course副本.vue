<template>
  <div class="container">
    <div class="card search-bar">
      <el-input v-model="data.name" style="width: 300px; margin-right: 10px" placeholder="请输入课程名称查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
      <!-- 教师端新增课程按钮 -->
      <el-button type="success" v-if="data.user.role === 'TEACHER'" @click="handleAdd">新增课程</el-button>
    </div>

    <!-- 教师端视图：卡片布局 -->
    <div class="teach-card" v-if="data.user.role === 'TEACHER'">
      <CourseCard
          v-for="course in data.tableData"
          :key="course.id"
          :course="course"
          :term="course.term"
          @delete="handleDelete(course.id)"
      />
    </div>
    <!-- 学生端视图：卡片布局 -->
    <div class="teach-card" v-if="data.user.role === 'STUDENT'">
      <CourseCard
          v-for="course in data.tableData"
          :key="course.id"
          :course="course"
          :term="course.term"
          @delete="handleDelete(course.id)"
      />
    </div>
    <!-- 管理员视图：表格布局 -->
    <div class="card table-view" v-if="data.user.role === 'ADMIN'">
      <div style="margin-bottom: 10px" v-if="data.user.role === 'ADMIN'">
        <el-button type="primary" @click="handleAdd">新增</el-button>
        <el-button type="primary" @click="triggerFileInput">批量导入</el-button>
        <input
            type="file"
            ref="fileInput"
            style="display: none"
            accept=".xlsx,.xls"
            @change="handleFileUpload"
        >
      </div>

      <el-table :data="data.tableData" stripe class="scrollable-content">
        <!-- 表格列定义保持不变 -->
        <el-table-column label="课程名称" prop="name"></el-table-column>
        <el-table-column label="课程介绍" prop="content" show-overflow-tooltip></el-table-column>
        <el-table-column label="课程学分" prop="score"></el-table-column>
        <el-table-column label="授课教师" prop="teacherName"></el-table-column>
        <el-table-column label="开班人数" prop="num"></el-table-column>
        <el-table-column label="上课时间" prop="time"></el-table-column>
        <el-table-column label="上课地点" prop="location"></el-table-column>
        <el-table-column label="所属学院" prop="collegeName"></el-table-column>
        <el-table-column label="班级" prop="className"></el-table-column>
        <el-table-column label="学期" prop="term"></el-table-column>
        <el-table-column label="已选人数" prop="alreadyNum" v-if="data.user.role !== 'STUDENT'"></el-table-column>

        <el-table-column label="操作" align="center" width="160" v-if="data.user.role === 'ADMIN'">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="160" v-else>
          <template #default="scope">
            <el-button type="info" @click="handleDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页组件 -->
    <div class="card pagination" v-if="data.user.role === 'ADMIN'">
      <el-pagination
          background layout="prev, pager, next"
          v-model:page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
          @current-change="changePage"
      />
    </div>

    <!-- 课程信息弹窗 -->
    <el-dialog title="课程信息" width="40%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <!-- 表单内容保持不变 -->
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="课程介绍" prop="content">
          <el-input type="textarea" :rows="3" v-model="data.form.content" autocomplete="off" />
        </el-form-item>
        <el-form-item label="课程学分" prop="score">
          <el-input v-model="data.form.score" autocomplete="off" />
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="data.form.teacherId" placeholder="请选择授课教师" :disabled="data.user.role === 'TEACHER'">
            <el-option
                v-for="item in data.teacherData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开班人数" prop="num">
          <el-input v-model="data.form.num" autocomplete="off" />
        </el-form-item>
        <el-form-item label="上课时间" prop="time">
          <el-input v-model="data.form.time" autocomplete="off" />
        </el-form-item>
        <el-form-item label="上课地点" prop="location">
          <el-input v-model="data.form.location" autocomplete="off" />
        </el-form-item>
        <el-form-item label="选择班级" prop="classId">
          <el-select v-model="data.form.classId" placeholder="请选择班级">
            <el-option
                v-for="item in data.classData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="data.form.collegeId" placeholder="请选择所属学院">
            <el-option
                v-for="item in data.collegeData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import router from "@/router";
import CourseCard from "@/views/manager/CourseCard.vue";

const fileInput = ref(null);

const data = reactive({
  pageNum: 1,
  pageSize: 5,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null,
  classData: [],
  collegeData: [],
  teacherData: [],
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
});

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value.click();
};

// 处理文件上传
const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  try {
    const formData = new FormData();
    formData.append('file', file);

    const res = await request.post('/course/import', formData);

    if (res.code === "200") {
      ElMessage.success(res.msg || "导入成功");
      load();
    } else {
      ElMessage.error(res.msg || "导入失败");
    }
  } catch (error) {
    ElMessage.error('导入失败: ' + (error.response?.data?.msg || error.message));
  } finally {
    event.target.value = '';
  }
};

// 分页查询
const load = () => {
  let teacherId = null;
  if (data.user.role === 'TEACHER') {
    teacherId = data.user.id;
  }
  request.get('/course/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,

    },
  }).then(res => {
    data.tableData = res.data?.list.map(course => ({
      ...course,
      imageUrl: '/default-course-image.jpg' // 设置默认图片
    }));
    data.total = res.data?.total;
    console.log('tableData:', data.tableData);
  });
};

// 新增
const handleAdd = () => {
  data.form = {};
  if(data.user.role === 'TEACHER') {
    data.form.teacherId = data.user.id;
  }
  data.formVisible = true;
};

// 查看详情
const handleDetail = (row) => {
  router.push({path:'/course/courseDetail', query: { id: row.id, courseName: row.name }});
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row));
  data.formVisible = true;
};

// 新增保存
const add = () => {
  request.post('/course/add', data.form).then(res => {
    if (res.code === '200') {
      load();
      ElMessage.success('操作成功');
      data.formVisible = false;
    }
  });
};

// 编辑保存
const update = () => {
  request.put('/course/update', data.form).then(res => {
    if (res.code === '200') {
      load();
      ElMessage.success('操作成功');
      data.formVisible = false;
    }
  });
};

// 弹窗保存
const save = () => {
  // data.form有id就是更新，没有就是新增
  data.form.id ? update() : add();
};

// 删除（统一处理函数）
const handleDelete = (id) => {
  console.log('删除课程ID:', id); // 确认id是否正确传递

  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', {type: 'warning'}).then(() => {
    request.delete(`/course/delete/${id}`).then(res => {
      if (res.code === '200') {
        load();
        ElMessage.success('操作成功');
      } else {
        ElMessage.error(res.msg);
      }
    });
  }).catch(() => {
  });
};

// 重置
const reset = () => {
  data.name = null;
  load();
};

// 分页
const changePage = (pageNum) => {
  data.pageNum = pageNum;
  load();
};

// 加载学院数据
const loadCollege = () => {
  request.get('/college/selectAll').then(res => {
    if (res.code === '200') {
      data.collegeData = res.data;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 加载教师数据
const loadTeacher = () => {
  request.get('/teacher/selectAll').then(res => {
    if (res.code === '200') {
      data.teacherData = res.data;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 加载班级数据
const loadClass = () => {
  request.get('/clazz/selectAll').then(res => {
    if (res.code === '200') {
      data.classData = res.data;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 初始化加载数据
loadClass();
load();
loadCollege();
loadTeacher();
</script>

<style scoped>
.container {
  height: 100vh;
  display: flex;
  flex: 1;
  flex-direction: column;
}

.teach-card {
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  display: grid;
  margin-bottom: 5px;
  justify-content: center;
  align-content: space-around;
  gap: 10px;
  position: relative;
}

.card {
  margin-bottom: 5px;
}

.scrollable-content {
  max-height: 60vh;
  overflow-y: auto;
}

.pagination {
  display: flex;
  justify-content: center;
}
</style>