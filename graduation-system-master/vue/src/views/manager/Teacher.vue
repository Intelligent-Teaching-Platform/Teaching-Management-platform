<template>
  <div class="container">
    <div class="card" style="margin-bottom: 5px">
    <el-input v-model="data.name" placeholder="请输入教师姓名" style="width: 200px;margin-right: 10px"></el-input>
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
                <div class="avatar-placeholder"><span>头像</span></div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column label="名称" prop="name"></el-table-column>
        <el-table-column label="性别" prop="sex"></el-table-column>
        <el-table-column label="职称" prop="title"></el-table-column>
        <el-table-column label="所属学院" prop="collegeName"></el-table-column>
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
      <el-pagination background layout="prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" @current-change="changePage"/>
    </div>
    <el-dialog title="教师信息" width="40%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px">
        <el-form-item label="头像" prop="avatar">
          <el-upload :action="uploadUrl" list-type="picture" :on-success="handleImgSuccess">
            <el-button type="primary">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input v-model="data.form.username" autocomplete="off" placeholder="请输入账号"/>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" placeholder="请输入姓名"/>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
         <el-select v-model="data.form.sex" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-select v-model="data.form.title" placeholder="请选择职称" style="width: 100%">
            <el-option label="讲师" value="讲师"></el-option>
            <el-option label="教授" value="教授"></el-option>
            <el-option label="副教授" value="副教授"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="所属学院" prop="specialityId">
          <el-select v-model="data.form.specialityId" placeholder="请选择学院">
            <el-option
                v-for="item in uniqueCollegeOptions"
                :key="item.id"
                :label="item.collegeName || item.name"
                :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save" >保 存</el-button>
      </span>
      </template>
    </el-dialog>

  </div>

</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { compileString } from 'sass'
import { getUploadUrl, resolveAvatarUrl } from '@/utils/appConfig'

const uploadUrl = getUploadUrl()
const fileInput = ref(null);



const reset= () => {
  data.name = null
  load()
}
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定要删除吗？', '确认删除', {type:'warning'}).then(() => {
    request.delete("/teacher/delete/" + id).then(res => {
      if (res.code === '200') {
        ElMessage.success("删除成功")
        load()
      } else {
        ElMessage.error(res.msg)}
    })
  }).catch(err=> {})
}
//文件上传接口

const load =() => {
  request.get("/teacher/selectPage",{
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
    }
  }).then(res => {
    if (res.code === '200'){
      data.tableData = res.data?.list
      data.total = res.data?.total
    }else{
      ElMessage.error(res.msg)
    }
    /**
     * 搜素
     */
  })

}
const changePage = (pageNum) => {
  data.pageNum =pageNum
  load()
  /**
   * 分页
   */
}

const data =reactive({
  formVisible:false,
  form:{},
  tableData:[],
  pageNum:1,
  pageSize:5,
  total:0,
  name: null,
  specialityData: [],
})

const uniqueCollegeOptions = computed(() => {
  const map = new Map()
  ;(data.specialityData || []).forEach(item => {
    const label = (item?.collegeName || item?.name || '').trim()
    if (!label || map.has(label)) return
    map.set(label, item)
  })
  return Array.from(map.values())
})
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
  /**
   * 添加
   */
}
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true

  /**
   * 编辑  数据回显
   */
}
const add = () => {
  request.post("/teacher/add", data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success("添加成功");
      data.formVisible = false;
      load();
    }
    else {
      ElMessage.error(res.msg);
      /**
       * 新增
       */
    }
  });
}
const update = () => {
  request.put("/teacher/update", data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success("操作成功");
      data.formVisible = false;
      load();
    }
    else {
      ElMessage.error(res.msg);
      /**
       * 编辑
       */
    }
  });
}



function save() {
data.form.id ? update() : add()
}
// 处理文件上传的钩子
const handleImgSuccess = (res) => {
  data.form.avatar = res.data  // res.data就是文件上传返回的文件路径，获取到路径后赋值表单的属性
}
const loadSpeciality = () => {
  request.get("/speciality/selectAll").then(res => {
    if (res.code === '200') {
      data.specialityData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}

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


    const res = await request.post('/teacher/import', formData);

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

loadSpeciality()
load()
</script>

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
  font-size: 12px;
  color: #999;
}
</style>