<template>
  <div class="page-layout" :class="layoutClass">
    <Header />
    <main class="page-layout__main">
      <slot />
    </main>
    <Bottom v-if="showFooter" />
  </div>
</template>

<script>
import Header from '@/components/header'
import Bottom from '@/components/bottom'

export default {
  name: 'PageLayout',
  components: {
    Header,
    Bottom
  },
  props: {
    showFooter: {
      type: Boolean,
      default: true
    },
    fullHeight: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    layoutClass() {
      return {
        'page-layout--full': this.fullHeight
      }
    }
  }
}
</script>

<style scoped>
.page-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-layout__main {
  padding-top: calc(var(--header-height, 80px) + var(--page-top-gap, 16px));
  box-sizing: border-box;
  min-height: calc(100vh - var(--header-height, 80px));
}

.page-layout--full {
  height: 100vh;
  overflow: hidden;
}

.page-layout--full .page-layout__main {
  height: calc(100vh - var(--header-height, 80px) - var(--page-top-gap, 16px));
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>
