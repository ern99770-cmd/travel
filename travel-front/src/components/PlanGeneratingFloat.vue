<template>
  <transition name="plan-float-fade">
    <div v-if="visible" class="plan-generating-float" :class="{ minimized: minimized }">
      <div v-if="minimized" class="plan-float-mini" @click="handleMiniClick">
        <i :class="loading ? 'el-icon-loading' : 'el-icon-success'"></i>
        <span>{{ loading ? '行程生成中' : '行程已生成，点击查看' }}</span>
      </div>
      <div v-else class="plan-float-card">
        <button class="plan-float-close" @click="handleClose" title="关闭">
          <i class="el-icon-close"></i>
        </button>
        <div class="plan-float-body">
          <i :class="loading ? 'el-icon-loading plan-float-icon' : 'el-icon-success plan-float-icon done'"></i>
          <div class="plan-float-info">
            <h4>{{ loading ? '正在为您规划行程' : '行程生成完成' }}</h4>
            <p v-if="destination">{{ destination }} · {{ loading ? loadingText : '已保存至我的行程' }}</p>
            <p v-else>{{ loading ? loadingText : '已保存至我的行程' }}</p>
            <div v-if="loading" class="plan-loading-bar"><span></span></div>
          </div>
        </div>
        <div class="plan-float-actions">
          <button v-if="loading" class="plan-float-btn secondary" @click="minimize">收起，后台继续</button>
          <button v-if="!loading && justCompleted" class="plan-float-btn primary" @click="goToMyPlan">查看我的行程</button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'PlanGeneratingFloat',
  computed: {
    ...mapState('plan', ['loading', 'loadingText', 'destination', 'minimized', 'justCompleted']),
    visible() {
      return this.loading || this.justCompleted
    }
  },
  methods: {
    minimize() {
      this.$store.dispatch('plan/minimize')
    },
    expand() {
      this.$store.dispatch('plan/expand')
    },
    handleMiniClick() {
      if (!this.loading && this.justCompleted) {
        this.goToMyPlan()
        return
      }
      this.expand()
    },
    goToMyPlan() {
      this.$store.dispatch('plan/goToMyPlan')
    },
    handleClose() {
      if (this.loading) {
        this.minimize()
      } else {
        this.$store.dispatch('plan/dismiss')
      }
    }
  }
}
</script>

<style scoped>
.plan-generating-float {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 3000;
  pointer-events: auto;
}

.plan-float-card {
  width: 340px;
  max-width: calc(100vw - 48px);
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 12px 40px rgba(26, 96, 234, 0.18);
  border: 1px solid rgba(102, 126, 234, 0.15);
  position: relative;
}

.plan-float-close {
  position: absolute;
  top: 12px;
  right: 12px;
  border: none;
  background: transparent;
  color: #999;
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  line-height: 1;
}

.plan-float-close:hover {
  color: #666;
}

.plan-float-body {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  padding-right: 20px;
}

.plan-float-icon {
  font-size: 28px;
  color: #1a60ea;
  flex-shrink: 0;
  margin-top: 2px;
}

.plan-float-icon.done {
  color: #67c23a;
}

.plan-float-info h4 {
  margin: 0 0 6px;
  font-size: 16px;
  color: #2c3e50;
}

.plan-float-info p {
  margin: 0 0 12px;
  font-size: 13px;
  color: #8a94a6;
  line-height: 1.5;
}

.plan-loading-bar {
  height: 3px;
  background: #eef3ff;
  border-radius: 999px;
  overflow: hidden;
}

.plan-loading-bar span {
  display: block;
  height: 100%;
  width: 40%;
  background: linear-gradient(90deg, #667eea, #1a60ea);
  border-radius: 999px;
  animation: plan-loading-slide 1.6s ease-in-out infinite;
}

@keyframes plan-loading-slide {
  0% { transform: translateX(-120%); }
  100% { transform: translateX(320%); }
}

.plan-float-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

.plan-float-btn {
  border: none;
  border-radius: 8px;
  padding: 8px 14px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.plan-float-btn.secondary {
  background: #f0f4ff;
  color: #1a60ea;
}

.plan-float-btn.secondary:hover {
  background: #e3ebff;
}

.plan-float-btn.primary {
  background: #1a60ea;
  color: #fff;
}

.plan-float-btn.primary:hover {
  background: #1550c8;
}

.plan-float-mini {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border-radius: 999px;
  padding: 10px 18px;
  box-shadow: 0 8px 24px rgba(26, 96, 234, 0.2);
  cursor: pointer;
  font-size: 13px;
  color: #1a60ea;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.plan-float-mini:hover {
  box-shadow: 0 10px 28px rgba(26, 96, 234, 0.28);
}

.plan-float-fade-enter-active,
.plan-float-fade-leave-active {
  transition: opacity 0.25s, transform 0.25s;
}

.plan-float-fade-enter,
.plan-float-fade-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
</style>
