<template>
  <div class="card">
    <div style="margin-bottom: 10px">
      <el-input v-model="searchName" style="width: 300px; margin-right: 10px" placeholder="请输入商品名称查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
    </div>
    <el-table :data="tableData" stripe>
      <el-table-column prop="name" label="商品名称"></el-table-column>
      <el-table-column prop="img" label="图片">
        <template #default="scope">
          <el-image v-if="scope.row.img" style="width: 100px; height: 100px;"
                    :src="scope.row.img" :preview-src-list="[scope.row.img]" preview-teleported></el-image>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="上架状态" width="150">
        <template #default="scope">
          <el-switch
              v-model="scope.row.statusSwitch"
              active-value="上架"
              inactive-value="下架"
              @change="updateStatus(scope.row)">
          </el-switch>
        </template>
      </el-table-column>
      <el-table-column label="推荐" width="200">
        <template #default="scope">
          <el-switch
              v-model="scope.row.recommendActive"
              @change="updateRecommend(scope.row)">
          </el-switch>
        </template>
      </el-table-column>
      <el-table-column prop="saleCount" label="销量" width="100"></el-table-column>
    </el-table>
    <div style="margin-top: 10px">
      <el-pagination @current-change="load" background layout="total, prev, pager, next" v-model:page-size="pageSize" v-model:current-page="pageNum" :total="total"/>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const searchName = ref('')

const load = () => {
  request.get('/goods/selectPage', {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      name: searchName.value
    }
  }).then(res => {
    const list = res.data?.list || []
    list.forEach(v => {
      // 初始化状态开关
      v.statusSwitch = v.status
      // 初始化推荐开关
      v.recommendActive = v.recommend === '是'
    })
    tableData.value = list
    total.value = res.data?.total || 0
  })
}

const reset = () => {
  searchName.value = ''
  load()
}

const updateStatus = (row) => {
  const form = { id: row.id, status: row.statusSwitch }
  request.put('/goods/update', form).then(res => {
    if (res.code === '200') {
      ElMessage.success('状态更新成功')
      load()
    } else {
      ElMessage.error(res.msg)
      // 恢复原状态
      row.statusSwitch = row.status
    }
  })
}

const updateRecommend = (row) => {
  const form = { id: row.id, recommend: row.recommendActive ? '是' : '否' }
  request.put('/goods/update', form).then(res => {
    if (res.code === '200') {
      ElMessage.success('推荐状态更新成功')
      load()
    } else {
      ElMessage.error(res.msg)
      row.recommendActive = !row.recommendActive
    }
  })
}

load()
</script>