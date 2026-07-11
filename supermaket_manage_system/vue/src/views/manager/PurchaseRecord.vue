<template>
  <div>
    <!-- 查询区域 -->
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.supplierName" style="width: 200px; margin-right: 10px" placeholder="供货商名称" clearable></el-input>
      <el-input v-model="data.goodsName" style="width: 200px; margin-right: 10px" placeholder="商品名称" clearable></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <!-- 表格区域（无新增按钮） -->
    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="供货商名称" prop="supplierName" min-width="120"></el-table-column>
        <el-table-column label="商品名称" prop="goodsName" min-width="120"></el-table-column>
        <el-table-column label="商品图片" width="80">
          <template #default="scope">
            <el-image v-if="scope.row.goodsImg" :src="scope.row.goodsImg" style="width: 50px; height: 50px" fit="cover" preview-teleported></el-image>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="进货时间" prop="purchaseTime" width="120"></el-table-column>
        <el-table-column label="生产日期" prop="productionDate" width="120"></el-table-column>
        <!-- 已移除保质期(天)列 -->
        <el-table-column label="过期日期" prop="expirationDate" width="120"></el-table-column>
        <el-table-column label="进货数量" prop="purchaseQuantity" width="80"></el-table-column>
        <el-table-column label="操作" align="center" width="80">
          <template #default="scope">
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="card">
      <el-pagination @current-change="load" background layout="total, prev, pager, next"
                     v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue"
import request from "@/utils/request"
import { ElMessage, ElMessageBox } from "element-plus"

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  supplierName: '',
  goodsName: '',
  tableData: []
})

const load = () => {
  request.get('/purchaseRecord/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      supplierName: data.supplierName,
      goodsName: data.goodsName
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data?.total || 0
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，确定删除吗？', '删除确认', { type: 'warning' }).then(() => {
    request.delete('/purchaseRecord/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const reset = () => {
  data.supplierName = ''
  data.goodsName = ''
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