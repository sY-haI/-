<template>
  <div class="front-container" style="width: 50%">
    <!-- ==================== 商品信息卡片 ==================== -->
    <div class="card" style="padding: 20px; display: flex; margin-bottom: 10px; grid-gap: 20px">
      <img :src="data.goods.img" alt="" width="300px" height="300px">
      <div style="flex: 1">
        <!-- 商品标题、收藏 -->
        <div style="display: flex; align-items: center; grid-gap: 20px; margin-bottom: 10px">
          <div style="font-size: 25px; font-weight: bold; flex: 1">
            <el-tag type="danger" v-if="data.goods.recommend === '是'">推荐</el-tag>{{ data.goods.name }}
          </div>
          <div style="width: fit-content; cursor: pointer; color: #666666" @click="addCollect" v-if="!data.userCollect?.id">
            <el-icon style="position: relative; top: 2px" size="18px"><Star /></el-icon> 收藏
          </div>
          <div style="width: fit-content; cursor: pointer; color: orange" @click="removeCollect" v-if="data.userCollect?.id">
            <el-icon style="position: relative; top: 2px" size="18px"><StarFilled /></el-icon> 取消收藏
          </div>
        </div>
        <!-- 价格、销量、库存 -->
        <div style="margin-bottom: 20px">
          <span style="color: red; font-size: 18px">￥</span>
          <b style="color: red; font-size: 25px">{{ data.goods.price }}</b>
          <span style="color: #666666; margin-left: 20px">累计销量 {{ data.goods.saleCount }}</span>
          <span style="color: #666666; margin-left: 20px">剩余库存 {{ data.goods.store }}</span>
        </div>
        <!-- 描述 -->
        <div style="padding: 10px; margin-bottom: 10px; border-radius: 5px; background-color: #d6d6d6; line-height: 25px; text-align: justify">
          {{ data.goods.description }}
        </div>
        <!-- 数量、加入购物车、立即购买 -->
        <div>
          <el-input-number :min="1" v-model="data.num" style="width: 150px; height: 40px;"></el-input-number>
          <el-button @click="addCart" style="height: 40px; margin-left: 5px" type="danger">加入购物车</el-button>
          <el-button @click="handleAddOrder" style="height: 40px; margin-left: 5px" type="danger">立即购买</el-button>
        </div>
        <div style="margin-top: 10px; color: #666666">XX超市销售并发货，由超市提供发票和相应的售后服务。请放心购买！</div>
      </div>
    </div>

    <!-- ==================== 商品详情 / 评价 ==================== -->
    <div class="card" style="padding: 20px; margin-bottom: 50px">
      <div style="font-size: 20px; padding-bottom: 10px; border-bottom: 1px solid #dddddd">
        <span @click="changeTab('商品详情')" :class="{ 'current-active': data.current === '商品详情' }" style="cursor: pointer">商品详情</span>
        <span @click="changeTab('商品评价')" :class="{ 'current-active': data.current === '商品评价' }" style="margin-left: 20px; cursor: pointer">商品评价</span>
      </div>
      <div v-if="data.current === '商品详情'" style="padding: 10px" v-html="data.goods.content"></div>
      <div v-if="data.current === '商品评价'">
        <div v-if="data.commentList.length === 0" style="padding: 50px; text-align: center; color: #666666">暂无数据</div>
        <div v-if="data.commentList.length > 0" style="padding: 20px;">
          <div v-for="(item, index) in data.commentList" :key="item.id"
               style="display: flex; grid-gap: 10px; padding: 10px 0;
               border-bottom: 1px solid #dddddd"
               :style="{ 'borderWidth': index === data.commentList.length - 1 ? 0 : '1px' }">
            <img :src="item.userAvatar" alt="" style="width: 50px; height: 50px; border-radius: 50%">
            <div style="flex: 1">
              <div>
                <span>{{ item.userName }}</span>
                <span style="color: #666666; font-size: 13px; margin-left: 10px">{{ item.time }}</span>
              </div>
              <div style="margin-bottom: 5px">
                <el-rate v-model="item.score" show-score allow-half disabled></el-rate>
              </div>
              <div>{{ item.content }}</div>
            </div>
          </div>
        </div>
        <div>
          <el-pagination @current-change="loadComment" background layout="total, prev, pager, next"
                         v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
        </div>
      </div>
    </div>

    <!-- ==================== 下单对话框 ==================== -->
    <el-dialog title="下单信息" width="30%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="100px" style="padding-right: 30px">
        <!-- 商品信息展示 -->
        <el-form-item label="商品名称">
          <el-input :value="data.goods.name" disabled />
        </el-form-item>
        <el-form-item label="商品单价">
          <el-input :value="'￥' + formatPrice(data.goods.price)" disabled />
        </el-form-item>
        <el-form-item label="购买数量">
          <el-input :value="data.num" disabled />
        </el-form-item>
        <el-divider />
        <!-- 账户余额 -->
        <el-form-item label="账户余额">
          <el-input :value="'￥' + formatPrice(data.user.account)" disabled>
            <template #suffix><span style="color: #67C23A;">可用</span></template>
          </el-input>
        </el-form-item>
        <!-- 需支付 -->
        <el-form-item label="需支付">
          <el-input :value="'￥' + totalPrice" disabled style="color: red; font-weight: bold;">
            <template #suffix><span style="color: #F56C6C;">应付</span></template>
          </el-input>
        </el-form-item>
        <!-- 余额不足提醒 -->
        <el-alert
            v-if="parseFloat(data.user.account || 0) < parseFloat(totalPrice)"
            title="账户余额不足，请先充值后再购买"
            type="error"
            :closable="false"
            style="margin-bottom: 15px;"
        />
        <el-divider />

        <!-- 配送类型 -->
        <el-form-item label="配送类型" prop="deliverType">
          <el-radio-group v-model="data.form.deliverType">
            <el-radio-button value="自提" label="自提"></el-radio-button>
            <el-radio-button value="外送" label="外送"></el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 收货地址（外送时显示，改为下拉选择） -->
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
          <div style="margin-top: 5px; display: flex; align-items: center; gap: 10px;">
  <span v-if="data.addressList.length === 0" style="color: #F56C6C; font-size: 12px;">
    你还没有收货地址，请先新增
  </span>
            <el-button type="success" size="small" @click="openAddAddressDialog">
              <el-icon><Plus /></el-icon> 新增收货地址
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="data.formVisible = false">取 消</el-button>
        <el-button
            type="primary"
            @click="addOrder"
            :disabled="parseFloat(data.user.account || 0) < parseFloat(totalPrice) ||
                    (data.form.deliverType === '外送' && !data.form.address)"
        >
          确认购买
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 新增地址弹窗 ==================== -->
    <el-dialog title="新增收货地址" width="450px" v-model="data.addAddressVisible"
               :close-on-click-modal="false" destroy-on-close>
      <el-form ref="addressFormRef" :model="data.newAddress" :rules="addressRules" label-width="100px">
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
        <el-button @click="data.addAddressVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitAddress">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, nextTick } from "vue";
