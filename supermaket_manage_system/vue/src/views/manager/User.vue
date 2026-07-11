<template>
  <div class="card">
    <el-input style=" width: 300px " v-model="date.name" placeholder="请输入名称" :prefix-icon="Search"/>
    <el-button @click="load" type="primary" style="margin-left: 10px">查询</el-button>
    <el-button @click="reset" type="info">重置</el-button>
  </div>

  <div class="card" style="margin-bottom: 5px">
    <div>
      <el-button @click="handleAdd" type="primary">新增</el-button>
    </div>
    <div>
      <el-table :data="date.tableData" stripe style="width: 100%">
        <el-table-column prop="username" label="账号"  />
        <el-table-column prop="name" label="姓名"  />
        <el-table-column prop="avatar" label="头像"  >
          <template #default="scope">
            <el-image v-if="scope.row.avatar" style="width: 50px; height: 50px; display: block; border-radius: 50%"
                      :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]" preview-teleported></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色"  />
        <el-table-column prop="account" label="账户"  />
        <el-table-column label="操作" width="180px">
          <template #default="scope">
          <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination v-model:current-page="date.pageNum" v-model:page-size="date.pageSize"
                     @current-change="load" background layout="total, prev, pager, next" :total="date.total"/>
    </div>

    <el-dialog v-model="date.formVisible" title="用户信息" width="30%" destroy-on-close>
      <el-form ref="formRef" :model="date.form" :rules="date.rules" label-width="80px" style="padding-right: 30px">
        <el-form-item prop="username" label="账号">
          <el-input :disabled="date.form.id !== undefined" v-model="date.form.username" placeholder="请输入账号" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="name" label="姓名">
          <el-input v-model="date.form.name" placeholder="请输入姓名" autocomplete="off" />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <el-upload
              :action="baseUrl + '/files/upload'"
              list-type="picture"
              :on-success="handleFileUpload"
          >
            <el-button type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="date.formVisible = false">取消</el-button>
          <el-button type="primary" @click="save">确认</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, ref} from "vue";
import { Search } from "@element-plus/icons-vue";
import  request  from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";

const baseUrl = import.meta.env.VITE_BASE_URL

const formRef = ref()

const date = reactive({
  name: null,
  tableData: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,
  formVisible: false,
  form: {},
  rules: {
    username: [
      { required: true, message: '请输入账号。', trigger: 'blur' },
    ]
  }
})



//分页查询
const load = () =>{
  request.get('user/selectPage', {
    params: {
      pageNum: date.pageNum,
      pageSize: date.pageSize,
      name: date.name
    }
  }).then(res => {
    if(res.code === '200'){
      date.tableData = res.data?.list
      date.total = res.data?.total
    }else {
      ElMessage.error(res.msg)
    }
  })
}

const reset = () =>{
  date.name = null
  load()
}

const del = (id) =>{
  ElMessageBox.confirm('您确定删除吗？', '确认删除', {type: 'warning'}).then(res => {
    request.delete('/user/delete/'+ id).then(res =>{
      if(res.code === '200'){
        ElMessage.success('操作成功')
        load()
      }else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

const handleAdd = () =>{
  date.form = {}
  date.formVisible = true
}

const handleEdit = (row) =>{
  date.form = JSON.parse(JSON.stringify(row))
  date.formVisible = true
}

const add = () =>{
  request.post('user/add', date.form).then(res =>{
    if(res.code === '200'){
      ElMessage.success('操作成功')
      date.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () =>{
  request.put('user/update', date.form).then(res =>{
    if(res.code === '200'){
      ElMessage.success('操作成功')
      date.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const save = () =>{
  formRef.value.validate((valid) =>{
    if(valid){
      date.form.id ? update() : add()
    }
  })
}

//头像上传的回调函数
const handleFileUpload = (response) =>{
  date.form.avatar = response.data
}
load()
</script>