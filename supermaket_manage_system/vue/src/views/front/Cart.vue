<template>
  <div class="front-container">
    <div class="card" style="padding: 10px">
      <div>
        <el-table :data="data.tableData" stripe @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55"/>
          <el-table-column prop="goodsName" label="商品名称"></el-table-column>
          <el-table-column prop="goodsImg" label="商品图片">
            <template #default="scope">
              <img style="width: 50px; height: 50px" :src="scope.row.goodsImg" alt="">
            </template>
          </el-table-column>
          <el-table-column label="商品数量">
            <template #default="scope">
              <el-input-number @change="changeNum(scope.row)" v-model="scope.row.num" :min="1" style="width: 150px;"></el-input-number>
            </template>
          </el-table-column>
          <el-table-column label="商品单价">
            <template #default="scope">
              <b style="color: red">￥{{ scope.row.goodsPrice }}</b>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="100">
            <template #default="scope">
              <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div style="text-align: right; margin-top: 20px; color: red; font-size: 20px">
        总价格:
        <b style="color: red; display: inline-block; min-width: 60px; text-align: left">￥{{ formatPrice(data.total) }} 元</b>
        <div style="margin-top: 20px">
          <el-button :disabled="data.total === 0" @click="handleAddOrder" type="danger">立即下单</el-button>
        </div>
      </div>
    </div>

    <!-- 下单对话框 -->
    <el-dialog title="下单信息" width="35%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="100px" style="padding-right: 30px">
        <el-form-item label="商品数量">
          <el-input :value="data.selectedRows.length + ' 种商品'" disabled />
        </el-form-item>
        <el-form-item label="商品总价">
          <el-input :value="'￥' + formatPrice(data.total)" disabled style="color: red; font-weight: bold;" />
        </el-form-item>
        <el-divider />
        <el-form-item label="账户余额">
          <el-input :value="'￥' + formatPrice(data.user.account)" disabled>
            <template #suffix>
              <span style="color: #67C23A;">可用</span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="需支付">
          <el-input :value="'￥' + formatPrice(data.total)" disabled style="color: red; font-weight: bold;">
            <template #suffix>
              <span style="color: #F56C6C;">应付</span>
            </template>
          </el-input>
        </el-form-item>
        <el-divider />
        <el-form-item label="配送类型" prop="deliverType">
          <el-radio-group v-model="data.form.deliverType">
            <el-radio-button value="自提" label="自提"></el-radio-button>
            <el-radio-button value="外送" label="外送"></el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 外送地址改为下拉选择 -->
        <el-form-item label="收货地址" prop="address" v-if="data.form.deliverType === '外送'">
          <el-select
              v-model="data.form.address"
              placeholder="请选择收货地址"
              style="width: 100%"
              @focus="loadAddressList"
          >
            <el-option
                v-for="item in data.addressList"
                :key="item.id"
                :label="`${item.receiverName} ${item.receiverPhone} ${item.detailAddress}`"
                :value="`收件人：${item.receiverName}，电话：${item.receiverPhone}，地址：${item.detailAddress}`"
            />
          </el-select>
          <div v-if="data.addressList.length === 0" style="color: #F56C6C; font-size: 12px; margin-top: 5px;">
            你还没有收货地址，请先新增
          </div>
          <div style="margin-top: 10px;">
            <el-button type="success" size="small" @click="openAddAddressDialog">
              <el-icon><Plus /></el-icon> 新增收货地址
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="danger" @click="data.formVisible = false">取 消</el-button>
          <el-button
              type="primary"
              @click="addOrder"
              :disabled="parseFloat(data.user.account || 0) < data.total ||
                      (data.form.deliverType === '外送' && !data.form.address)"
          >
            确认购买
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 新增地址弹窗 -->
    <el-dialog title="新增收货地址" width="450px" v-model="data.addAddressVisible"
               :close-on-click-modal="false" destroy-on-close>
      <el-form ref="addressFormRef" :model="data.newAddress" :rules="addressRules" label-width="100px" style="padding-right: 30px; padding-top: 20px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="data.newAddress.receiverName" placeholder="请输入收货人姓名"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="receiverPhone">
          <el-input v-model="data.newAddress.receiverPhone" placeholder="请输入手机号码"></el-input>
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="data.newAddress.detailAddress" type="textarea" :rows="2"
                    placeholder="街道、小区、门牌号等"></el-input>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="data.newAddress.isDefault" active-value="1" inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.addAddressVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitAddress">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
  <Footer />
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref, nextTick } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import Footer from "@/components/Footer.vue";
import { Plus } from '@element-plus/icons-vue';

const formRef = ref();
const addressFormRef = ref();

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  selectedRows: [],
  addressList: [],           // 地址列表
  addAddressVisible: false,  // 新增地址弹窗
  newAddress: {              // 新地址表单
    receiverName: '',
    receiverPhone: '',
    detailAddress: '',
    isDefault: '0'
  },
  rules: {
    deliverType: [
      { required: true, message: '请选择配送类型', trigger: 'change' }
    ],
    address: [
      { required: true, message: '请选择收货地址', trigger: 'change' }
    ]
  }
});

const addressRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
};

// 格式化价格
const formatPrice = (price) => {
  if (price === null || price === undefined || price === '') return '0.00';
  const numPrice = parseFloat(price);
  return isNaN(numPrice) ? '0.00' : numPrice.toFixed(2);
};

// 加载用户余额
const loadAccount = () => {
  if (data.user.id) {
    request.get('/user/selectById/' + data.user.id).then(res => {
      if (res.code === '200' && res.data) {
        data.user.account = res.data.account || 0;
        localStorage.setItem('system-user', JSON.stringify(data.user));
      }
    }).catch(err => console.error('获取用户信息失败', err));
  }
};

// 加载地址列表
const loadAddressList = () => {
  if (!data.user.id) return;
  request.get('/userAddress/list', {
    params: { userId: data.user.id }
  }).then(res => {
    if (res.code === '200') {
      data.addressList = res.data || [];
      // 如果没有选中地址，自动选默认地址
      if (data.addressList.length > 0 && !data.form.address) {
        const defaultAddr = data.addressList[0];
        data.form.address = `收件人：${defaultAddr.receiverName}，电话：${defaultAddr.receiverPhone}，地址：${defaultAddr.detailAddress}`;
      }
    }
  }).catch(err => console.error('获取地址失败', err));
};

// 打开下单对话框
const handleAddOrder = () => {
  loadAccount();
  data.form = {
    deliverType: '自提',
    address: ''
  };
  data.formVisible = true;
  loadAddressList();
};

// 下单提交
const addOrder = () => {
  if (!data.selectedRows?.length) {
    ElMessage.warning('请选择商品');
    return;
  }
  formRef.value.validate(valid => {
    if (!valid) return;
    const account = parseFloat(data.user.account || 0);
    const needPay = data.total;
    if (isNaN(account) || isNaN(needPay)) {
      ElMessage.error('价格计算错误，请刷新页面重试');
      return;
    }
    if (account < needPay) {
      ElMessage.error('账户余额不足，请充值后再购买');
      return;
    }
    data.form.userId = data.user.id;
    data.form.cartList = data.selectedRows;
    data.form.totalPrice = needPay;

    request.post('/orders/add', data.form).then(res => {
      if (res.code === '200') {
        data.user.account = (account - needPay).toFixed(2);
        localStorage.setItem('system-user', JSON.stringify(data.user));
        ElMessage.success('下单成功');
        data.formVisible = false;
        load();
      } else {
        ElMessage.error(res.msg);
      }
    });
  });
};

// 数量变更
const changeNum = (row) => {
  calTotal();
  data.form = row;
  update();
};

// 计算总价
const calTotal = () => {
  data.total = 0;
  data.selectedRows.forEach(item => {
    data.total += item.goodsPrice * item.num;
  });
  data.total = Math.round(data.total * 100) / 100;
};

// 选择多选行
const handleSelectionChange = (rows) => {
  data.selectedRows = rows;
  calTotal();
};

// 加载购物车列表
const load = () => {
  request.get('/cart/selectAll', {
    params: { userId: data.user.id }
  }).then(res => {
    data.tableData = res.data;
  });
};

// 更新数量
const update = () => {
  request.put('/cart/update', data.form).then(res => {
    if (res.code === '200') {
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('您确定删除吗?', '删除确认', { type: 'warning' }).then(() => {
    request.delete('/cart/delete/' + id).then(res => {
      if (res.code === '200') {
        load();
        ElMessage.success('操作成功');
        calTotal();
      } else {
        ElMessage.error(res.msg);
      }
    });
  }).catch(err => {});
};

// 打开新增地址弹窗
const openAddAddressDialog = () => {
  data.newAddress = {
    receiverName: '',
    receiverPhone: '',
    detailAddress: '',
    isDefault: '0'
  };
  data.addAddressVisible = true;
  nextTick(() => addressFormRef.value?.clearValidate());
};

// 提交新增地址
const submitAddress = () => {
  addressFormRef.value.validate(valid => {
    if (!valid) return;
    const params = {
      userId: data.user.id,
      receiverName: data.newAddress.receiverName,
      receiverPhone: data.newAddress.receiverPhone,
      detailAddress: data.newAddress.detailAddress,
      isDefault: parseInt(data.newAddress.isDefault)
    };
    request.post('/userAddress/add', params).then(res => {
      if (res.code === '200') {
        ElMessage.success('地址添加成功');
        data.addAddressVisible = false;
        loadAddressList(); // 刷新下拉列表
      } else {
        ElMessage.error(res.msg || '添加失败');
      }
    });
  });
};

// 初始化
load();
loadAccount();
</script>