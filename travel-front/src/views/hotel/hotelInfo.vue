<template>
  <PageLayout>
  <div class="attractionsInfo">
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
                <div class="attractionsInfo7">
                    <el-button type="primary" icon="el-icon-s-order" @click="toOrder">立即预定</el-button>
                </div>
            </div>
        </div>
    </div>
    <el-dialog
      title="预定"
      :visible.sync="dialogVisible"
      width="520px"
      custom-class="hotel-book-dialog"
      :close-on-click-modal="false">
      <div class="book-form">
        <div class="book-section">
          <div class="book-section__title">选择房型</div>
          <div v-if="hotel.length" class="room-list">
            <div
              v-for="(item, index) in hotel"
              :key="index"
              class="room-card"
              :class="{ active: radio1 === item.id }"
              @click="radio1 = item.id">
              <div class="room-card__main">
                <span class="room-card__radio" :class="{ checked: radio1 === item.id }"></span>
                <span class="room-card__name">{{ item.name }}</span>
              </div>
              <div class="room-card__meta">
                <span>库存 {{ item.num }} 间</span>
                <span class="room-card__price">¥{{ item.price }}/晚</span>
              </div>
            </div>
          </div>
          <div v-else class="book-empty">暂无可预订房型</div>
        </div>

        <div class="book-section">
          <div class="book-section__title">预订信息</div>
          <div class="book-row">
            <div class="book-field">
              <label>房间数量</label>
              <el-input-number
                v-model="num"
                :min="1"
                :max="maxBookNum"
                size="small"
                controls-position="right">
              </el-input-number>
            </div>
            <div class="book-field book-field--grow">
              <label>入住日期</label>
              <el-date-picker
                v-model="date1"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="选择预定日期"
                size="small"
                style="width: 100%">
              </el-date-picker>
            </div>
          </div>
        </div>

        <div class="book-section">
          <div class="book-section__title">入住人信息</div>
          <div class="guest-fields">
            <el-input v-model="people.name" size="small" prefix-icon="el-icon-user" placeholder="请输入姓名"></el-input>
            <el-input v-model="people.tel" size="small" prefix-icon="el-icon-phone" placeholder="请输入电话"></el-input>
            <el-input v-model="people.idCard" size="small" prefix-icon="el-icon-postcard" placeholder="请输入身份证号"></el-input>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="closeOrder">取 消</el-button>
        <el-button type="primary" @click="saveOrder">确 定</el-button>
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
  </div>
  </PageLayout>
</template>

<script>
  import {getSysHotelById,getSysHotelItemList,saveSysHotelOrder,getMyCoupons,useCoupon} from '../../api/api'
  import { showPointsEarned, extractPointsEarned, extractOrderFromRes } from '@/utils/pointsToast'
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
    components: {},
    computed: {
      maxBookNum() {
        const selected = this.hotel.find(item => item.id === this.radio1)
        return selected ? Math.min(selected.num, 10) : 10
      }
    },
    watch: {
      radio1() {
        if (this.num > this.maxBookNum) {
          this.num = this.maxBookNum || 1
        }
      }
    },
    methods: {
      getSysHotelItemList() {
        getSysHotelItemList({id:this.id}).then(res => {
          if (res.code == 1000) {
            this.hotel = res.data
            if (this.hotel.length && !this.radio1) {
              this.radio1 = this.hotel[0].id
            }
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
                showPointsEarned(extractPointsEarned(res), '预约酒店奖励')
                const order = extractOrderFromRes(res)
                this.currentOrderId = order?.id || res.data?.id
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
        if (!this.hotel.length) {
          this.$message.warning('暂无可预订房型')
          return
        }
        if (!this.radio1) {
          this.radio1 = this.hotel[0].id
        }
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

.book-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.book-section__title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.room-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.room-card {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 12px 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.room-card:hover {
  border-color: #409eff;
}

.room-card.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.room-card__main {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.room-card__radio {
  width: 16px;
  height: 16px;
  border: 2px solid #dcdfe6;
  border-radius: 50%;
  flex-shrink: 0;
  position: relative;
}

.room-card__radio.checked {
  border-color: #409eff;
}

.room-card__radio.checked::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 8px;
  height: 8px;
  background: #409eff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
}

.room-card__name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.room-card__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-left: 26px;
  font-size: 13px;
  color: #909399;
}

.room-card__price {
  color: #f56c6c;
  font-weight: 600;
}

.book-empty {
  text-align: center;
  color: #909399;
  padding: 24px;
  background: #f5f7fa;
  border-radius: 8px;
}

.book-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
}

.book-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.book-field label {
  font-size: 13px;
  color: #606266;
}

.book-field--grow {
  flex: 1;
}

.guest-fields {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

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
