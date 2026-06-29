<template>
<div>
  <el-dialog title="编辑积分商品" width="46%" :destroy-on-close="true" :visible.sync="updateVisible" :before-close="handleClose">
    <el-form :model="form" :rules="rules" ref="ruleForm">
      <el-row :gutter="10">
        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
          <span class="search-title">名称:</span>
          <div style="width:100%">
            <el-form-item prop="name" style="margin-bottom:0">
              <el-input v-model="form.name" size="mini"></el-input>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
          <span class="search-title">描述:</span>
          <div style="width:100%">
            <el-form-item prop="description" style="margin-bottom:0">
              <el-input type="textarea" v-model="form.description" size="mini"></el-input>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
          <span class="search-title">参考价(元):</span>
          <div style="width:100%">
            <el-form-item prop="price" style="margin-bottom:0">
              <el-input type="number" v-model="form.price" size="mini"></el-input>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
          <span class="search-title">所需积分:</span>
          <div style="width:100%">
            <el-form-item prop="pointsRequired" style="margin-bottom:0">
              <el-input type="number" v-model="form.pointsRequired" size="mini"></el-input>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
          <span class="search-title">库存:</span>
          <div style="width:100%">
            <el-form-item prop="stock" style="margin-bottom:0">
              <el-input type="number" v-model="form.stock" size="mini"></el-input>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
          <span class="search-title">商品类型:</span>
          <div style="width:100%">
            <el-form-item prop="type" style="margin-bottom:0">
              <el-select v-model="form.type" size="mini" style="width:100%">
                <el-option label="实物商品" :value="0"></el-option>
                <el-option label="纪念品" :value="1"></el-option>
                <el-option label="虚拟商品" :value="2"></el-option>
              </el-select>
            </el-form-item>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
          <span class="search-title">排序:</span>
          <div style="width:100%">
            <el-form-item prop="sort" style="margin-bottom:0">
              <el-input type="number" v-model="form.sort" size="mini"></el-input>
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
        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
          <span class="search-title">图片:</span>
          <div style="width:100%">
            <el-form-item prop="images" style="margin-bottom:0">
              <el-upload
                :file-list="fileList"
                ref="upload"
                :action="uploadImageUrl()"
                accept="image/*"
                :multiple="false"
                :limit="5"
                :before-upload="beforeAvatorUpload"
                list-type="picture-card"
                :on-preview="handlePictureCardPreview"
                :on-exceed="handleExceed"
                :on-success="handleAvatorSuccess"
                :on-remove="handleRemove">
                <i class="el-icon-plus"></i>
              </el-upload>
              <el-dialog title="预览" :visible.sync="dialogVisible" append-to-body>
                <img width="100%" :src="dialogImageUrl" alt="">
              </el-dialog>
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
import { mixin } from '../../../minix/index'
import { editSysProduct, getSysProductById } from '../../../api/api'

export default {
  mixins: [mixin],
  props: ['updateVisible', 'updateId'],
  data() {
    return {
      form: {},
      fileList: [],
      dialogImageUrl: '',
      dialogVisible: false,
      rules: {
        name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
        pointsRequired: [{ required: true, message: '请输入所需积分', trigger: 'blur' }],
        stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
        images: [{ required: true, message: '请上传图片', trigger: 'blur' }]
      }
    }
  },
  watch: {
    updateId(val) {
      if (val) this.loadData()
    }
  },
  methods: {
    loadData() {
      getSysProductById({ id: this.updateId }).then(res => {
        if (res.code == 1000) {
          this.form = res.data || {}
          this.fileList = []
          if (this.form.images) {
            this.form.images.split(',').forEach(url => {
              if (url) this.fileList.push({ url })
            })
          }
        }
      })
    },
    handleAvatorSuccess(res) {
      if (res.code == 1000) {
        this.$message.success('上传成功')
        this.fileList.push({ url: this.$store.state.configure.HOST + res.message })
      } else {
        this.$notify.error({ title: '错误', message: res.message })
      }
    },
    handleRemove(file, fileList) {
      this.fileList = fileList
    },
    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },
    submit() {
      this.form.images = this.fileList.map(item => item.url).join(',')
      this.$refs.ruleForm.validate(valid => {
        if (!valid) return
        editSysProduct(this.form).then(res => {
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
      this.form = {}
      this.fileList = []
      this.$emit('updateFalse')
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
