<template>
  <div class="search-panel">
    <div class="search-panel__title">
      <i :class="icon"></i> {{ title }}
    </div>
    <div class="search-panel__fields">
      <slot />
      <div class="search-panel__actions">
        <el-button type="primary" icon="el-icon-search" @click="$emit('search')">搜索</el-button>
        <el-button icon="el-icon-refresh-left" @click="$emit('reset')">重置</el-button>
      </div>
    </div>
    <div v-if="$slots.quick" class="search-panel__quick">
      <slot name="quick" />
    </div>
  </div>
</template>

<script>
export default {
  name: 'SearchPanel',
  props: {
    title: {
      type: String,
      default: '筛选'
    },
    icon: {
      type: String,
      default: 'el-icon-search'
    }
  }
}
</script>

<style scoped>
.search-panel {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.search-panel__title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.search-panel__title i {
  color: #409EFF;
}

.search-panel__fields {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.search-panel__fields ::v-deep .search-field {
  width: 200px;
}

.search-panel__actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.search-panel__quick {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #f0f2f5;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px 8px;
}

.search-panel__quick ::v-deep .quick-label {
  font-size: 13px;
  color: #909399;
  margin-right: 4px;
}

.search-panel__quick ::v-deep .el-button--text {
  color: #409EFF;
  padding: 4px 8px;
}

@media (max-width: 992px) {
  .search-panel__actions {
    margin-left: 0;
    width: 100%;
  }

  .search-panel__fields ::v-deep .search-field {
    width: 100%;
  }
}
</style>
