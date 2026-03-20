<template>
  <div class="container">
    <!-- 添加资料按钮 -->
    <div style="margin-bottom: 20px;">
      <el-button type="primary" @click="openAddForm" v-if="data.user.role === 'TEACHER'">添加资料</el-button>
    </div>
    <!-- 搜索框 -->
    <div class="search-bar">
      <el-input v-model="searchQuery" placeholder="搜索" class="search-input"></el-input>
    </div>

    <!-- 文件列表 -->
    <el-table :data="filteredFiles" style="width: 100%" stripe>
      <el-table-column type="selection"></el-table-column>
      <el-table-column prop="name" label="文件名">
        <template #default="scope">
          <i class="el-icon-folder" v-if="scope.row.isDirectory"></i>
          <img
              class="img"
              src="@/assets/imgs/file.png"
              @click="handleClick(scope.row.path)"
          >
          {{ scope.row.name }}
        </template>
      </el-table-column>
      <el-table-column prop="size" label="大小"></el-table-column>
      <el-table-column prop="teacherName" label="创建者"></el-table-column>
      <el-table-column prop="createTime" label="创建日期">
        <template #default="scope">
          {{ scope.row.createTime || '未设置' }}
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column label="操作">
        <template #default="scope">
          <el-button type="primary" size="small" @click="openEditForm(scope.row)" v-if="data.user.role === 'TEACHER'">修改</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row.id)" v-if="data.user.role === 'TEACHER'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
          background
          layout="prev, pager, next"
          :total="files.length"
          @current-change="handlePageChange"
      ></el-pagination>
    </div>

    <!-- 添加资料弹窗 -->
    <el-dialog title="添加资料" v-model="addDialogVisible" :close-on-click-modal="false" destroy-on-close width="500px">
      <el-form :model="addForm" ref="addFormRef" label-width="120px" :rules="addRules">
        <el-form-item label="文件名" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入文件名"></el-input>
        </el-form-item>
        <el-form-item label="大小" prop="size">
          <el-input v-model="addForm.size" placeholder="请输入文件大小"></el-input>
        </el-form-item>
        <el-form-item label="创建者" prop="teacherName">
          <el-input v-model="addForm.teacherName" placeholder="请输入创建者"></el-input>
        </el-form-item>
        <el-form-item label="创建日期" prop="createTime">
          <el-date-picker
              v-model="addForm.createTime"
              type="date"
              placeholder="选择日期"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="路径" prop="path">
          <el-input v-model="addForm.path" placeholder="请输入文件路径"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAddForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改资料弹窗 -->
    <el-dialog title="修改资料" v-model="editDialogVisible" :close-on-click-modal="false" destroy-on-close width="500px">
      <el-form :model="editForm" ref="editFormRef" label-width="120px" :rules="editRules">
        <el-form-item label="文件名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入文件名"></el-input>
        </el-form-item>
        <el-form-item label="大小" prop="size">
          <el-input v-model="editForm.size" placeholder="请输入文件大小"></el-input>
        </el-form-item>
        <el-form-item label="创建者" prop="teacherName">
          <el-input v-model="editForm.teacherName" placeholder="请输入创建者"></el-input>
        </el-form-item>
        <el-form-item label="创建日期" prop="createTime">
          <el-date-picker
              v-model="editForm.createTime"
              type="date"
              placeholder="选择日期"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="路径" prop="path">
          <el-input v-model="editForm.path" placeholder="请输入文件路径"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from "element-plus";
import axios from "axios";

const data = reactive({
  pageNum: 1,
  pageSize: 5,
  total: 0,
  materialId: [],
  name: null,
  material: null,
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
});

// 弹窗状态
const addDialogVisible = ref(false);
const editDialogVisible = ref(false);

// 文件数据
const files = ref([]);

// 搜索查询
const searchQuery = ref('');

