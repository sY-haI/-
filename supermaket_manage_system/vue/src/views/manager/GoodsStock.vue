<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <div style="display: flex; gap: 20px; align-items: center;">
        <el-input v-model="searchName" style="width: 300px" placeholder="请输入商品名称查询"></el-input>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button type="info" @click="reset">重置</el-button>
      </div>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table :data="sortedData" stripe>
        <el-table-column label="商品名称" prop="name"></el-table-column>
        <el-table-column prop="img" label="图片">
          <template #default="scope">
            <el-image v-if="scope.row.img" style="width: 100px; height: 100px;"
                      :src="scope.row.img" :preview-src-list="[scope.row.img]" preview-teleported></el-image>
          </template>
        </el-table-column>
        <el-table-column label="当前库存" prop="store" width="200">
          <template #default="scope">
            <span :style="{ color: scope.row.store <= 10 ? '#f56c6c' : '#67c23a', fontWeight: 'bold' }">
              {{ scope.row.store }}
            </span>
            <el-tag v-if="scope.row.store <= 0" type="danger" size="small" style="margin-left: 10px">缺货</el-tag>
            <el-tag v-else-if="scope.row.store <= 10" type="warning" size="small" style="margin-left: 10px">库存预警</el-tag>
          </template>
        </el-table-column>
        <!-- 临期状态列 -->
        <el-table-column label="临期状态" width="200">
          <template #default="scope">
            <template v-if="scope.row.store > 0 && scope.row.remainingDays != null">
              <span :style="{ color: scope.row.remainingDays <= 30 ? '#f56c6c' : '#909399', fontWeight: 'bold' }">
                {{ scope.row.earliestExpireDate || '-' }}
              </span>
              <el-tag v-if="scope.row.remainingDays <= 0" type="danger" size="small" style="margin-left: 10px">已过期</el-tag>
              <el-tag v-else-if="scope.row.remainingDays <= 30" type="warning" size="small" style="margin-left: 10px">即将过期</el-tag>
              <span v-else style="color: #909399; margin-left: 10px; font-size: 12px;">
                剩余{{ scope.row.remainingDays }}天
              </span>
            </template>
            <template v-else-if="scope.row.store <= 0">
              <span style="color: #909399;">已售罄</span>
            </template>
            <span v-else style="color: #909399;">无批次信息</span>
          </template>
        </el-table-column>
        <!-- 操作列：处理按钮 -->
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button
                v-if="scope.row.store > 0 && scope.row.remainingDays != null && scope.row.remainingDays <= 30"
                type="primary" size="small"
                @click="handleMarkProcessed(scope.row)">
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination @current-change="handlePageChange" background layout="total, prev, pager, next"
                     v-model:page-size="pageSize" v-model:current-page="pageNum" :total="total"/>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const searchName = ref('')

// 排序：缺货 > 临期预警（30天内，库存>0） > 库存预警（1-10） > 正常
const sortedData = computed(() => {
  return [...tableData.value].sort((a, b) => {
    const outOfStockA = a.store <= 0
    const outOfStockB = b.store <= 0
    if (outOfStockA && !outOfStockB) return -1
    if (!outOfStockA && outOfStockB) return 1
    if (outOfStockA && outOfStockB) return a.store - b.store

    const expireA = (a.remainingDays != null) ? a.remainingDays : Number.MAX_VALUE
    const expireB = (b.remainingDays != null) ? b.remainingDays : Number.MAX_VALUE

    const isExpireWarningA = expireA <= 30
    const isExpireWarningB = expireB <= 30
    if (isExpireWarningA && !isExpireWarningB) return -1
    if (!isExpireWarningA && isExpireWarningB) return 1
    if (isExpireWarningA && isExpireWarningB) return expireA - expireB

    const isStockWarningA = a.store <= 10
    const isStockWarningB = b.store <= 10
    if (isStockWarningA && !isStockWarningB) return -1
    if (!isStockWarningA && isStockWarningB) return 1
    if (isStockWarningA && isStockWarningB) return a.store - b.store

    return a.store - b.store
  })
})

const load = () => {
  request.get('/goods/selectPage', {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      name: searchName.value
    }
  }).then(res => {
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0

    const warningStock = tableData.value.filter(item => item.store > 0 && item.store <= 10).length
    const warningExpire = tableData.value.filter(item => item.store > 0 && item.remainingDays != null && item.remainingDays <= 30).length
    let msg = []
    if (warningStock > 0) msg.push(`${warningStock} 个商品库存不足`)
    if (warningExpire > 0) msg.push(`${warningExpire} 个商品临期`)
    if (msg.length > 0) {
      ElMessage.warning(msg.join('，') + '，请及时处理！')
    }
  })
}

// 处理：批量标记该商品下所有未处理记录为已处理
const handleMarkProcessed = (row) => {
  if (!row.id) {
    ElMessage.error('商品信息错误')
    return
  }
  ElMessageBox.confirm(
      `确认商品“${row.name}”的所有临期/过期库存已处理吗？处理后将不再预警。`,
      '确认处理',
      { type: 'warning' }
  ).then(() => {
    request.put(`/purchaseRecord/handleByGoods/${row.id}`).then(res => {
      if (res.code === '200') {
        ElMessage.success('处理成功')
        load()
      } else {
        ElMessage.error(res.msg || '处理失败')
      }
    }).catch(() => {
      ElMessage.error('请求失败')
    })
  }).catch(() => {})
}

const handlePageChange = () => load()
const reset = () => {
  searchName.value = ''
  pageNum.value = 1
  load()
}

load()
</script>

<style scoped>
.card {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, .12), 0 0 6px rgba(0, 0, 0, .04);
}
</style>