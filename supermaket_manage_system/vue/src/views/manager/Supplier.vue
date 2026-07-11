<template>
  <div>

    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" style="width: 300px; margin-right: 10px" placeholder="请输入供货商名称查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="data.tableData" stripe>
        <el-table-column label="供货商名称" prop="name"></el-table-column>
        <el-table-column label="联系方式" prop="contact"></el-table-column>
        <el-table-column label="商品名称" prop="goodsName"></el-table-column>
        <el-table-column label="进货数量" prop="purchaseQuantity"></el-table-column>
        <el-table-column label="进货时间" prop="purchaseTime"></el-table-column>
        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="success" @click="handlePurchase(scope.row)">进货</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination @current-change="load" background layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <!-- 新增弹窗（仅录入供货商信息，无进货相关字段） -->
    <el-dialog title="新增供货商" width="30%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="100px" style="padding-right: 30px; padding-top: 20px">
        <el-form-item label="供货商名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" placeholder="请输入供货商名称" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contact">
          <el-input
              v-model="data.form.contact"
              autocomplete="off"
              placeholder="请输入11位手机号"
              maxlength="11"
              @input="handlePhoneInput"
              show-word-limit
          />
          <div style="color: #909399; font-size: 12px; margin-top: 5px">
            格式：1开头，第二位3-9，共11位数字
          </div>
        </el-form-item>

        <!-- 先选择商品分类 -->
        <el-form-item label="商品分类" prop="categoryId">
          <el-select
              v-model="data.form.categoryId"
              placeholder="请先选择商品分类"
              style="width: 100%"
              @change="handleCategoryChange"
              clearable
          >
            <el-option
                v-for="item in data.categoryList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <!-- 再选择该分类下的商品 -->
        <el-form-item label="选择商品" prop="goodsId">
          <el-select
              v-model="data.form.goodsId"
              placeholder="请选择商品"
              style="width: 100%"
              @change="handleGoodsChange"
              :disabled="!data.form.categoryId"
              clearable
          >
            <el-option
                v-for="item in data.filteredGoodsList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
          <div v-if="!data.form.categoryId" style="color: #909399; font-size: 12px; margin-top: 5px">
            请先选择商品分类
          </div>
        </el-form-item>

        <!-- 已移除进货数量和进货时间 -->
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">保 存</el-button>
      </span>
      </template>
    </el-dialog>

    <!-- 进货弹窗（包含生产日期、保质期必填） -->
    <el-dialog title="商品进货" width="35%" v-model="data.purchaseVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="purchaseFormRef" :model="data.purchaseForm" :rules="data.purchaseRules" label-width="110px" style="padding-right: 30px; padding-top: 20px">
        <el-form-item label="供货商名称">
          <el-input v-model="data.purchaseForm.name" disabled />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="data.purchaseForm.contact" disabled />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-input v-model="data.purchaseForm.categoryName" disabled />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="data.purchaseForm.goodsName" disabled />
        </el-form-item>
        <el-form-item label="当前库存数量">
          <el-input v-model="data.purchaseForm.currentStore" disabled>
            <template #suffix>
              <span style="color: #909399;">件</span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="已进货总数">
          <el-input v-model="data.purchaseForm.originalQuantity" disabled>
            <template #suffix>
              <span style="color: #909399;">件</span>
            </template>
          </el-input>
        </el-form-item>

        <!-- 生产日期（必填） -->
        <el-form-item label="生产日期" prop="productionDate">
          <el-date-picker
              v-model="data.purchaseForm.productionDate"
              type="date"
              placeholder="请选择生产日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
          />
        </el-form-item>

        <!-- 保质期（数值 + 单位，必填） -->
        <el-form-item label="保质期" prop="shelfLifeNum">
          <div style="display: flex; gap: 10px; width: 100%">
            <el-input-number
                v-model="data.purchaseForm.shelfLifeNum"
                :min="1"
                :precision="0"
                style="flex: 1"
                placeholder="请输入数值"
            />
            <el-select
                v-model="data.purchaseForm.shelfLifeUnit"
                style="width: 100px"
                placeholder="单位"
            >
              <el-option label="天" value="day" />
              <el-option label="月" value="month" />
              <el-option label="年" value="year" />
            </el-select>
          </div>
        </el-form-item>

        <el-form-item label="本次进货数量" prop="purchaseQuantity">
          <el-input-number v-model="data.purchaseForm.purchaseQuantity" :min="1" style="width: 100%" placeholder="请输入本次进货数量" />
          <div style="color: #909399; font-size: 12px; margin-top: 5px">
            输入本次进货数量，系统将累加到原有进货数量上
          </div>
        </el-form-item>
        <el-form-item label="进货时间">
          <el-input v-model="data.purchaseForm.purchaseTime" disabled placeholder="系统自动获取" />
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.purchaseVisible = false">取 消</el-button>
        <el-button type="primary" @click="savePurchase">确认进货</el-button>
      </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive, ref} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";

