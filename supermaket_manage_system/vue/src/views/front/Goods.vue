<template>
  <div class="front-container">
    <div style="display: flex; grid-gap: 20px; margin-bottom: 20px">
      <div style="flex: 1; display: flex; align-items: center; grid-gap: 20px">
        <div :class="{'active': null === data.categoryId}" @click="loadByCategory(null)"
             style="font-size: 18px; padding-bottom: 5px; cursor: pointer">全部</div>
        <div :class="{'active': item.id === data.categoryId}" @click="loadByCategory(item.id)"
             style="font-size: 18px; padding-bottom: 5px; cursor: pointer" v-for="item in data.categoryList"
             :key="item.id">{{ item.name }}</div>
      </div>
      <div>
        <el-input clearable @clear="load" style="height: 40px; width: 300px" v-model="data.name" placeholder="请输入商品名称"></el-input>
        <el-button @click="load" type="primary" style="height: 40px">搜 索</el-button>
      </div>
    </div>
    <div v-if="data.total > 0">
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in data.tableData" :key="item.id">
          <div @click="router.push('/front/goodsDetail?id=' + item.id)" class="card goods-item"
               style="width: 100%; padding: 0; border-radius: 5px; margin-bottom: 20px" >
            <img :src="item.img" alt="" style="width: 100%; height: 260px; border-radius: 5px 5px 5px 5px">
            <div style="padding: 5px">
              <div class="line1" style="font-size: 16px;">{{ item.name }}</div>
              <div>
                <span style="color: red;">￥</span><b style="color: red; font-size: 20px">{{ item.price }} </b>
                <span style="margin-left: 10px; color: #979797"> 销量: {{ item.saleCount }}件</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
      <div class="card" v-if="data.total > 0">
        <el-pagination @current-change="load" background layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
      </div>

    <div v-else style="padding: 50px; text-align: center; color: #666666">暂无数据</div>
  </div>
</template>

<script setup>

import {reactive} from "vue";
import router from "@/router";
import request from "@/utils/request";

const data = reactive({
  name: router.currentRoute.value.query.name,
  pageNum: 1,
  pageSize: 12,
  total: 0,
  tableData: [],
  categoryId: null,
  categoryList: [],
})

const loadByCategory = (categoryId) =>{
  data.categoryId = categoryId
  load()
}

const load = () => {
  request.get('/goods/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      status: '上架',
      name: data.name,
      categoryId : data.categoryId,
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()

const loadCategory = () => {
  request.get('/category/selectAll').then(res => {
    data.categoryList = res.data
  })
}
loadCategory()

const clearPathParam = () => {
  let url = location.href
  url = url.replace(/(\?|#)[^'"]*/, '');           //去除参数
  window.history.pushState({},0, url);
}
clearPathParam()
</script>

<style scoped>
.active{
  font-weight: bold;
  color: #F64848;
  border-bottom: 2px  solid #F64848;
}
.goods-item{
  cursor: pointer;
  transition: all 0.5s;
}
.goods-item:hover{
  transform: scale(1.05);
}
</style>