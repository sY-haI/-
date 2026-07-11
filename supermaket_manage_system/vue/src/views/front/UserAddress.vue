<template>
  <div class="front-container">

    <!-- 搜索与操作行 -->
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.keyword" style="width: 300px; margin-right: 10px" placeholder="输入收货人或电话搜索"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
      <el-button type="success" @click="handleAdd" style="margin-left: 20px;">
        <el-icon><Plus /></el-icon> 新增收货地址
      </el-button>
    </div>

    <!-- 表格 -->
    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe>
        <el-table-column prop="receiverName" label="收货人" width="100"></el-table-column>
        <el-table-column prop="receiverPhone" label="联系电话" width="140"></el-table-column>
        <el-table-column prop="detailAddress" label="详细地址" show-overflow-tooltip></el-table-column>
        <el-table-column label="默认地址" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isDefault === 1" type="success">默认</el-tag>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" align="center" width="220">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleSetDefault(scope.row.id)" v-if="scope.row.isDefault !== 1">设为默认</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="card">
      <el-pagination @current-change="load" background layout="total, prev, pager, next"
                     v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="data.form.id ? '编辑收货地址' : '新增收货地址'" width="450px" v-model="data.formVisible"
               :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="100px" style="padding-right: 30px; padding-top: 20px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="data.form.receiverName" placeholder="请输入收货人姓名"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="receiverPhone">
          <el-input v-model="data.form.receiverPhone" placeholder="请输入手机号码"></el-input>
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input type="textarea" :rows="2" v-model="data.form.detailAddress" placeholder="街道、小区、门牌号等"></el-input>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="data.form.isDefault" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref, nextTick } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from '@element-plus/icons-vue';

const formRef = ref();

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  keyword: '',
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  formVisible: false,
  form: {
    id: null,
    receiverName: '',
    receiverPhone: '',
    detailAddress: '',
    isDefault: 0
  },
  rules: {
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
  }
});

// 加载数据
const load = () => {
  if (!data.user.id) {
    ElMessage.error('用户信息丢失，请重新登录');
    return;
  }
  request.get('/userAddress/selectPage', {
    params: {
      userId: data.user.id,
      keyword: data.keyword,
      pageNum: data.pageNum,
      pageSize: data.pageSize
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || [];
      data.total = res.data?.total || 0;
    } else {
      ElMessage.error(res.msg);
    }
  }).catch(err => console.error(err));
};

// 新增
const handleAdd = () => {
  data.form = {
    id: null,
    receiverName: '',
    receiverPhone: '',
    detailAddress: '',
    isDefault: 0
  };
  data.formVisible = true;
  nextTick(() => formRef.value?.clearValidate());
};

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row));
  data.formVisible = true;
  nextTick(() => formRef.value?.clearValidate());
};

// 保存（新增或更新）
const save = () => {
  formRef.value.validate(valid => {
    if (!valid) return;

    const params = {
      userId: data.user.id,
      receiverName: data.form.receiverName,
      receiverPhone: data.form.receiverPhone,
      detailAddress: data.form.detailAddress,
      isDefault: data.form.isDefault
    };

    if (data.form.id) {
      params.id = data.form.id;
      request.put('/userAddress/update', params).then(res => {
        if (res.code === '200') {
          ElMessage.success('更新成功');
          data.formVisible = false;
          load();
        } else {
          ElMessage.error(res.msg);
        }
      });
    } else {
      request.post('/userAddress/add', params).then(res => {
        if (res.code === '200') {
          ElMessage.success('新增成功');
          data.formVisible = false;
          load();
        } else {
          ElMessage.error(res.msg);
        }
      });
    }
  });
};

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(() => {
    request.delete('/userAddress/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功');
        load();
      } else {
        ElMessage.error(res.msg);
      }
    });
  }).catch(() => {});
};

// 设为默认
const handleSetDefault = (id) => {
  ElMessageBox.confirm('设为默认地址后，其他地址将取消默认，确定继续吗？', '确认操作', { type: 'info' }).then(() => {
    request.put('/userAddress/setDefault/' + id, null, {
      params: { userId: data.user.id }
    }).then(res => {
      if (res.code === '200') {
        ElMessage.success('设置成功');
        load();
      } else {
        ElMessage.error(res.msg);
      }
    });
  }).catch(() => {});
};

// 重置搜索
const reset = () => {
  data.keyword = '';
  data.pageNum = 1;
  load();
};

// 初始化
load();
</script>

<style scoped>
.front-container {
  padding: 20px;
}
.card {
  background: #fff;
  padding: 20px;
  border-radius: 5px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
</style>