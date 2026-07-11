<template>
  <div>
    <!-- 统计卡片 -->
    <div style="display: flex; grid-gap: 10px">
      <div class="card" style="padding: 20px; flex: 1; display: flex">
        <div style="flex: 1; font-size: 20px">销售总额</div>
        <div style="flex: 1; font-size: 20px; font-weight: bold; color: red">￥{{ data.count.total }}</div>
      </div>
      <div class="card" style="padding: 20px; flex: 1; display: flex">
        <div style="flex: 1; font-size: 20px">今日销售额</div>
        <div style="flex: 1; font-size: 20px; font-weight: bold; color: #55ff00">￥{{ data.count.today }}</div>
      </div>
      <div class="card" style="padding: 20px; flex: 1; display: flex">
        <div style="flex: 1; font-size: 20px">商品总数</div>
        <div style="flex: 1; font-size: 20px; font-weight: bold; color: #00ffd9">{{ data.count.goods }}</div>
      </div>
      <div class="card" style="padding: 20px; flex: 1; display: flex">
        <div style="flex: 1; font-size: 20px">注册用户</div>
        <div style="flex: 1; font-size: 20px; font-weight: bold; color: #590aba">{{ data.count.user }}</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div style="margin-top: 10px; display: flex; grid-gap: 10px">
      <div id="line" style="flex: 1; padding: 20px; height: 500px;" class="card"></div>
      <div id="pie" style="flex: 1; padding: 20px; height: 500px" class="card"></div>
    </div>

    <!-- 缺货商品提醒 -->
    <div style="margin-top: 10px">
      <div class="card" style="padding: 20px">
        <h3 style="margin: 0 0 20px 0">缺货商品提醒</h3>

        <el-table :data="outOfStockGoods" stripe style="width: 100%">
          <el-table-column label="商品名称" prop="name"></el-table-column>
          <el-table-column label="当前库存" width="150">
            <template #default="scope">
              <span style="color: #f56c6c; font-weight: bold; font-size: 18px">
                {{ scope.row.store }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="150">
            <template #default="scope">
              <el-tag v-if="scope.row.store <= 0" type="danger">缺货</el-tag>
              <el-tag v-else-if="scope.row.store <= 10" type="warning">库存预警</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="outOfStockGoods.length === 0" style="text-align: center; padding: 40px; color: #67c23a">
          暂无缺货商品，所有商品库存充足
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, ref } from "vue";
import request from "@/utils/request";
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const data = reactive({
  count: {}
})

const outOfStockGoods = ref([])

const lineOption = {
  title: {
    text: '近一周的订单销售趋势图',
    subtext: '趋势图（基于实付金额）',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis',
    formatter: function(params) {
      return params[0].axisValue + '<br/>销售额：¥' + params[0].value.toFixed(2);
    }
  },
  legend: {
    left: 'left'
  },
  xAxis: {
    name: '日期',
    type: 'category',
    data: []
  },
  yAxis: {
    name: '销售额（元）',
    type: 'value'
  },
  grid: {
    top: '20%',
    bottom:'10%'
  },
  series: [
    {
      name: '销售额',
      data: [],
      type: 'line',
      smooth: true,
      areaStyle: {
        opacity: 0.8,
        color: 'rgb(185,190,255)'
      },
      markPoint: {
        data: [
          { type: 'max', name: '最高销售额' },
          { type: 'min', name: '最低销售额' }
        ]
      },
      markLine: {
        data: [{ type: 'average', name: '平均销售额' }]
      }
    },
  ]
}

const pieOption = {
  title: {
    text: '订单销售统计',
    subtext: '比例图（基于实付金额）',
    left: 'center'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b} : ¥{c} ({d}%)'
  },
  legend: {
    top: 0,
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: '销售额',
      type: 'pie',
      center: ['50%', '60%'],
      radius: '50%',
      data: [],
      label: {
        show: true,
        formatter(param) {
          return param.name + ' (' + param.percent + '%)';
        }
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
}

request.get('/count').then(res =>{
  data.count = res.data
})

// 加载缺货商品
const loadOutOfStockGoods = () => {
  request.get('/goods/selectAll').then(res => {
    if (res.code === '200') {
      const allGoods = res.data || [];

      // 1. 先过滤出所有库存 ≤10 的商品，并按库存升序排列
      const allFiltered = allGoods
          .filter(item => item.store <= 10)
          .sort((a, b) => a.store - b.store);

      // 2. 表格只展示前5条
      outOfStockGoods.value = allFiltered.slice(0, 5);

      // 3. 提醒仍基于完整的缺货/预警数量
      if (allFiltered.length > 0) {
        const outOfStock = allFiltered.filter(item => item.store <= 0).length;
        if (outOfStock > 0) {
          ElMessage.warning(`有 ${outOfStock} 个商品已缺货，请及时补货！`);
        } else {
          ElMessage.warning(`有 ${allFiltered.length} 个商品库存不足10件`);
        }
      }
    }
  });
};

// 等页面所有元素加载完成后再设置 echarts图表
onMounted(() => {
  // 折线图
  let lineDom = document.getElementById('line')
  let lineChart = echarts.init(lineDom)
  // 请求数据  初始化图表
  request.get('/selectLine').then(res =>{
    lineOption.xAxis.data = res.data.date
    lineOption.series[0].data = res.data.count
    lineChart.setOption(lineOption)
  })

  // 饼图
  let pieDom = document.getElementById('pie')
  let pieChart = echarts.init(pieDom)
  // 请求数据  初始化图表
  request.get('/selectPie').then(res =>{
    pieOption.series[0].data = res.data
    pieChart.setOption(pieOption)
  })

  // 加载缺货商品
  loadOutOfStockGoods()
})
</script>