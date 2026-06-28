<template>
  <div class="attractionsInfo">
    <headers></headers>
    <div class="attractionsInfo1">
        <div class="attractionsInfo2">
            <div class="attractionsInfo3">
                <el-carousel height="500px" v-if="info.images">
                    <el-carousel-item v-for="(item,index) in info.images.split(',')" :key="index">
                        <img style="width:100%;height:100%" :src="item">
                    </el-carousel-item>
                </el-carousel>
            </div>
            <div class="attractionsInfo4" v-if="info">
                <div class="attractionsInfo5">{{info.name || ''}}</div>
                <div class="" style="margin-top: 10px;margin-left: 20px;">所属景区：{{info.attractions || ''}}</div>
                <div class="" style="margin-top: 10px;margin-left: 20px;">地址：{{info.address || ''}}</div>
                <div class="attractionsInfo6">{{info.introduce || ''}}</div>
                <div class="attractionsInfo7" style="margin-left:10px">
                    <el-button size="small" type="primary" plain @click="toOrder">立即预定</el-button>
                </div>
            </div>
        </div>
    </div>
    <el-dialog
    title="预定"
    :visible.sync="dialogVisible"
    width="40%">
    <span>
        <div>
            <el-radio style="margin-top:10px" v-for="(item,index) in hotel" :key="index" size="small" v-model="radio1" :label="item.id" border>{{item.name}} - 数量{{item.num}} - 价格{{item.price}}</el-radio>
        </div>
        <el-input-number style="margin-top:10px" size="small" v-model="num" :min="1" :max="10"></el-input-number>
        <el-date-picker size="small" style="margin-left:20px;margin-top:10px"
        v-model="date1"
        type="date"
        value-format="yyyy-MM-dd"
        placeholder="选择预定日期">
        </el-date-picker>
        <div class="attractionsInfo9">
            <el-input size="small" v-model="people.name" placeholder="请输入姓名"></el-input>
            <el-input size="small" style="margin-left:10px" v-model="people.tel" placeholder="请输入电话"></el-input>
            <el-input size="small" style="margin-left:10px" v-model="people.idCard" placeholder="请输入身份证号"></el-input>
        </div>
    </span>
    <span slot="footer" class="dialog-footer">
        <el-button @click="closeOrder" size="small">取 消</el-button>
        <el-button type="primary" @click="saveOrder"  size="small">确 定</el-button>
    </span>
    </el-dialog>

    <!-- 支付弹窗 -->
    <el-dialog title="订单支付" :visible.sync="paymentDialogVisible" width="50%">
      <div class="payment-content">
        <div class="order-info">
          <h3>{{info.name || ''}}</h3>
          <p>预约时间：{{date1}}</p>
          <p>预约人数：{{num}}人</p>
          <p class="original-price">订单金额：¥{{orderAmount}}</p>
        </div>

        <div class="coupon-section">
          <h4>选择优惠券</h4>
          <div v-if="availableCoupons.length === 0" class="no-coupon">
            暂无可用优惠券
          </div>
          <div v-else class="coupon-list">
            <div 
              v-for="(coupon, index) in availableCoupons" 
              :key="coupon.id"
              class="coupon-item"
              :class="{ selected: selectedCouponId === coupon.id }"
              @click="selectCoupon(coupon)">
              <div class="coupon-left">
                <div class="coupon-amount">¥{{coupon.discountAmount}}</div>
                <div class="coupon-condition">满{{coupon.minAmount}}可用</div>
              </div>
              <div class="coupon-right">
                <div class="coupon-name">{{coupon.couponName}}</div>
                <div class="coupon-expire">有效期至：{{formatTime(coupon.expireTime)}}</div>
              </div>
            </div>
            <div 
              class="coupon-item"
              :class="{ selected: selectedCouponId === null }"
              @click="selectCoupon(null)">
              <div class="coupon-left">
                <div class="coupon-amount">不使用</div>
              </div>
              <div class="coupon-right">
                <div class="coupon-name">不使用优惠券</div>
              </div>
            </div>
          </div>
        </div>

        <div class="final-price">
          <span>优惠金额：</span>
          <span class="discount">-¥{{discountAmount}}</span>
        </div>
        <div class="final-price">
          <span>实付金额：</span>
          <span class="price">¥{{finalAmount}}</span>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="closePaymentDialog">取消</el-button>
        <el-button type="primary" @click="confirmPayment">确认支付</el-button>
      </span>
    </el-dialog>

    <bottoms></bottoms>
  </div>
