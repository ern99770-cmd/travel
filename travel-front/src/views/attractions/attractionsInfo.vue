<template>
    <PageLayout>
    <div class="attractionsInfo">
        <div class="attractionsInfo1">
            <div class="attractionsInfo2">
                <div class="attractionsInfo3">
                    <el-carousel height="500px" v-if="info.images">
                        <el-carousel-item v-for="(item, index) in info.images.split(',')" :key="index">
                            <img style="width:100%;height:100%" :src="item">
                        </el-carousel-item>
                    </el-carousel>
                </div>
                <div class="attractionsInfo4" v-if="info">
                    <div class="attractionsInfo5">{{ info.name || '' }}</div>
                    <div class="attractionsInfo5">价格：{{ info.price || 0 }}（元）</div>
                    <div style="margin-left:20px;margin-top:10px">库存：{{ info.num || 0 }} -{{ info.realName == 0 ? '非实名' : '实名' }}</div>
                    <div class="attractionsInfo6">{{ info.introduce || '' }}</div>
                    <div class="attractionsInfo7">
                        <el-button type="primary" icon="el-icon-tickets" @click="toOrder">立即预约</el-button>
                    </div>
                </div>
            </div>
        </div>
        <div class="attractionsInfo1" v-if="info">
            <div class="attractionsInfo8">
                <div style="margin-left:20px;margin-right:20px;margin-top:20px">
                    介绍：{{ info.content || '' }}
                </div>
                <div style="margin-left:20px;margin-right:20px;margin-top:20px">
                    预约须知：{{ info.open || '' }}
                </div>
                <div style="margin-left:20px;margin-right:20px;margin-top:20px;margin-bottom:20px">
                    入园时间：{{ info.time || '' }}
                </div>
            </div>
        </div>
        <div class="attractionsInfo1">
            <div class="attractionsInfo8">
                <el-input style="margin-top:20px" v-model="content" type="textarea" rows="7"
                    placeholder="请输入评论内容"></el-input>
                <el-button style="margin-top:20px" type="primary" size="small" plain
                    @click="saveSysComments">评论</el-button>
                <div class="forum1" style="width:100%">
                    <div class="forum2" style="padding:0" v-for="(item, index) in tableData" :key="index">
                        <img style="border-radius:50%;width:40px;height:40px;margin-left:20px"
                            :src="$store.state.HOST + item.avatar">
                        <div style="margin-left:10px">
                            {{ item.createBy }}
                        </div>
                        <div style="margin-left:20px">
                            {{ item.content }}
                        </div>
                    </div>
                </div>
                <el-pagination background :page-size="search.pageSize" layout="prev, pager, next"
                    @current-change="handleCurrentChange" :total="total">
                </el-pagination>
            </div>
        </div>
        <el-dialog
            title="预约"
            :visible.sync="dialogVisible"
            width="520px"
            custom-class="attraction-book-dialog"
            :close-on-click-modal="false">
            <div class="book-form">
                <div class="book-section">
                    <div class="book-section__title">景点信息</div>
                    <div class="attraction-summary">
                        <div class="attraction-summary__name">{{ info.name || '' }}</div>
                        <div class="attraction-summary__meta">
                            <span class="attraction-summary__price">¥{{ info.price || 0 }}/人</span>
                            <span>库存 {{ info.num || 0 }} 张</span>
                            <span>{{ info.realName == 1 ? '实名预约' : '非实名' }}</span>
                        </div>
                    </div>
                </div>

                <div class="book-section">
                    <div class="book-section__title">预约信息</div>
                    <div class="book-row">
                        <div class="book-field">
                            <label>预约人数</label>
                            <el-input-number
                                v-model="num"
                                :min="1"
                                :max="maxBookNum"
                                size="small"
                                controls-position="right">
                            </el-input-number>
                        </div>
                        <div class="book-field book-field--grow">
                            <label>入园日期</label>
                            <el-date-picker
                                v-model="date1"
                                type="date"
                                value-format="yyyy-MM-dd"
                                placeholder="选择预约日期"
                                size="small"
                                style="width: 100%">
                            </el-date-picker>
                        </div>
                    </div>
                </div>

                <div class="book-section">
                    <div class="book-section__title">预约人信息</div>
                    <div class="guest-list">
                        <div v-for="(item, index) in people" :key="index" class="guest-card">
                            <div class="guest-card__title">预约人 {{ index + 1 }}</div>
                            <div class="guest-fields">
                                <el-input v-model="item.name" size="small" prefix-icon="el-icon-user" placeholder="请输入姓名"></el-input>
                                <el-input v-model="item.tel" size="small" prefix-icon="el-icon-phone" placeholder="请输入电话"></el-input>
                                <el-input
                                    v-if="info.realName == 1"
                                    v-model="item.idCard"
                                    size="small"
                                    prefix-icon="el-icon-postcard"
                                    placeholder="请输入身份证号">
                                </el-input>
                            </div>
                        </div>
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
                    <h3>{{ info.name }}</h3>
                    <p>预约时间：{{ date1 }}</p>
                    <p>预约人数：{{ num }}人</p>
                    <p class="original-price">订单金额：¥{{ orderAmount }}</p>
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
                            @click="selectCoupon(coupon)"
                        >
                            <div class="coupon-left">
                                <div class="coupon-amount">¥{{ coupon.discountAmount }}</div>
                                <div class="coupon-condition">满{{ coupon.minAmount }}可用</div>
                            </div>
                            <div class="coupon-right">
                                <div class="coupon-name">{{ coupon.couponName }}</div>
                                <div class="coupon-expire">有效期至：{{ formatTime(coupon.expireTime) }}</div>
                            </div>
                        </div>
                        <div 
                            class="coupon-item"
                            :class="{ selected: selectedCouponId === null }"
                            @click="selectCoupon(null)"
                        >
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
                    <span class="discount">-¥{{ discountAmount }}</span>
                </div>
                <div class="final-price">
                    <span>实付金额：</span>
                    <span class="price">¥{{ finalAmount }}</span>
                </div>
            </div>
            <span slot="footer" class="dialog-footer">
                <el-button @click="closePaymentDialog">取消</el-button>
                <el-button type="primary" @click="confirmPayment">确认支付</el-button>
            </span>
        </el-dialog>
        <!-- 附近酒店推荐 -->
        <div v-if="nearbyHotels.length > 0" class="attractionsInfo1">
            <div class="attractionsInfo8">
                <div class="nearby-hotels-title">
                    <i class="el-icon-office-building"></i>
                    <span>附近酒店推荐</span>
                </div>
                <div class="nearby-hotels-list">
                    <div class="hotel-item" v-for="(item, index) in nearbyHotels" :key="index" @click="toHotelInfo(item.id)">
                        <img :src="item.images ? item.images.split(',')[0] : ''" class="hotel-img">
                        <div class="hotel-info">
                            <div class="hotel-name">{{ item.name || '' }}</div>
                            <div class="hotel-intro">{{ item.introduce || '' }}</div>
                            <div class="hotel-address">
                                <i class="el-icon-location-outline"></i> {{ item.address || '' }}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    </PageLayout>
