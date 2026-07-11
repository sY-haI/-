<template>
  <div>
    <div style="height: 60px; background-color: #2e3143; display: flex; align-items: center;">
      <div style="width: 20%;">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/logo.png" alt="" style="width: 40px">
          <div style="font-weight: bold; font-size: 24px; margin-left: 5px; color: #fff">百货超市</div>
        </div>
      </div>
      <div style="width: 60%; height: 60px; display: flex; align-items: center">
        <div style="flex: 1">
          <el-menu router :default-active="router.currentRoute.value.path" ellipsis mode="horizontal" style="max-width: 100%; background-color: #2e3143">
            <el-menu-item index="/front/home" >首页</el-menu-item>
            <el-menu-item index="/front/goods" >商品</el-menu-item>
            <el-menu-item index="/front/cart" >购物车</el-menu-item>
          </el-menu>
        </div>
        <div style="width: fit-content" v-if="router.currentRoute.value.path !== '/front/goods'">
          <el-input @keyup.enter="search" prefix-icon="Search" style="width: 300px; height: 40px" v-model="data.goodsName" placeholder="请输入商品名称搜索"></el-input>
        </div>
      </div>
      <div style="width: 20%; padding-right: 10px; text-align: right;">
        <el-dropdown>
          <div style="width: fit-content; padding-right: 10px; display: flex; align-items: center;">
            <img style="width: 40px; height: 40px; border-radius: 50%" :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="">
            <span style="color: #fff; margin-left: 5px">{{ data.user.name}}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click  ="router.push('/front/userCollect')">我的收藏</el-dropdown-item>
              <el-dropdown-item @click  ="router.push('/front/userOrders')">我的订单</el-dropdown-item>
              <el-dropdown-item @click  ="router.push('/front/userAddress')">我的地址</el-dropdown-item>
              <el-dropdown-item @click  ="router.push('/front/userComment')">我的评价</el-dropdown-item>
              <el-dropdown-item @click  ="router.push('/front/userRecharge')">充值记录</el-dropdown-item>
              <el-dropdown-item @click  ="router.push('/front/person')">个人信息</el-dropdown-item>
              <el-dropdown-item @click  ="router.push('/front/password')">修改密码</el-dropdown-item>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>

            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div style="background: #c5d3d3">
      <router-view @updateUser="updateUser" />
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import router from "@/router";
import {ElMessage} from "element-plus";


const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  goodsName: null
})

if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
}

const search = () =>{
  if(data.goodsName){
    router.push('/front/goods?name=' + data.goodsName)
  }
}

const logout = () =>{
  localStorage.removeItem('system-user')
  router.push('/login')
  ElMessage.success('退出成功！')
}

//更新user对象的最新值
const updateUser = () =>{
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}

</script>

<style>
.el-tooltip__trigger{
  cursor: pointer;
  outline: none;
}
.el-menu--horizontal{
  border: none !important;
}
.el-menu--horizontal .el-menu-item{
  border: none;
  height: 60px;
}

.el-menu--horizontal .el-menu-item{
  color: white;
}

.el-menu--horizontal .el-menu-item.is-active{
  border: none;
  color: white !important;
  background-color: #1967e3 !important;
}

.el-menu--horizontal .el-menu-item:not(.is-active):hover{
  color: white ;
  background-color: #13ce66 !important;
}

</style>