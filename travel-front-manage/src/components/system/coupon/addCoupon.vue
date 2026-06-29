<template>
<div>
  <el-dialog title="新增优惠券" width="46%" :destroy-on-close="true" :visible.sync="addVisible" :before-close="handleClose">
    <el-form :model="form" :rules="rules" ref="ruleForm">
      <el-row :gutter="10">
        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
          <span class="search-title">名称:</span>
          <div style="width:100%">
            <el-form-item prop="name" style="margin-bottom:0">
              <el-input v-model="form.name" size="mini" placeholder="请输入优惠券名称"></el-input>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
          <span class="search-title">描述:</span>
          <div style="width:100%">
            <el-form-item prop="description" style="margin-bottom:0">
              <el-input type="textarea" v-model="form.description" size="mini" placeholder="请输入描述"></el-input>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
          <span class="search-title">门槛(元):</span>
          <div style="width:100%">
            <el-form-item prop="minAmount" style="margin-bottom:0">
              <el-input type="number" v-model="form.minAmount" size="mini" placeholder="最低消费金额"></el-input>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
          <span class="search-title">优惠(元):</span>
          <div style="width:100%">
            <el-form-item prop="discountAmount" style="margin-bottom:0">
              <el-input type="number" v-model="form.discountAmount" size="mini" placeholder="优惠金额"></el-input>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
          <span class="search-title">所需积分:</span>
          <div style="width:100%">
            <el-form-item prop="pointsRequired" style="margin-bottom:0">
              <el-input type="number" v-model="form.pointsRequired" size="mini" placeholder="兑换所需积分"></el-input>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
          <span class="search-title">会员等级:</span>
          <div style="width:100%">
            <el-form-item prop="requireLevel" style="margin-bottom:0">
              <el-select v-model="form.requireLevel" size="mini" placeholder="请选择" style="width:100%">
                <el-option label="普通用户" :value="0"></el-option>
                <el-option label="VIP" :value="1"></el-option>
                <el-option label="SVIP" :value="2"></el-option>
              </el-select>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
          <span class="search-title">发放总量:</span>
          <div style="width:100%">
            <el-form-item prop="totalCount" style="margin-bottom:0">
              <el-input type="number" v-model="form.totalCount" size="mini" placeholder="可领取总数量"></el-input>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
          <span class="search-title">有效天数:</span>
          <div style="width:100%">
            <el-form-item prop="validDays" style="margin-bottom:0">
              <el-input type="number" v-model="form.validDays" size="mini" placeholder="领取后有效天数"></el-input>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
          <span class="search-title">排序:</span>
          <div style="width:100%">
            <el-form-item prop="sort" style="margin-bottom:0">
              <el-input type="number" v-model="form.sort" size="mini" placeholder="数字越小越靠前"></el-input>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
          <span class="search-title">状态:</span>
          <div style="width:100%">
            <el-form-item prop="status" style="margin-bottom:0">
              <el-select v-model="form.status" size="mini" style="width:100%">
                <el-option label="上架" :value="1"></el-option>
                <el-option label="下架" :value="0"></el-option>
              </el-select>
            </el-form-item>
          </div>
        </el-col>
      </el-row>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button size="mini" type="primary" @click="submit">确 定</el-button>
      <el-button size="mini" @click="handleClose">取 消</el-button>
    </div>
  </el-dialog>
</div>
</template>

<script>
import { saveSysCoupon } from '../../../api/api'

export default {
  props: ['addVisible'],
  data() {
    return {
      form: {
        name: '',
        description: '',
        minAmount: '',
        discountAmount: '',
        pointsRequired: '',
        requireLevel: 0,
        totalCount: '',
        validDays: 30,
        sort: 0,
        status: 1
      },
      rules: {
        name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
        minAmount: [{ required: true, message: '请输入门槛金额', trigger: 'blur' }],
        discountAmount: [{ required: true, message: '请输入优惠金额', trigger: 'blur' }],
        pointsRequired: [{ required: true, message: '请输入所需积分', trigger: 'blur' }],
        totalCount: [{ required: true, message: '请输入发放总量', trigger: 'blur' }],
        validDays: [{ required: true, message: '请输入有效天数', trigger: 'blur' }]
      }
    }
  },
  methods: {
    submit() {
      this.$refs.ruleForm.validate(valid => {
        if (!valid) return
        saveSysCoupon(this.form).then(res => {
          if (res.code == 1000) {
            this.$notify.success({ title: '成功', message: '保存成功' })
            this.handleClose()
          } else {
            this.$notify.error({ title: '错误', message: res.message })
          }
        })
      })
    },
    handleClose() {
      this.form = {
        name: '',
        description: '',
        minAmount: '',
        discountAmount: '',
        pointsRequired: '',
        requireLevel: 0,
        totalCount: '',
        validDays: 30,
        sort: 0,
        status: 1
      }
      this.$emit('addFalse')
    }
  }
}
</script>

<style lang=scss scoped>
  .el-col {
    display: flex;
    flex-direction: row;
    align-items: center;
    margin-top: 24px
  }
  .search-title {
    font-family: '黑体';
    float: right;
    white-space: nowrap;
    font-size: 14px;
    width: 84px;
    text-align: right;
  }
</style>
