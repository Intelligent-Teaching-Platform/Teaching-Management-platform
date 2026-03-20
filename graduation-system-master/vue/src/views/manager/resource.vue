<template>
  <div class="resource-page">
    <!-- 顶部筛选区域 -->
    <div class="filter-bar">
      <el-select
        v-model="filters.courseId"
        placeholder="全部课程"
        class="filter-select"
        clearable
      >
        <el-option
          v-for="c in courses"
          :key="c.id"
          :label="c.name"
          :value="c.id"
        />
      </el-select>

      <el-input
        v-model="filters.keyword"
        placeholder="按文件名搜索"
        class="search-input"
        clearable
        @keyup.enter="doSearch"
      >
        <template #suffix>
          <el-icon @click="doSearch" class="search-icon">
            <Search />
          </el-icon>
        </template>
      </el-input>
    </div>

    <!-- 资源卡片区域：每张卡片代表一个课程“文件夹” -->
    <div class="card-grid">
      <div
        v-for="folder in filteredFolders"
        :key="folder.courseId"
        class="resource-card"
        @click="openFolder(folder)"
      >
        <div class="card-cover">
          <img :src="defaultCover" :alt="folder.courseName" />
          <span class="corner-dot"></span>
        </div>
        <div class="card-title" :title="folder.courseName">
          {{ folder.courseName }}
        </div>
        <div class="card-meta">
          <span class="meta-course">
            {{ folder.files.length }} 个文件
          </span>
        </div>
      </div>
    </div>

    <!-- 文件列表弹窗：点击课程“文件夹”后展示 -->
    <el-dialog
      v-model="fileDialogVisible"
      :title="currentFolder?.courseName || '课程资料'"
      width="900px"
      top="8vh"
      class="resource-dialog"
    >
      <el-table :data="currentFolder?.files || []" size="small" style="width: 100%">
        <el-table-column prop="name" label="文件名" min-width="260" show-overflow-tooltip />
        <el-table-column label="大小" width="100">
          <template #default="scope">
            {{ formatSize(scope.row.size) }}
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="上传者" width="120" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="downloadFile(scope.row.id)">
              下载
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import engImage from '@/assets/imgs/eng.png';
import request from '@/utils/request';

const defaultCover = engImage;

const filters = reactive({
  courseId: null,
  keyword: "",
});

// 课程列表 & 资料列表
const courses = ref([]);
const resourceList = ref([]);

const courseMap = computed(() => {
  const map = {};
  courses.value.forEach(c => { map[c.id] = c.name; });
  return map;
});

// 课程“文件夹”列表：按 courseId 分组
const folders = computed(() => {
  const grouped = new Map();
  resourceList.value.forEach((m) => {
    if (!m.courseId) return;
    const id = m.courseId;
    if (!grouped.has(id)) {
      grouped.set(id, {
        courseId: id,
        courseName: courseMap.value[id] || '未命名课程',
        files: [],
      });
    }
    grouped.get(id).files.push(m);
  });
  return Array.from(grouped.values());
});

// 过滤后的课程“文件夹”列表
const filteredFolders = computed(() => {
  let list = folders.value;
  if (filters.courseId) {
    list = list.filter(f => f.courseId === filters.courseId);
  }
  if (filters.keyword) {
    const k = filters.keyword.toLowerCase();
    list = list.filter(f =>
      String(f.courseName || '').toLowerCase().includes(k) ||
      f.files.some(file => String(file.name || '').toLowerCase().includes(k))
    );
  }
  return list;
});

const doSearch = () => {
  // 这里只是前端筛选，逻辑在 computed 里
};

const handleUpload = () => {
  ElMessage.info("请在课程详情中的资料页面上传文件");
};

const formatSize = (bytes) => {
  if (!bytes && bytes !== 0) return '';
  const b = Number(bytes);
  if (Number.isNaN(b)) return '';
  if (b < 1024) return `${b} B`;
  if (b < 1024 * 1024) return `${(b / 1024).toFixed(1)} KB`;
  if (b < 1024 * 1024 * 1024) return `${(b / 1024 / 1024).toFixed(1)} MB`;
  return `${(b / 1024 / 1024 / 1024).toFixed(1)} GB`;
};

const formatDateTime = (val) => {
  if (!val) return '';
  const d = new Date(val);
  if (Number.isNaN(d.getTime())) return val;
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} `
    + `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
};

const fileDialogVisible = ref(false);
const currentFolder = ref(null);

const openFolder = (folder) => {
  currentFolder.value = folder;
  fileDialogVisible.value = true;
};

const downloadFile = (id) => {
  if (!id) return;
  // 直接使用相对接口地址，后端处理文件下载
  window.open(`/material/download/${id}`, '_blank');
};

// 加载所有课程和资料（不区分教师）
const loadData = async () => {
  try {
    const [courseRes, materialRes] = await Promise.all([
      request.get('/course/selectAll'),
      request.get('/material/selectAll'),
    ]);
    if (courseRes.code === '200') {
      courses.value = courseRes.data || [];
    }
    if (materialRes.code === '200') {
      resourceList.value = materialRes.data || [];
    }
  } catch (e) {
    ElMessage.error('加载资源失败，请稍后重试');
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.resource-page {
  padding: 16px 20px;
  background-color: #f5f7fb;
  min-height: 100%;
  box-sizing: border-box;
  min-width:100%;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-select {
  width: 160px;
}

.upload-btn {
  border-radius: 999px;
  padding: 0 22px;
}

.search-input {
  width: 260px;
}

.search-icon {
  cursor: pointer;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 18px;
}

.resource-card {
  background-color: #ffffff;
  border-radius: 14px;
  box-shadow: 0 6px 16px rgba(15, 40, 80, 0.08);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.resource-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 22px rgba(15, 40, 80, 0.16);
}

.card-cover {
  position: relative;
  width: 100%;
  padding-top: 120px; /* 近似比例 */
  overflow: hidden;
}

.card-cover img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.corner-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #1e6fff;
  border: 2px solid #ffffff;
}

.card-title {
  padding: 10px 12px 12px;
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  padding: 0 12px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #6b7280;
}

.meta-course {
  max-width: 140px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta-size {
  flex-shrink: 0;
}

/* 资料弹窗美化 */
.resource-dialog :deep(.el-dialog) {
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 18px 40px rgba(15, 40, 80, 0.25);
}

.resource-dialog :deep(.el-dialog__header) {
  margin: 0;
  padding: 14px 20px;
  background: linear-gradient(90deg, #2563eb, #22c55e);
  color: #ffffff;
}

.resource-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
}

.resource-dialog :deep(.el-dialog__headerbtn) {
  top: 14px;
  right: 16px;
}

.resource-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(255, 255, 255, 0.85);
}

.resource-dialog :deep(.el-dialog__body) {
  padding: 18px 22px 22px;
  background: radial-gradient(circle at top left, #ebf3ff 0, #f9fafb 45%, #ecfdf5 100%);
  min-height: 420px;
}

.resource-dialog :deep(.el-table) {
  --el-table-header-bg-color: #eff6ff;
  --el-table-row-hover-bg-color: #e0f2fe;
  border-radius: 10px;
  overflow: hidden;
  font-size: 14px;
}

.resource-dialog :deep(.el-table th.el-table__cell) {
  font-weight: 600;
  color: #1f2937;
  font-size: 14px;
}

.resource-dialog :deep(.el-table td.el-table__cell) {
  font-size: 14px;
}
</style>