import router from "@/router";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import { Star, StarFilled, Plus } from '@element-plus/icons-vue';

// ==================== 数据定义 ====================
const formRef = ref();
const addressFormRef = ref();

const data = reactive({
  id: router.currentRoute.value.query.id,
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  goods: {},
  num: 1,
  current: '商品详情',
  commentList: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  userCollect: {},
  form: {
    deliverType: '自提',
    address: ''
  },
  formVisible: false,
  rules: {
    deliverType: [
      { required: true, message: '请选择配送类型', trigger: 'change' }
    ],
    address: [
      { required: true, message: '请选择收货地址', trigger: 'change' }
    ]
  },
  addressList: [],            // 用户地址列表
  addAddressVisible: false,   // 新增地址弹窗
  newAddress: {               // 新地址表单数据
    receiverName: '',
    receiverPhone: '',
    detailAddress: '',
    isDefault: '0'            // 默认非默认，配合el-switch
  }
});

// ==================== 校验规则（新增地址） ====================
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

// ==================== 工具方法 ====================
const formatPrice = (price) => {
  if (price === null || price === undefined || price === '') return '0.00';
  const numPrice = parseFloat(price);
  return isNaN(numPrice) ? '0.00' : numPrice.toFixed(2);
};

// 总价计算
const totalPrice = computed(() => {
  if (!data.goods.price || !data.num) return '0.00';
  const price = parseFloat(data.goods.price);
  const num = parseInt(data.num);
  if (isNaN(price) || isNaN(num)) return '0.00';
  return (price * num).toFixed(2);
});