const formRef = ref()
const purchaseFormRef = ref()

// 手机号校验
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入联系方式'))
    return
  }
  const phone = value.trim()
  if (phone.length !== 11) {
    callback(new Error('手机号必须是11位数字'))
    return
  }
  if (!/^\d+$/.test(phone)) {
    callback(new Error('手机号只能包含数字'))
    return
  }
  if (!phone.startsWith('1')) {
    callback(new Error('手机号必须以1开头'))
    return
  }
  const secondChar = phone.charAt(1)
  if (secondChar < '3' || secondChar > '9') {
    callback(new Error('手机号第二位必须是3-9之间的数字'))
    return
  }
  callback()
}

// 数量校验
const validatePurchaseQuantity = (rule, value, callback) => {
  if (value === null || value === undefined || value === '') {
    callback(new Error('请输入进货数量'))
    return
  }
  if (typeof value !== 'number' || isNaN(value)) {
    callback(new Error('进货数量必须是数字'))
    return
  }
  if (!Number.isInteger(value)) {
    callback(new Error('进货数量必须是整数'))
    return
  }
  if (value < 1) {
    callback(new Error('进货数量必须大于0，请输入正确的数量'))
    return
  }
  callback()
}

// 保质期数值校验
const validateShelfLifeNum = (rule, value, callback) => {
  if (value === null || value === undefined || value === '') {
    callback(new Error('请输入保质期数值'))
    return
  }
  if (typeof value !== 'number' || isNaN(value)) {
    callback(new Error('保质期必须是数字'))
    return
  }
  if (value < 1) {
    callback(new Error('保质期必须大于0'))
    return
  }
  callback()
}

const handlePhoneInput = (value) => {
  let newValue = value.replace(/\D/g, '')
  if (newValue.length > 11) newValue = newValue.slice(0, 11)
  data.form.contact = newValue
}

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  purchaseVisible: false,
  form: {},
  purchaseForm: {},
  tableData: [],
  name: null,
  categoryList: [],
  goodsList: [],
  filteredGoodsList: [],
  rules: {
    name: [
      { required: true, message: '请输入供货商名称', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    contact: [
      { required: true, message: '请输入联系方式', trigger: 'blur' },
      { validator: validatePhone, trigger: 'blur' }
    ],
    categoryId: [
      { required: true, message: '请选择商品分类', trigger: 'change' },
    ],
    goodsId: [
      { required: true, message: '请选择商品', trigger: 'change' },
    ]
  },
  purchaseRules: {
    productionDate: [
      { required: true, message: '请选择生产日期', trigger: 'change' }
    ],
    shelfLifeNum: [
      { required: true, message: '请输入保质期数值', trigger: 'blur' },
      { validator: validateShelfLifeNum, trigger: 'blur' }
    ],
    purchaseQuantity: [
      { required: true, message: '请输入本次进货数量', trigger: 'blur' },
      { validator: validatePurchaseQuantity, trigger: 'blur' }
    ]
  }
})

const getCurrentTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const getCurrentDate = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const loadCategoryList = () => {
  request.get('/category/selectAll').then(res => {
    if (res.code === '200') data.categoryList = res.data || []
  })
}

const loadGoodsList = () => {
  request.get('/goods/selectAll').then(res => {
    if (res.code === '200') data.goodsList = res.data || []
  })
}

const getCategoryName = (categoryId) => {
  const category = data.categoryList.find(item => item.id === categoryId)
  return category ? category.name : ''
}

const handleCategoryChange = (categoryId) => {
  data.form.goodsId = null
  data.form.goodsName = null
  if (categoryId) {
    data.filteredGoodsList = data.goodsList.filter(item => item.categoryId === categoryId)
  } else {
    data.filteredGoodsList = []
  }
}

