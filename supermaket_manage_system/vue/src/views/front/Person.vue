<template>
  <div class="front-container" style="width: 40%">
    <div class="card" style="padding: 20px">
      <div style="font-size: 20px; margin-bottom: 40px; text-align: center">个人页面</div>
      <el-form ref="formRef" :model="data.user" :rules="data.rules" label-width="80px" style="padding-right: 30px">
        <el-form-item label="头像" prop="avatar">
          <el-upload
              class="avatar-uploader"
              :action="baseUrl + '/files/upload'"
              :show-file-list="false"
              :on-success="handleFileUpload"
          >
            <img v-if="data.user.avatar" :src="data.user.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item prop="username" label="账号">
          <el-input :disabled="data.user.id !== undefined" v-model="data.user.username" placeholder="请输入账号" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="name" label="姓名">
          <el-input v-model="data.user.name" placeholder="请输入姓名" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="account" label="账户余额">
          <div style="color: green; font-weight: bold">{{ data.user.account }}￥</div>
        </el-form-item>

        <!-- 积分显示区域 -->
        <el-divider content-position="center">会员积分</el-divider>

        <el-form-item label="当前积分">
          <div style="color: #e6a23c; font-weight: bold; font-size: 18px">
            {{ data.user.points || 0 }} 积分
          </div>
        </el-form-item>

        <el-form-item label="会员等级">
          <div :style="{ color: getLevelColor(), fontWeight: 'bold' }">
            {{ getLevelName() }}
          </div>
        </el-form-item>

        <el-form-item label="享受折扣">
          <el-tag :type="getDiscountType()" size="large">
            {{ getDiscountText() }}
          </el-tag>
        </el-form-item>

        <!-- 升级进度条 -->
        <el-form-item label="升级进度" v-if="nextLevelInfo">
          <div style="width: 100%">
            <el-progress
                :percentage="nextLevelInfo.percentage"
                :color="nextLevelInfo.color"
            />
            <div style="margin-top: 8px; font-size: 12px; color: #909399">
              距离{{ nextLevelInfo.levelName }}还差 {{ nextLevelInfo.needPoints }} 积分
            </div>
          </div>
        </el-form-item>

      </el-form>
      <div style="text-align: center">
        <el-button type="primary" size="large" @click="update">保存</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>

import { reactive, ref, computed } from "vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";

const emit = defineEmits(['updateUser'])
const formRef = ref()
const baseUrl = import.meta.env.VITE_BASE_URL

// 会员等级配置（根据积分）
const levelConfig = [
  { name: '青铜会员', minPoints: 0, maxPoints: 499, discount: 1.00, discountText: '无折扣', color: '#cd7f32', tagType: 'info' },
  { name: '白银会员', minPoints: 500, maxPoints: 999, discount: 0.99, discountText: '99折', color: '#87ceeb', tagType: 'success' },
  { name: '黄金会员', minPoints: 1000, maxPoints: 1999, discount: 0.98, discountText: '98折', color: '#ffa500', tagType: 'warning' },
  { name: '铂金会员', minPoints: 2000, maxPoints: 3999, discount: 0.95, discountText: '95折', color: '#c0c0c0', tagType: 'primary' },
  { name: '钻石会员', minPoints: 4000, maxPoints: 9999, discount: 0.93, discountText: '93折', color: '#00bfff', tagType: 'primary' },
  { name: '至尊会员', minPoints: 10000, maxPoints: 999999, discount: 0.90, discountText: '9折', color: '#ffd700', tagType: 'danger' }
]

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  rules: {
    username: [
      { required: true, message: '请输入账号。', trigger: 'blur' },
    ]
  }
})

// 获取当前会员等级
const getCurrentLevel = () => {
  const points = data.user.points || 0
  return levelConfig.find(level => points >= level.minPoints && points <= level.maxPoints) || levelConfig[0]
}

// 获取等级名称
const getLevelName = () => {
  return getCurrentLevel().name
}

// 获取等级颜色
const getLevelColor = () => {
  return getCurrentLevel().color
}

// 获取折扣文本
const getDiscountText = () => {
  return getCurrentLevel().discountText
}

// 获取折扣标签类型
const getDiscountType = () => {
  return getCurrentLevel().tagType
}

// 计算下一等级信息
const nextLevelInfo = computed(() => {
  const points = data.user.points || 0
  const currentLevel = getCurrentLevel()
  const currentIndex = levelConfig.findIndex(level => level.name === currentLevel.name)

  // 如果是最高等级，没有下一级
  if (currentIndex === levelConfig.length - 1) {
    return null
  }

  const nextLevel = levelConfig[currentIndex + 1]
  const needPoints = nextLevel.minPoints - points
  let percentage = Math.floor((points / nextLevel.minPoints) * 100)
  if (percentage > 100) percentage = 100

  let color = '#67c23a'
  if (percentage < 30) color = '#f56c6c'
  else if (percentage < 60) color = '#e6a23c'

  return {
    levelName: nextLevel.name,
    needPoints: needPoints,
    percentage: percentage,
    color: color
  }
})

const loadUser = () =>{
  request.get('user/selectById/' + data.user.id).then(res =>{
    data.user = res.data
    // 如果用户没有points字段，初始化为0
    if (data.user.points === undefined) {
      data.user.points = 0
    }
    //存储最新的用户信息
    localStorage.setItem('system-user', JSON.stringify(res.data))
    emit('updateUser')
  })
}
loadUser()

const update = () =>{
  request.put('user/update', data.user).then(res =>{
    if(res.code === '200'){
      ElMessage.success('修改成功')
      loadUser()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleFileUpload = (response) =>{
  data.user.avatar = response.data
}

</script>

<style scoped>
.avatar-uploader .avatar {
  width: 130px;
  height: 130px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 130px;
  height: 130px;
  text-align: center;
}
</style>