// ==================== 数据加载 ====================
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

const loadComment = () => {
  request.get('/comment/selectPage', {
    params: { pageNum: data.pageNum, pageSize: data.pageSize, goodsId: data.id }
  }).then(res => {
    data.commentList = res.data?.list;
    data.total = res.data?.total;
  });
};

// 加载地址列表
const loadAddressList = () => {
  if (!data.user.id) return;
  request.get('/userAddress/list', {
    params: { userId: data.user.id }
  }).then(res => {
    if (res.code === '200') {
      data.addressList = res.data || [];
      // 如果还没有选择地址，自动选中默认地址
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

// 下单
const addOrder = () => {
  formRef.value.validate(valid => {
    if (!valid) return;

    const account = parseFloat(data.user.account || 0);
    const needPay = parseFloat(totalPrice.value);
    if (isNaN(account) || isNaN(needPay)) {
      ElMessage.error('价格计算错误，请刷新页面重试');
      return;
    }
    if (account < needPay) {
      ElMessage.error('账户余额不足，请充值后再购买');
      return;
    }

    data.form.userId = data.user.id;
    data.form.cartList = [{ goodsId: data.id, num: data.num }];
    data.form.totalPrice = needPay;

    request.post('/orders/add', data.form).then(res => {
      if (res.code === '200') {
        data.user.account = (account - needPay).toFixed(2);
        localStorage.setItem('system-user', JSON.stringify(data.user));
        ElMessage.success('下单成功');
        load();
        data.formVisible = false;
      } else {
        ElMessage.error(res.msg);
      }
    });
  });
};

// 加入购物车
const addCart = () => {
  request.post('cart/add', {
    goodsId: data.id, userId: data.user.id, num: data.num
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('加入购物车成功');
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 切换Tab
const changeTab = (tabName) => {
  data.current = tabName;
};

// 收藏相关
const loadCollect = () => {
  request.get('collect/selectAll', {
    params: { goodsId: data.id, userId: data.user.id }
  }).then(res => {
    if (res.data?.length > 0) {
      data.userCollect = res.data[0];
    } else {
      data.userCollect = {};
    }
  });
};
const removeCollect = () => {
  request.delete('collect/delete/' + data.userCollect.id).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功');
      loadCollect();
    } else {
      ElMessage.error(res.msg);
    }
  });
};
const addCollect = () => {
  request.post('collect/add', { goodsId: data.id, userId: data.user.id }).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功');
      loadCollect();
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 加载商品详情
const load = () => {
  request.get('/goods/selectById/' + data.id).then(res => {
    data.goods = res.data;
  });
};

// ==================== 新增地址相关 ====================
const openAddAddressDialog = () => {
  // 重置表单
  data.newAddress = {
    receiverName: '',
    receiverPhone: '',
    detailAddress: '',
    isDefault: '0'
  };
  data.addAddressVisible = true;
  nextTick(() => {
    addressFormRef.value?.clearValidate();
  });
};

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
        // 刷新地址列表
        loadAddressList();
      } else {
        ElMessage.error(res.msg || '添加失败');
      }
    });
  });
};

// ==================== 初始化 ====================
load();
loadAccount();
loadComment();
loadCollect();
</script>

<style>
.current-active {
  color: red;
  border-bottom: 1px solid red;
  padding-bottom: 10px;
}
</style>