</template>

<script>
  import {getSysHotelById,getSysHotelItemList,saveSysHotelOrder,getMyCoupons,useCoupon} from '../../api/api'
  import headers from '@/components/header'
  import bottoms from '@/components/bottom'
  export default {
    data() {
      return{
        id: "",
        dialogVisible: false,
        content: "",
        date1: "",
        num: 1,
        people:{
            name: "",
            tel: "",
            idCard: ""
        },
        info: {},
        hotel: [],
        radio1: "",
        paymentDialogVisible: false,
        currentOrderId: "",
        orderAmount: 0,
        availableCoupons: [],
        selectedCouponId: null,
        discountAmount: 0,
        finalAmount: 0
      }
    },
    components: {
      headers,
      bottoms
    },
    methods: {
      getSysHotelItemList() {
        getSysHotelItemList({id:this.id}).then(res => {
          if (res.code == 1000) {
            this.hotel = res.data
          }
        })
      },
      closeOrder() {
        this.date1 = ""
        this.num = 1
        this.radio1 = ''
        this.people = 
        {
          name: "",
          tel: "",
          idCard: ""
        }
        this.dialogVisible = false
      },
      saveOrder() {
          if (!this.date1) {
            this.$message({
                message: '请选择预约时间',
                type: 'warning'
            });
            return
          }
          if (!this.people.name) {
              this.$message({
                  message: '请完善预约人姓名',
                  type: 'warning'
              });
              return
          }
          if (!this.people.tel) {
              this.$message({
                  message: '请完善预约人联系方式',
                  type: 'warning'
              });
              return
          }
          if (!this.people.idCard ) {
              this.$message({
                  message: '请完善预约人证件号',
                  type: 'warning'
              });
              return
          }
          if (!this.radio1 ) {
              this.$message({
                  message: '请选择房型',
                  type: 'warning'
              });
              return
          }
          var param = {
            hotelId: this.id,
            num: this.num,
            itemId: this.radio1,
            time: this.date1,
            people: JSON.stringify(this.people)
          }
          saveSysHotelOrder(param).then(res => {
              if (res.code == 1000) {
                this.currentOrderId = res.data?.id || res.data
                const selectedItem = this.hotel.find(item => item.id === this.radio1)
                this.orderAmount = (selectedItem?.price || 0) * this.num
                this.closeOrder()
                this.openPaymentDialog()
              } else {
                this.$message({
                    message: res.message,
                    type: 'warning'
                });
              }
          }).catch(err => {
              console.error('预约失败:', err)
              this.$message({
                  message: '预约失败，请稍后重试',
                  type: 'error'
              });
          })
      },
      getSysHotelById() {
        getSysHotelById({id:this.id}).then(res => {
          if (res.code == 1000) {
            this.info = res.data || {}
          }
        }).catch(err => {
          console.error('获取酒店信息失败:', err)
          this.$message({
            message: '获取酒店信息失败',
            type: 'error'
          })
        })
      },
      toOrder() {
        this.dialogVisible = true
      },
      openPaymentDialog() {
        this.paymentDialogVisible = true
        this.loadAvailableCoupons()
      },
      closePaymentDialog() {
        this.paymentDialogVisible = false
        this.currentOrderId = ""
        this.selectedCouponId = null
        this.discountAmount = 0
        this.finalAmount = 0
        this.availableCoupons = []
      },
      loadAvailableCoupons() {
        const userInfo = window.localStorage.getItem("user_info")
        if (!userInfo) return
        
        const user = JSON.parse(userInfo)
        getMyCoupons({ userId: user.id, status: 0 }).then(res => {
          if (res.code == 1000) {
            this.availableCoupons = res.data.filter(coupon => 
              coupon.minAmount <= this.orderAmount
            )
            if (this.availableCoupons.length > 0) {
              this.selectCoupon(this.availableCoupons[0])
            } else {
              this.selectCoupon(null)
            }
          }
        })
      },
      selectCoupon(coupon) {
        if (coupon) {
          this.selectedCouponId = coupon.id
          this.discountAmount = coupon.discountAmount
        } else {
          this.selectedCouponId = null
          this.discountAmount = 0
        }
        this.finalAmount = Math.max(0, this.orderAmount - this.discountAmount)
      },
      confirmPayment() {
        if (this.selectedCouponId) {
          useCoupon({
            couponUserId: this.selectedCouponId,
            orderId: this.currentOrderId,
            orderType: 1
          }).then(res => {
            if (res.code == 1000) {
              this.$message.success('支付成功，优惠券已使用')
              this.closePaymentDialog()
            } else {
              this.$message.error(res.message)
            }
          })
        } else {
          this.$message.success('支付成功')
          this.closePaymentDialog()
        }
      },
      formatTime(time) {
        if (!time) return ''
        const date = new Date(time)
        const y = date.getFullYear()
        const m = String(date.getMonth() + 1).padStart(2, '0')
        const d = String(date.getDate()).padStart(2, '0')
        return `${y}-${m}-${d}`
      }
    },
    created() {
     
    },
    mounted() {
      this.id = this.$route.query.id
      this.getSysHotelById()
      this.getSysHotelItemList()
    }
 }