const handleGoodsChange = (goodsId) => {
  const goods = data.filteredGoodsList.find(item => item.id === goodsId)
  if (goods) {
    data.form.goodsName = goods.name
  }
}

// 分页查询
const load = () => {
  request.get('/supplier/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data?.total || 0
  })
}

// 新增按钮（仅初始化基本信息，不设置进货数量和进货时间）
const handleAdd = () => {
  data.form = {
    name: '',
    contact: '',
    categoryId: null,
    goodsId: null,
    goodsName: null
  }
  data.filteredGoodsList = []
  data.formVisible = true
  setTimeout(() => formRef.value?.clearValidate(), 100)
}

// 进货弹窗初始化
const handlePurchase = (row) => {
  const rowData = JSON.parse(JSON.stringify(row))
  let categoryId = null, categoryName = '', currentStore = 0, goodsImg = ''

  if (rowData.goodsId) {
    const goods = data.goodsList.find(item => item.id === rowData.goodsId)
    if (goods) {
      categoryId = goods.categoryId
      categoryName = getCategoryName(categoryId)
      currentStore = goods.store || 0
      goodsImg = goods.img || ''
    }
  }

  data.purchaseForm = {
    id: rowData.id,
    name: rowData.name,
    contact: rowData.contact,
    goodsId: rowData.goodsId,
    goodsName: rowData.goodsName,
    goodsImg: goodsImg,
    categoryId: categoryId,
    categoryName: categoryName,
    currentStore: currentStore,
    originalQuantity: rowData.purchaseQuantity || 0,
    purchaseQuantity: 1,
    purchaseTime: getCurrentTime(),
    productionDate: null,
    shelfLifeNum: null,      // 保质期数值
    shelfLifeUnit: 'day'     // 保质期单位，默认天
  }

  data.purchaseVisible = true
  setTimeout(() => purchaseFormRef.value?.clearValidate(), 100)
}

// 新增保存（只发送基本信息，不含进货数量和进货时间）
const add = () => {
  request.post('/supplier/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 将保质期数值和单位转换为天数
const convertShelfLifeToDays = (num, unit) => {
  if (!num || num <= 0) return null
  switch (unit) {
    case 'year':
      return num * 365
    case 'month':
      return num * 30
    case 'day':
    default:
      return num
  }
}

// 确认进货
const savePurchase = () => {
  purchaseFormRef.value.validate(valid => {
    if (!valid) return

    // 先更新库存
    const purchaseData = {
      id: data.purchaseForm.id,
      purchaseQuantity: data.purchaseForm.purchaseQuantity,
      purchaseTime: data.purchaseForm.purchaseTime
    }

    request.put('/supplier/purchase', purchaseData).then(res => {
      if (res.code !== '200') {
        ElMessage.error(res.msg)
        return
      }

      // 计算保质期天数
      const shelfLifeDays = convertShelfLifeToDays(
          data.purchaseForm.shelfLifeNum,
          data.purchaseForm.shelfLifeUnit
      )

      const recordData = {
        supplierName: data.purchaseForm.name,
        goodsName: data.purchaseForm.goodsName,
        goodsImg: data.purchaseForm.goodsImg || null,
        purchaseTime: getCurrentDate(),
        productionDate: data.purchaseForm.productionDate,
        shelfLife: shelfLifeDays,    // 转换后的天数
        purchaseQuantity: data.purchaseForm.purchaseQuantity,
        goodsId: data.purchaseForm.goodsId || null,
        supplierId: data.purchaseForm.id
      }

      request.post('/purchaseRecord/add', recordData).then(res2 => {
        if (res2.code === '200') {
          load()
          loadGoodsList()
          ElMessage.success('进货成功，已生成进货记录')
          data.purchaseVisible = false
        } else {
          ElMessage.error('进货记录生成失败：' + res2.msg)
        }
      }).catch(() => {
        ElMessage.error('生成进货记录异常')
      })
    }).catch(() => {
      ElMessage.error('进货失败')
    })
  })
}

const save = () => {
  formRef.value.validate(valid => {
    if (valid) add()
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(() => {
    request.delete('/supplier/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const reset = () => {
  data.name = null
  load()
}

// 初始化
load()
loadCategoryList()
loadGoodsList()
</script>

<style scoped>
.card {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, .12), 0 0 6px rgba(0, 0, 0, .04);
}
</style>