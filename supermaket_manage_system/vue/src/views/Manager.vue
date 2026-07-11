<template>
  <div>
    <div style="height: 60px; background-color: #2e3143; display: flex; align-items: center; border-bottom: 1px solid #ddd">
      <div style="flex: 1">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/logo.png" alt="" style="width: 40px">
          <div style="font-weight: bold; font-size: 24px; margin-left: 5px; color: #fff">超市管理系统</div>
        </div>
      </div>
      <div style="width: fit-content; padding-right: 10px; display: flex; align-items: center;">
        <img style="width: 40px; height: 40px; border-radius: 50%" :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="">
        <span style="color: #fff; margin-left: 5px">{{ data.user.name}}</span>
      </div>
    </div>

    <div style="display: flex">
      <div style="width: 200px; border-right: 1px solid #ddd; min-height: calc(100vh - 60px)">
        <el-menu
            router
            style="border: none"
            :default-active="router.currentRoute.value.path"
            :default-openeds="['user', 'info']"
        >
          <el-menu-item index="/manager/home">
            <el-icon><HomeFilled /></el-icon>
            <span>系统首页</span>
          </el-menu-item>
          <el-sub-menu index="user">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/manager/user">
              <el-icon><User /></el-icon>
              <span>普通用户</span>
            </el-menu-item>
            <el-menu-item index="/manager/admin">
              <el-icon><User /></el-icon>
              <span>管理员信息</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="info">
            <template #title>
              <el-icon><Memo /></el-icon>
              <span>基础信息管理</span>
            </template>
            <el-menu-item index="/manager/category">
              <el-icon><Document /></el-icon>
              <span>商品分类</span>
            </el-menu-item>
            <el-menu-item index="/manager/goods">
              <el-icon><Document /></el-icon>
              <span>商品信息</span>
            </el-menu-item>
            <el-menu-item index="/manager/goodsStatusAndRecommend">
              <el-icon><Document /></el-icon>
              <span>商品状态信息</span>
            </el-menu-item>
            <el-menu-item index="/manager/carousel">
              <el-icon><Document /></el-icon>
              <span>轮播信息</span>
            </el-menu-item>
            <el-menu-item index="/manager/comment">
              <el-icon><Document /></el-icon>
              <span>商品评价</span>
            </el-menu-item>
            <el-menu-item index="/manager/collect">
              <el-icon><Document /></el-icon>
              <span>商品收藏信息</span>
            </el-menu-item>
            <el-menu-item index="/manager/recharge">
              <el-icon><Document /></el-icon>
              <span>用户充值记录</span>
            </el-menu-item>
          </el-sub-menu >
          <el-sub-menu index="3">
            <template #title>
              <el-icon><Memo /></el-icon>
              <span>订单信息管理</span>
            </template>
            <el-menu-item index="/manager/orders">
              <el-icon><Document /></el-icon>
              <span>订单信息</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="4">
            <template #title>
              <el-icon><Memo /></el-icon>
              <span>供货商信息管理</span>
            </template>
            <el-menu-item index="/manager/supplier">
              <el-icon><Document /></el-icon>
              <span>供货商信息</span>
            </el-menu-item>
            <el-menu-item index="/manager/purchaseRecord">
              <el-icon><Document /></el-icon>
              <span>进货订单</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="5">
            <template #title>
              <el-icon><Memo /></el-icon>
              <span>库存管理</span>
            </template>
            <el-menu-item index="/manager/goodsStock">
              <el-icon><Document /></el-icon>
              <span>商品库存信息</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/manager/person">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/manager/password">
            <el-icon><Lock /></el-icon>
            <span>修改密码</span>
          </el-menu-item>
          <el-menu-item @click="logout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出系统</span>
          </el-menu-item>
        </el-menu>
      </div>
      <div style="flex: 1; width: 0; background-color: #f8f8ff; padding: 10px">
        <router-view @updateUser="updateUser" />
      </div>
    </div>

  </div>
</template>

<script setup>
import { reactive } from "vue";
import router from "@/router";
import {ElMessage} from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}')
})

if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}

const logout = () => {
  router.push('/login')
  ElMessage.success('退出成功')
  localStorage.removeItem('system-user')
}
</script>

<style scoped>
.el-menu-item.is-active {
  background-color: #979797 !important;
}
.el-menu-item:hover {
  color: #000;
}
</style>