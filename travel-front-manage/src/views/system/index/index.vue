<template>
  <div class="index">
    <div class="index1" id="index1">

    </div>
    <div class="index1" id="index2">

    </div>
    <div class="index1" id="index3">

    </div>

    <!-- 景点景气度分析柱状图 -->
    <div class="index1" id="attractionProsperityChart"></div>

    <!-- 酒店景气度分析柱状图 -->
    <div class="index1" id="hotelProsperityChart"></div>
  </div>
</template>

<script>
import {getManageData, getSysHotelOrderPage, getSysAttractionOrderPage, getSysAttractionsPage, getSysHotelPage} from '../../../api/api'
import * as echarts from "echarts";
export default {
  data() {
    return{
      myChart: null,
      myChart1: null,
      myChart2: null,
      attractionProsperityChart: null,
      hotelProsperityChart: null,
      shuju: {},
      orderList: [], // 存储酒店订单数据
      attractionOrderList: [], // 存储景点订单数据
      attractions: [], // 上架景点列表
      hotels: [], // 上架酒店列表
      attractionProsperityData: [], // 景点景气度表格数据
      hotelProsperityData: [], // 酒店景气度表格数据
      refreshInterval: null, // 定时刷新器
    }
  },
  methods: {
    // 获取上架的景点和酒店
    async getItems() {
      try {
        const attractionRes = await getSysAttractionsPage({pageNumber: 1, pageSize: 1000});
        if (attractionRes.code == 1000) {
          this.attractions = attractionRes.data.records.filter(item => item.state == 1);
        }
        const hotelRes = await getSysHotelPage({pageNumber: 1, pageSize: 1000});
        if (hotelRes.code == 1000) {
          this.hotels = hotelRes.data.records.filter(item => item.state == 1);
        }
      } catch (error) {
        console.error('获取基础数据失败：', error);
      }
    },

    // 获取所有订单数据
    async getAllOrders() {
      try {
        // 获取所有酒店订单
        const hotelOrderRes = await getSysHotelOrderPage({pageNumber: 1, pageSize: 1000});
        if (hotelOrderRes.code == 1000) {
          this.orderList = hotelOrderRes.data.records || [];
        }

        // 获取所有景点订单
        const attractionOrderRes = await getSysAttractionOrderPage({pageNumber: 1, pageSize: 1000});
        if (attractionOrderRes.code == 1000) {
          this.attractionOrderList = attractionOrderRes.data.records || [];
        }

        this.calculateProsperity();
      } catch (error) {
        console.error('获取订单数据失败：', error);
      }
    },

    // 计算景气度
    calculateProsperity() {
      if (!this.shuju.dates) return;

      // 计算景点景气度
      this.attractionProsperityData = this.attractions.map(attr => {
        const row = { name: attr.name, id: attr.id, total: 0 };
        this.shuju.dates.forEach(date => {
          const count = this.attractionOrderList.filter(order => {
            const orderDate = order.createTime ? order.createTime.split(' ')[0] : '';
            return order.name === attr.name && orderDate === date && (order.state == 1 || order.state == 5);
          }).length;
          row[date] = count;
          row.total += count;
        });
        return row;
      }).sort((a, b) => b.total - a.total); // 按总景气度排序

      // 计算酒店景气度
      this.hotelProsperityData = this.hotels.map(hotel => {
        const row = { name: hotel.name, id: hotel.id, total: 0 };
        this.shuju.dates.forEach(date => {
          const count = this.orderList.filter(order => {
            const orderDate = order.createTime ? order.createTime.split(' ')[0] : '';
            // 酒店订单可能使用 name 或其他标识，这里假设 name 匹配
            return order.name === hotel.name && orderDate === date && (order.state == 1 || order.state == 5);
          }).length;
          row[date] = count;
          row.total += count;
        });
        return row;
      }).sort((a, b) => b.total - a.total);

      this.updateProsperityCharts();
    },

    // 更新景气度柱状图
    updateProsperityCharts() {
      if (!this.shuju.dates) return;

      // 1. 处理景点景气度数据 (每日仅取最景气的)
      const topAttractionsPerDay = this.shuju.dates.map(date => {
        let maxCount = -1;
        let topName = '无订单';

        this.attractions.forEach(attr => {
          const count = this.attractionOrderList.filter(order => {
            const orderDate = order.createTime ? order.createTime.split(' ')[0] : '';
            return order.name === attr.name && orderDate === date && (order.state == 1 || order.state == 5);
          }).length;

          if (count > maxCount) {
            maxCount = count;
            topName = attr.name;
          }
        });

        return {
          date: date,
          value: maxCount > 0 ? maxCount : 0,
          name: maxCount > 0 ? topName : '无订单'
        };
      });

      const attractionDom = document.getElementById('attractionProsperityChart');
      if (attractionDom) {
        if (!this.attractionProsperityChart) {
          this.attractionProsperityChart = echarts.init(attractionDom);
        }

        this.attractionProsperityChart.setOption({
          title: {
            text: '每日最景气景点'
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'cross',
              label: {
                backgroundColor: '#6a7985'
              }
            },
            formatter: function(params) {
              const data = topAttractionsPerDay[params[0].dataIndex];
              return data.date + '<br/>' +
                  '最景气景点：' + data.name + '<br/>' +
                  '当日订单量：' + data.value;
            }
          },
          legend: {
            data: ['当日最高订单量']
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: [
            {
              type: 'category',
              boundaryGap: true,
              data: this.shuju.dates
            }
          ],
          yAxis: [
            {
              type: 'value',
              minInterval: 1
            }
          ],
          series: [
            {
              name: '当日最高订单量',
              type: 'bar',
              barWidth: 30,
              itemStyle: {
                color: '#409EFF',
                borderRadius: [5, 5, 0, 0]
              },
              label: {
                show: true,
                position: 'top',
                formatter: (params) => {
                  const data = topAttractionsPerDay[params.dataIndex];
                  return data.value > 0 ? data.name : '';
                }
              },
              data: topAttractionsPerDay.map(item => item.value)
            }
          ]
        });
      }

      // 2. 处理酒店景气度数据 (每日仅取最景气的)
      const topHotelsPerDay = this.shuju.dates.map(date => {
        let maxCount = -1;
        let topName = '无订单';

        this.hotels.forEach(hotel => {
          const count = this.orderList.filter(order => {
            const orderDate = order.createTime ? order.createTime.split(' ')[0] : '';
            return order.name === hotel.name && orderDate === date && (order.state == 1 || order.state == 5);
          }).length;

          if (count > maxCount) {
            maxCount = count;
            topName = hotel.name;
          }
        });

        return {
          date: date,
          value: maxCount > 0 ? maxCount : 0,
          name: maxCount > 0 ? topName : '无订单'
        };
      });

      const hotelDom = document.getElementById('hotelProsperityChart');
      if (hotelDom) {
        if (!this.hotelProsperityChart) {
          this.hotelProsperityChart = echarts.init(hotelDom);
        }

        this.hotelProsperityChart.setOption({
          title: {
            text: '每日最景气酒店'
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'cross',
              label: {
                backgroundColor: '#6a7985'
              }
            },
            formatter: function(params) {
              const data = topHotelsPerDay[params[0].dataIndex];
              return data.date + '<br/>' +
                  '最景气酒店：' + data.name + '<br/>' +
                  '当日订单量：' + data.value;
            }
          },
          legend: {
            data: ['当日最高订单量']
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: [
            {
              type: 'category',
              boundaryGap: true,
              data: this.shuju.dates
            }
          ],
          yAxis: [
            {
              type: 'value',
              minInterval: 1
            }
          ],
          series: [
            {
              name: '当日最高订单量',
              type: 'bar',
              barWidth: 30,
              itemStyle: {
                color: 'green',
                borderRadius: [5, 5, 0, 0]
              },
              label: {
                show: true,
                position: 'top',
                formatter: (params) => {
                  const data = topHotelsPerDay[params.dataIndex];
                  return data.value > 0 ? data.name : '';
                }
              },
              data: topHotelsPerDay.map(item => item.value)
            }
          ]
        });
      }
    },

    // 根据订单数据计算每日收入
    calculateDailyIncomeFromOrders(dates) {
      if (!this.orderList || this.orderList.length === 0 || !dates) {
        return new Array(dates?.length || 0).fill(0);
      }

      // 初始化每日收入数组
      const dailyIncome = new Array(dates.length).fill(0);

      // 遍历所有订单
      this.orderList.forEach(order => {
        // 只计算已确认（1）、已使用（5）的订单收入
        // 可以根据实际业务需求调整需要计入收入的订单状态
        if (order.state == 1 || order.state == 5) {
          // 获取订单的创建时间（createTime）
          const orderDate = order.createTime ? order.createTime.split(' ')[0] : ''; // 格式：YYYY-MM-DD

          // 在dates数组中查找对应的日期索引
          const dateIndex = dates.findIndex(date => date === orderDate);

          if (dateIndex !== -1) {
            // 将订单价格累加到对应日期的收入中
            const price = parseFloat(order.price) || 0;
            dailyIncome[dateIndex] += price;
          }
        }
      });

      return dailyIncome;
    },

    // 获取每日的订单数量
    calculateDailyOrdersCount(dates) {
      if (!this.orderList || this.orderList.length === 0 || !dates) {
        return new Array(dates?.length || 0).fill(0);
      }

      const dailyOrders = new Array(dates.length).fill(0);

      this.orderList.forEach(order => {
        if (order.state == 1 || order.state == 5) {
          const orderDate = order.createTime ? order.createTime.split(' ')[0] : '';
          const dateIndex = dates.findIndex(date => date === orderDate);

          if (dateIndex !== -1) {
            dailyOrders[dateIndex] += 1;
          }
        }
      });

      return dailyOrders;
    },

    // 获取每日的平均房价
    calculateDailyAvgPrice(dates) {
      if (!this.orderList || this.orderList.length === 0 || !dates) {
        return new Array(dates?.length || 0).fill(0);
      }

      const dailyOrders = new Array(dates.length).fill(0);
      const dailyTotalPrice = new Array(dates.length).fill(0);

      this.orderList.forEach(order => {
        if (order.state == 1 || order.state == 5) {
          const orderDate = order.createTime ? order.createTime.split(' ')[0] : '';
          const dateIndex = dates.findIndex(date => date === orderDate);

          if (dateIndex !== -1) {
            const price = parseFloat(order.price) || 0;
            dailyOrders[dateIndex] += 1;
            dailyTotalPrice[dateIndex] += price;
          }
        }
      });

      // 计算平均房价
      return dailyTotalPrice.map((total, index) => {
        if (dailyOrders[index] > 0) {
          return Math.round(total / dailyOrders[index]);
        }
        return 0;
      });
    },

    // 更新所有图表
    updateAllCharts() {
      if (!this.shuju.dates) return;

      // 从订单数据计算每日数据
      const incomeData = this.calculateDailyIncomeFromOrders(this.shuju.dates);
      const dailyOrdersCount = this.calculateDailyOrdersCount(this.shuju.dates);
      const avgPriceData = this.calculateDailyAvgPrice(this.shuju.dates);

      // 计算景气度
      this.calculateProsperity();

      // 更新第二个图表（酒店预约数）- 使用实际订单数量
      if (this.myChart1) {
        this.myChart1.setOption({
          series: [{
            data: dailyOrdersCount
          }]
        });
      }

      // 更新第三个图表（收入）
      if (this.myChart2) {
        this.myChart2.setOption({
          series: [{
            data: this.shuju.dates.map((date, index) => {
              const income = incomeData[index] || 0;
              return {
                value: income,
                income: income,
                orderCount: dailyOrdersCount[index] || 0,
                avgPrice: avgPriceData[index] || 0,
                date: date
              };
            })
          }]
        });
      }
    },

    async init() {
      try {
        // 先获取管理数据（包含dates和nums等）
        const res = await getManageData();
        if (res.code == 1000) {
          this.shuju = res.data

          // 获取上架项和订单数据
          await this.getItems();
          await this.getAllOrders();

          // 第一个图表 - 近七日景点预约
          var chartDom = document.getElementById('index1');
          this.myChart = echarts.init(chartDom);
          var option = {
            title: {
              text: '近七日景点预约'
            },
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'cross',
                label: {
                  backgroundColor: '#6a7985'
                }
              }
            },
            legend: {
              data: ['预约数']
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
              containLabel: true
            },
            xAxis: [
              {
                type: 'category',
                boundaryGap: false,
                data: this.shuju.dates
              }
            ],
            yAxis: [
              {
                type: 'value'
              }
            ],
            series: [
              {
                name: '预约数',
                type: 'line',
                stack: 'Total',
                label: {
                  show: true,
                  position: 'top'
                },
                areaStyle: {},
                emphasis: {
                  focus: 'series'
                },
                data: this.shuju.nums
              }
            ]
          }
          this.myChart.setOption(option);

          // 从订单数据计算每日数据
          const incomeData = this.calculateDailyIncomeFromOrders(this.shuju.dates);
          const dailyOrdersCount = this.calculateDailyOrdersCount(this.shuju.dates);
          const avgPriceData = this.calculateDailyAvgPrice(this.shuju.dates);

          // 第二个图表 - 近七日酒店预约（使用实际订单数据）
          var chartDom1 = document.getElementById('index2');
          this.myChart1 = echarts.init(chartDom1);
          var option1 = {
            title: {
              text: '近七日酒店预约'
            },
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'cross',
                label: {
                  backgroundColor: '#6a7985'
                }
              }
            },
            legend: {
              data: ['预约数']
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
              containLabel: true
            },
            xAxis: [
              {
                type: 'category',
                boundaryGap: false,
                data: this.shuju.dates
              }
            ],
            yAxis: [
              {
                type: 'value'
              }
            ],
            series: [
              {
                name: '预约数',
                type: 'bar',
                label: {
                  show: true,
                  position: 'top'
                },
                barWidth: 30,
                itemStyle: {
                  color: 'green'
                },
                barCategoryGap: '20%',
                data: dailyOrdersCount // 使用实际订单数量
              }
            ]
          }
          this.myChart1.setOption(option1);

          // 第三个图表 - 每日收入（基于实际订单价格）
          var chartDom2 = document.getElementById('index3');
          this.myChart2 = echarts.init(chartDom2);
          var option2 = {
            title: {
              text: '近七日酒店收入'
            },
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'cross',
                label: {
                  backgroundColor: '#6a7985'
                }
              },
              formatter: function (params) {
                const data = params[0].data;
                return params[0].name + '<br/>' +
                    '订单数：' + (data.orderCount || 0) + '单' + '<br/>' +
                    '平均房价：¥' + (data.avgPrice || 0) + '<br/>' +
                    '总收入：¥' + (data.income || 0);
              }
            },
            legend: {
              data: ['收入金额']
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
              containLabel: true
            },
            xAxis: [
              {
                type: 'category',
                boundaryGap: false,
                data: this.shuju.dates,
                axisLabel: {
                  rotate: 0,
                  interval: 0
                }
              }
            ],
            yAxis: [
              {
                type: 'value',
                name: '金额(元)',
                axisLabel: {
                  formatter: '¥{value}'
                }
              }
            ],
            series: [
              {
                name: '收入金额',
                type: 'bar', // 改为柱状图，更直观显示每日收入对比
                label: {
                  show: true,
                  position: 'top',
                  formatter: function (params) {
                    return '¥' + params.data.income;
                  }
                },
                barWidth: 40,
                itemStyle: {
                  color: function (params) {
                    // 根据收入大小设置不同的颜色深浅
                    const value = params.data.income;
                    const maxValue = Math.max(...incomeData, 1);
                    const intensity = 0.3 + (value / maxValue) * 0.7;
                    return `rgba(255, 99, 132, ${intensity})`;
                  },
                  borderRadius: [5, 5, 0, 0]
                },
                data: this.shuju.dates.map((date, index) => {
                  const income = incomeData[index] || 0;
                  return {
                    value: income,
                    income: income,
                    orderCount: dailyOrdersCount[index] || 0,
                    avgPrice: avgPriceData[index] || 0,
                    date: date
                  };
                })
              }
            ]
          }
          this.myChart2.setOption(option2);

          // 窗口大小变化时自适应
          window.addEventListener('resize', this.handleResize);

          // 启动定时刷新（可选，每5分钟刷新一次数据）
          this.startDataRefresh();
        }
      } catch (error) {
        console.error('初始化图表失败：', error);
      }
    },

    handleResize() {
      this.myChart?.resize();
      this.myChart1?.resize();
      this.myChart2?.resize();
      this.attractionProsperityChart?.resize();
      this.hotelProsperityChart?.resize();
    },

    // 启动定时刷新数据
    startDataRefresh() {
      // 每5分钟刷新一次订单数据并更新图表
      this.refreshInterval = setInterval(async () => {
        await this.getItems();
        await this.getAllOrders();
        this.updateAllCharts();
      }, 300000); // 300000毫秒 = 5分钟
    },

    // 手动刷新数据
    async refreshData() {
      await this.getItems();
      await this.getAllOrders();
      this.updateAllCharts();
      this.$message({
        type: 'success',
        message: '数据刷新成功!'
      });
    }
  },
  created() {

  },
  mounted() {
    this.init()
  },
  beforeDestroy() {
    // 组件销毁前移除事件监听
    window.removeEventListener('resize', this.handleResize);

    // 清除定时器
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }

    // 销毁图表实例
    this.myChart?.dispose();
    this.myChart1?.dispose();
    this.myChart2?.dispose();
    this.attractionProsperityChart?.dispose();
    this.hotelProsperityChart?.dispose();
  }
}
</script>

<style scoped>
.index {
  width: 100%;
  height: 100%;
  font-family: '黑体';
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.index1 {
  width: 100%;
  height: 32%; /* 从49%调整为32%，为三个图表分配空间 */
  margin-bottom: 1%;
  background-color: #ffffff;
  box-shadow: rgba(100, 100, 111, 0.2) 0px 7px 29px 0px;
}
</style>