</template>

<script>
import { getSysAttractionsById, saveSysAttractionOrder, getSysCommentsPage, saveSysComments, getSysHotelPage, getMyCoupons, useCoupon } from '../../api/api'
import { showPointsEarned, extractPointsEarned, extractOrderFromRes } from '@/utils/pointsToast'
export default {
    data() {
        return {
            id: "",
            dialogVisible: false,
            search: {
                attractionsId: "",
                pageSize: 10,
                pageNumber: 1,
            },
            info: {},
            total: 100,
            tableData: [],
            content: "",
        num: 1,
        date1: "",
        people: [
            {
                name: "",
                tel: "",
                idCard: ""
            }
        ],
        nearbyHotels: [],
        // 支付相关
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
            return Math.min(this.info.num || 10, 10)
        }
    },
    watch: {
        num(newVal) {
            while (this.people.length < newVal) {
                this.people.push({ name: '', tel: '', idCard: '' })
            }
            while (this.people.length > newVal) {
                this.people.pop()
            }
        }
    },
    methods: {
        getNearbyHotels() {
            // 获取该景点附近的酒店
            if (this.info.name) {
                getSysHotelPage({ attractions: this.info.name, pageSize: 6, pageNumber: 1, state: 1 }).then(res => {
                    if (res.code == 1000) {
                        this.nearbyHotels = res.data.records
                    }
                })
            }
        },
        toHotelInfo(id) {
            this.$router.push("/hotelInfo?id=" + id)
        },
        saveSysComments() {
            if (!this.content) {
                this.$message({
                    message: '请输入评论内容',
                    type: 'warning'
                });
                return
            }
            var param = {
                content: this.content,
                attractionsId: this.id
            }
            saveSysComments(param).then(res => {
                if (res.code == 1000) {
                    showPointsEarned(extractPointsEarned(res), '评论奖励')
                    this.$message({
                        message: '评论成功',
                        type: 'success'
                    });
                    this.content = ""
                    this.getSysCommentsPage()
                }
            })
        },
        getSysCommentsPage() {
            this.search.attractionsId = this.id
            getSysCommentsPage(this.search).then(res => {
                if (res.code == 1000) {
                    this.tableData = res.data.records
                    this.total = res.data.total
                }
            })
        },
        closeOrder() {
            this.date1 = ""
            this.num = 1
            this.people = [
                {
                    name: "",
                    tel: "",
                    idCard: ""
                }
            ],
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
            if (this.people.length < this.num) {
                this.$message({
                    message: '请完善预约人信息',
                    type: 'warning'
                });
                return
            }
            if (this.people.length > this.num) {
                this.$message({
                    message: '预约人信息超出预约人数',
                    type: 'warning'
                });
                return
            }
            for (let i = 0; i < this.people.length; i++) {
                var item = this.people[i]
                if (!item.name) {
                    this.$message({
                        message: '请完善预约人姓名',
                        type: 'warning'
                    });
                    return
                }
                if (!item.tel) {
                    this.$message({
                        message: '请完善预约人联系方式',
                        type: 'warning'
                    });
                    return
                }
                if (this.info.realName == 1 && !item.idCard) {
                    this.$message({
                        message: '请完善预约人证件号',
                        type: 'warning'
                    });
                    return
                }
            }
            var param = {
                attractionsId: this.id,
                num: this.num,
                time: this.date1,
                people: JSON.stringify(this.people)
            }
            saveSysAttractionOrder(param).then(res => {
                if (res.code == 1000) {
                    showPointsEarned(extractPointsEarned(res), '预约景点奖励')
                    const order = extractOrderFromRes(res)
                    this.currentOrderId = order?.id || res.data?.id
                    this.orderAmount = (this.info?.price || 0) * this.num
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
        getSysAttractionsById() {
            getSysAttractionsById({ id: this.id }).then(res => {
                if (res.code == 1000) {
                    this.info = res.data || {}
                    // 获取景点信息后，再获取附近酒店
                    this.getNearbyHotels()
                }
            }).catch(err => {
                console.error('获取景点信息失败:', err)
                this.$message({
                    message: '获取景点信息失败',
                    type: 'error'
                })
            })
        },
        toOrder() {
            if (!this.info.num || this.info.num <= 0) {
                this.$message.warning('暂无库存')
                return
            }
            this.dialogVisible = true
        },
        handleCurrentChange() {

        },
        // 打开支付弹窗
        openPaymentDialog() {
            this.paymentDialogVisible = true
            this.loadAvailableCoupons()
        },
        // 关闭支付弹窗
        closePaymentDialog() {
            this.paymentDialogVisible = false
            this.currentOrderId = ""
            this.selectedCouponId = null
            this.discountAmount = 0
            this.finalAmount = 0
            this.availableCoupons = []
        },
        // 加载可用优惠券
        loadAvailableCoupons() {
            const userInfo = window.localStorage.getItem("user_info")
            if (!userInfo) return
            
            const user = JSON.parse(userInfo)
            getMyCoupons({ userId: user.id, status: 0 }).then(res => {
                if (res.code == 1000) {
                    // 筛选出满足金额条件的优惠券
                    this.availableCoupons = res.data.filter(coupon => 
                        coupon.minAmount <= this.orderAmount
                    )
                    // 默认选择第一个
                    if (this.availableCoupons.length > 0) {
                        this.selectCoupon(this.availableCoupons[0])
                    } else {
                        this.selectCoupon(null)
                    }
                }
            })
        },
        // 选择优惠券
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
        // 确认支付
        confirmPayment() {
            if (this.selectedCouponId) {
                // 使用优惠券
                useCoupon({
                    couponUserId: this.selectedCouponId,
                    orderId: this.currentOrderId,
                    orderType: 2 // 2表示景点订单
                }).then(res => {
                    if (res.code == 1000) {
                        this.$message.success('支付成功，优惠券已使用')
                        this.closePaymentDialog()
                    } else {
                        this.$message.error(res.message)
                    }
                })
            } else {
                // 不使用优惠券，直接支付
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
        this.getSysAttractionsById()
        this.getSysCommentsPage()
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

.attraction-summary {
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    padding: 14px 16px;
    background: #f5f7fa;
}

.attraction-summary__name {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
}

.attraction-summary__meta {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    font-size: 13px;
    color: #909399;
}

.attraction-summary__price {
    color: #f56c6c;
    font-weight: 600;
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

.guest-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    max-height: 280px;
    overflow-y: auto;
}

.guest-card {
    border: 1px solid #ebeef5;
    border-radius: 8px;
    padding: 12px 14px;
    background: #fafafa;
}

.guest-card__title {
    font-size: 13px;
    font-weight: 600;
    color: #606266;
    margin-bottom: 10px;
}

.guest-fields {
    display: flex;
    flex-direction: column;
    gap: 10px;
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

.nearby-hotels-title {
    font-size: 20px;
    font-weight: 700;
    color: #303133;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 10px;
    border-bottom: 2px solid #f0f2f5;
}

.nearby-hotels-title i {
    color: #fa8c16;
    font-size: 24px;
}

.nearby-hotels-list {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    padding: 20px;
}

.hotel-item {
    width: calc(33.333% - 14px);
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0,0,0,0.1);
    cursor: pointer;
    transition: all 0.3s;
}

.hotel-item:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}

.hotel-img {
    width: 100%;
    height: 200px;
    object-fit: cover;
}

.hotel-info {
    padding: 15px;
}

.hotel-name {
    font-size: 16px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 10px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.hotel-intro {
    font-size: 14px;
    color: #606266;
    margin-bottom: 10px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.hotel-address {
    font-size: 13px;
    color: #909399;
    display: flex;
    align-items: center;
    gap: 5px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.hotel-address i {
    color: #fa8c16;
}

@media (max-width: 1200px) {
    .hotel-item {
        width: calc(50% - 10px);
    }
}

@media (max-width: 768px) {
    .hotel-item {
        width: 100%;
    }
}
</style>