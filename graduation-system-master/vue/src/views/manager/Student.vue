<template>
  <div class="container">
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.name" placeholder="请输入学生姓名" style="width: 200px; margin-right: 10px"></el-input>
      <el-button type="primary" @click="load">搜索</el-button>
      <el-button type="primary" @click="reset">重置</el-button>
    </div>

    <div class="card">
      <div style="margin-bottom: 10px">
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

      <el-table :data="data.tableData" stripe>
        <el-table-column label="用户名" prop="username"></el-table-column>
        <el-table-column label="头像" prop="avatar">
          <template v-slot="scope">
            <el-image :src="resolveAvatarUrl(scope.row.avatar)" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover" fit="cover">
              <template #error>
                <div class="avatar-placeholder"><el-icon><Avatar /></el-icon></div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column label="名称" prop="name"></el-table-column>
        <el-table-column label="性别" prop="sex"></el-table-column>
        <el-table-column label="学号" prop="code"></el-table-column>
        <el-table-column label="学分" prop="score"></el-table-column>
        <el-table-column label="所属学院" prop="collegeName"></el-table-column>
        <el-table-column label="班级" prop="classId"></el-table-column>
        <el-table-column label="角色" prop="role"></el-table-column>

        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination
          background
          layout="prev, pager, next"
          v-model:page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
          @current-change="changePage"
      />
    </div>

    <el-dialog title="学生信息" width="40%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px">
        <el-form-item label="头像" prop="avatar">
          <el-upload
              :action="uploadUrl"
              list-type="picture"
              :on-success="handleImgSuccess"
              :show-file-list="false"
          >
            <el-image
                v-if="data.form.avatar"
                :src="resolveAvatarUrl(data.form.avatar)"
                style="width: 100px; height: 100px; border-radius: 50%; object-fit: cover"
                fit="cover"
            />
            <el-icon v-else size="100"><Avatar /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input v-model="data.form.username" autocomplete="off" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="data.form.sex" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学号" prop="code">
          <el-input v-model="data.form.code" autocomplete="off" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="data.form.collegeId" placeholder="请选择学院">
            <el-option
                v-for="item in data.collegeData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="所属班级" prop="classId">
          <el-select v-model="data.form.classId" placeholder="请选择班级">
            <el-option
                v-for="item in data.classData"
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
import { reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Avatar } from '@element-plus/icons-vue'
import { getUploadUrl, resolveAvatarUrl } from '@/utils/appConfig'

const uploadUrl = getUploadUrl()
const fileInput = ref(null);

const data = reactive({
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 5,
  total: 0,
  name: null,
  collegeData: [],
  classData: [],
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
    formData.append('file', file); // 确保字段名是 'file'


    const res = await request.post('/student/import', formData);

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

const reset = () => {
  data.name = null;
  load();
};

const handleDelete = (id) => {
  ElMessageBox.confirm("删除后数据无法恢复，您确定要删除吗？", "确认删除", { type: "warning" })
      .then(() => {
        request.delete("/student/delete/" + id).then((res) => {
          if (res.code === "200") {
            ElMessage.success("删除成功");
            load();
          } else {
            ElMessage.error(res.msg);
          }
        });
      })
      .catch(() => {});
};

const load = () => {
  request
      .get("/student/selectPage", {
        params: {
          pageNum: data.pageNum,
          pageSize: data.pageSize,
          name: data.name,
        },
      })
      .then((res) => {
        if (res.code === "200") {
          data.tableData = res.data?.list || [];
          data.total = res.data?.total || 0;
        } else {
          ElMessage.error(res.msg);
        }
      });
};

const changePage = (pageNum) => {
  data.pageNum = pageNum;
  load();
};

const handleAdd = () => {
  data.form = {
    sex: '男',
    role: 'STUDENT'
  };
  data.formVisible = true;
};

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row));
  data.formVisible = true;
};

const add = () => {
  request.post("/student/add", data.form).then((res) => {
    if (res.code === "200") {
      ElMessage.success("添加成功");
      data.formVisible = false;
      load();
    } else {
      ElMessage.error(res.msg);
    }
  });
};

const update = () => {
  request.put("/student/update", data.form).then((res) => {
    if (res.code === "200") {
      ElMessage.success("操作成功");
      data.formVisible = false;
      load();
    } else {
      ElMessage.error(res.msg);
    }
  });
};

const save = () => {
  if (!data.form.username) {
    ElMessage.warning("请输入账号");
    return;
  }
  if (!data.form.name) {
    ElMessage.warning("请输入姓名");
    return;
  }
  data.form.id ? update() : add();
};

const handleImgSuccess = (res) => {
  if (res.code === "200") {
    data.form.avatar = res.data;
    ElMessage.success("头像上传成功");
  } else {
    ElMessage.error(res.msg || "头像上传失败");
  }
};

const loadCollege = () => {
  request.get("/college/selectAll").then((res) => {
    if (res.code === "200") {
      data.collegeData = res.data || [];
    } else {
      ElMessage.error(res.msg);
    }
  });
};

const loadClass = () => {
  request.get("/class/selectAll").then((res) => {
    if (res.code === "200") {
      data.classData = res.data || [];
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 初始化加载数据
loadCollege();
loadClass();
load();
</script>

<style scoped>
.card {
  padding: 20px;
  margin-bottom: 10px;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}
</style>

<style lang="scss" scoped>
.container {
  background-color: #fff;
  height: 100vh;
  width: 100%;
}

.avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #999;
}
</style>