</script>

<style scoped>
   @import url('../../assets/css/attractionsInfo.css');

.payment-content {
    padding: 10px 0;
}

.order-info {
    background: #f5f7fa;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
}

.order-info h3 {
    margin: 0 0 15px 0;
    color: #303133;
    font-size: 18px;
}

.order-info p {
    margin: 8px 0;
    color: #606266;
}

.original-price {
    font-size: 18px;
    font-weight: bold;
    color: #303133;
    margin-top: 15px !important;
}

.coupon-section {
    margin-bottom: 20px;
}

.coupon-section h4 {
    margin: 0 0 15px 0;
    color: #303133;
    font-size: 16px;
}

.no-coupon {
    text-align: center;
    color: #909399;
    padding: 30px;
    background: #f5f7fa;
    border-radius: 8px;
}

.coupon-list {
    max-height: 300px;
    overflow-y: auto;
}

.coupon-item {
    display: flex;
    border: 2px solid #e8e8e8;
    border-radius: 12px;
    margin-bottom: 12px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s;
}

.coupon-item:hover {
    border-color: #409eff;
}

.coupon-item.selected {
    border-color: #409eff;
    background: #ecf5ff;
}

.coupon-left {
    background: linear-gradient(135deg, #ff6b6b, #ff8e53);
    padding: 20px 15px;
    min-width: 120px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: white;
}

.coupon-amount {
    font-size: 28px;
    font-weight: bold;
}

.coupon-condition {
    font-size: 12px;
    margin-top: 5px;
    opacity: 0.9;
}

.coupon-right {
    padding: 15px 20px;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.coupon-name {
    font-size: 16px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 8px;
}

.coupon-expire {
    font-size: 12px;
    color: #909399;
}

.final-price {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 0;
    border-top: 1px solid #e8e8e8;
    font-size: 16px;
    color: #606266;
}

.final-price .discount {
    color: #ff6b6b;
    font-weight: bold;
    font-size: 20px;
}

.final-price .price {
    color: #ff6b6b;
    font-weight: bold;
    font-size: 24px;
}
</style>