// 过滤后的文件列表
const filteredFiles = computed(() => {
  return files.value.filter(file =>
      file.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

// 当前页码
const currentPage = ref(1);

// 每页显示的文件数量
const pageSize = 5;

// 添加表单数据
const addForm = reactive({
  name: '',
  size: '',
  teacherName: data.user.name || '',
  createTime: new Date(),
  path: '',
});

// 修改表单数据
const editForm = reactive({
  id: null,
  name: '',
  size: '',
  teacherName: '',
  createTime: null,
  path: '',
});

// 表单验证规则
const addRules = reactive({
  name: [
    { required: true, message: '请输入文件名', trigger: 'blur' },
  ],
  teacherName: [
    { required: true, message: '请输入创建者', trigger: 'blur' },
  ],
  path: [
    { required: true, message: '请输入文件路径', trigger: 'blur' },
  ],
});

const editRules = reactive({
  name: [
    { required: true, message: '请输入文件名', trigger: 'blur' },
  ],
  teacherName: [
    { required: true, message: '请输入创建者', trigger: 'blur' },
  ],
  path: [
    { required: true, message: '请输入文件路径', trigger: 'blur' },
  ],
});

// 表单引用
const addFormRef = ref(null);
const editFormRef = ref(null);

// 处理页码变化
const handlePageChange = (page: number) => {
  currentPage.value = page;
};

// 从后端加载数据
const loadFiles = async () => {
  try {
    let teacherId = null;
    let studentId = null;

    if (data.user.role === 'TEACHER') {
      teacherId = data.user.id;
    }
    if (data.user.role === 'STUDENT') {
      studentId = data.user.id;
    }

    // 调用后端接口获取数据
    const response = await axios.get('http://localhost:9090/material/selectAll', {
      params: {
        teacherId: teacherId,
        studentId: studentId,
      }
    });

    if (response.data.code === "200") {
      files.value = response.data.data.map(item => ({
        id: item.id,
        name: item.name,
        isDirectory: false,
        size: item.size,
        teacherName: item.teacherName,
        createTime: item.createTime,
        path: item.path,
      }));
    } else {
      ElMessage.error(response.data.message || '获取文件列表失败');
    }
  } catch (error) {
    console.error('请求数据失败:', error);
    ElMessage.error('请求数据失败，请稍后重试');
  }
};

// 处理图片点击事件
const handleClick = (path: string) => {
  if (path) window.open(path, '_blank');
};

// 处理删除按钮点击事件
const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该资料吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
      .then(() => {
        axios.delete(`http://localhost:9090/material/delete/${id}`)
            .then(() => {
              ElMessage.success('删除成功');
              loadFiles(); // 重新加载数据
            })
            .catch((error) => {
              ElMessage.error('删除失败，请稍后重试');
              console.error('删除失败:', error);
            });
      })
      .catch(() => {
        ElMessage.info('已取消删除');
      });
};

// 打开添加表单
const openAddForm = () => {
  addDialogVisible.value = true;
  resetAddForm();
};

// 打开修改表单
const openEditForm = (row) => {
  console.log('openEditForm 方法被调用，数据:', row);
  editDialogVisible.value = true;
  editForm.id = row.id;
  editForm.name = row.name;
  editForm.size = row.size;
  editForm.teacherName = row.teacherName;
  editForm.createTime = row.createTime ? new Date(row.createTime) : null;
  editForm.path = row.path;
};

// 重置添加表单
const resetAddForm = () => {
  addForm.name = '';
  addForm.size = '';
  addForm.teacherName = data.user.name || '';
  addForm.createTime = new Date();
  addForm.path = '';
};

// 重置修改表单
const resetEditForm = () => {
  editForm.id = null;
  editForm.name = '';
  editForm.size = '';
  editForm.teacherName = '';
  editForm.createTime = null;
  editForm.path = '';
};

// 提交添加表单
const submitAddForm = () => {
  (addFormRef.value as any).validate((valid: boolean) => {
    if (!valid) return;

    axios.post('http://localhost:9090/material/add', addForm)
        .then(() => {
          ElMessage.success('添加成功');
          addDialogVisible.value = false;
          loadFiles(); // 重新加载数据
        })
        .catch((error) => {
          ElMessage.error('添加失败，请稍后重试');
          console.error('添加失败:', error);
        });
  });
};

// 提交修改表单
const submitEditForm = () => {
  (editFormRef.value as any).validate((valid: boolean) => {
    if (!valid) return;

    axios.put(`http://localhost:9090/material/update`, editForm)
        .then(() => {
          ElMessage.success('修改成功');
          editDialogVisible.value = false;
          loadFiles(); // 重新加载数据
        })
        .catch((error) => {
          ElMessage.error('修改失败，请稍后重试');
          console.error('修改失败:', error);
        });
  });
};

// 组件挂载后加载数据
onMounted(() => {
  loadFiles();
});
</script>

<style scoped>
.container {
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
  box-shadow: var(--el-box-shadow-light);
}
.el-table {
  width: 100%;
}
.el-table .cell {
  display: flex;
}

.search-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}
.search-input {
  width: 200px;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.img {
  width: 28px;
  height: 28px;
  display: block;
  border-radius: 3px;
  margin-right: 10px;
}
.img:hover {
  background-color: #eadd4d;
}
</style>