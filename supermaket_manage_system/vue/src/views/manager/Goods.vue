<template>
  <div>

    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" style="width: 300px; margin-right: 10px" placeholder="请输入名称查询"></el-input>
      <el-select style="width: 300px; margin-right: 10px" v-model="data.categoryId" placeholder="请选择商品分类">
        <el-option v-for="item in data.categoryList" :key="item.id" :value="item.id" :label="item.name"></el-option>
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table tooltip-effect="dark myEff" :data="data.tableData" stripe>
        <el-table-column prop="name" label="名称" show-overflow-tooltip></el-table-column>
        <el-table-column prop="img" label="图片">
          <template #default="scope">
            <el-image v-if="scope.row.img" style="width: 100px; height: 100px;"
                      :src="scope.row.img" :preview-src-list="[scope.row.img]" preview-teleported></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格"></el-table-column>
        <el-table-column prop="description" label="简介" show-overflow-tooltip></el-table-column>
        <el-table-column prop="content" label="详情">
          <template #default="scope">
            <el-button type="primary" @click="view(scope.row.content)">查看内容</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类名称"></el-table-column>
        <el-table-column prop="saleCount" label="销量"></el-table-column>
        <el-table-column prop="time" label="创建时间"></el-table-column>

        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination @current-change="load" background layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <el-dialog title="商品信息" width="50%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px; padding-top: 20px">
        <el-form-item prop="name" label="名称">
          <el-input v-model="data.form.name" placeholder="请输入名称"></el-input>
        </el-form-item>
        <el-form-item prop="img" label="图片">
          <el-upload :action="uploadUrl" list-type="picture" :on-success="handleImgSuccess">
            <el-button type="primary">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="price" label="价格">
          <el-input-number :min="0" v-model="data.form.price" placeholder="请输入价格"></el-input-number>
        </el-form-item>
        <el-form-item prop="description" label="简介">
          <el-input type="textarea" :rows="3" v-model="data.form.description" placeholder="请输入简介"></el-input>
        </el-form-item>
        <el-form-item prop="categoryId" label="分类ID">
          <el-select style="width: 100%" v-model="data.form.categoryId">
            <el-option v-for="item in data.categoryList" :key="item.id" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item prop="content" label="详情">
          <div style="border: 1px solid #ccc; width: 100%">
            <Toolbar
                style="border-bottom: 1px solid #ccc"
                :editor="editorRef"
                :mode="mode"
            />
            <Editor
                style="height: 500px; overflow-y: hidden;"
                v-model="data.form.content"
                :mode="mode"
                :defaultConfig="editorConfig"
                @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">保 存</el-button>
      </span>
      </template>
    </el-dialog>

    <el-dialog title="内容" v-model="data.viewVisible" width="50%" :close-on-click-modal="false" destroy-on-close>
      <div class="editor-content-view" style="padding: 20px" v-html="data.content"></div>
      <template #footer>
    <span class="dialog-footer">
      <el-button @click="data.viewVisible = false">关 闭</el-button>
    </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive, ref} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";
import '@wangeditor/editor/dist/css/style.css'
import {onBeforeUnmount, shallowRef} from "vue";
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const formRef = ref()
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  categoryList: [],
  name: null,
  categoryId: null,
  viewVisible: false,
  content: null,
  rules: {
    name: [
      { required: true, message: '请输入名称', trigger: 'blur' }
    ],
    img: [
      { required: true, message: '请输入图片', trigger: 'blur' }
    ],
    price: [
      { required: true, message: '请输入价格', trigger: 'blur' }
    ],
    description: [
      { required: true, message: '请输入简介', trigger: 'blur' }
    ],
    content: [
      { required: true, message: '请输入详情', trigger: 'blur' }
    ],
    // 库存校验规则已移除
    categoryId: [
      { required: true, message: '请选择分类ID', trigger: 'change' }
    ],
    // 商品状态校验规则已移除
  }
})

// 富文本编辑器初始化
const baseUrl = import.meta.env.VITE_BASE_URL
const editorRef = shallowRef()
const mode = 'default'
const editorConfig = { MENU_CONF: {} }
editorConfig.MENU_CONF['uploadImage'] = {
  server: baseUrl + '/files/wang/upload',
  fieldName: 'file'
}
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})
const handleCreated = (editor) => {
  editorRef.value = editor
}

const view = (content) => {
  data.content = content
  data.viewVisible = true
}

const load = () => {
  request.get('/goods/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      categoryId : data.categoryId,
    }
  }).then(res => {
    data.tableData = res.data?.list
    // 移除了推荐状态的处理逻辑
    data.total = res.data?.total
  })
}

const handleAdd = () => {
  data.form = { price: '0' }
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// updateRecommend 函数已移除

const add = () => {
  request.post('/goods/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/goods/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const save = () => {
  formRef.value.validate(valid =>{
    if(valid){
      data.form.id ? update() : add()
    }
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/goods/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

const reset = () => {
  data.name = null
  data.categoryId = null
  load()
}

request.get('/category/selectAll').then(res => {
  data.categoryList = res.data
})

const handleImgSuccess = (res) => {
  data.form.img = res.data
}

load()
</script>