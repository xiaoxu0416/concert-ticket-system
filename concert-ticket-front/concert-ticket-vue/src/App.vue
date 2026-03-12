<template>
  <div id="app">
    <transition name="page-fade" mode="out-in">
      <router-view />
    </transition>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      userName: localStorage.getItem('userName') || ''
    }
  },
  methods: {
    logout() {
      localStorage.clear()
      this.$router.push('/login')
      this.$message.success('退出成功')
    }
  }
}
</script>

<style>
/* ============================================================
   演唱会门票系统 · 浅色主题 · 全局样式
   设计语言: Warm Stage Light — 温暖、精致、现代
   ============================================================ */

:root {
  /* 主色调 */
  --ct-primary: #E85D3A;
  --ct-primary-light: #FF7F5C;
  --ct-primary-dark: #C94A2A;
  --ct-primary-bg: #FFF4F0;

  /* 功能色 */
  --ct-success: #10B981;
  --ct-warning: #F59E0B;
  --ct-danger: #EF4444;
  --ct-info: #6366F1;

  /* 中性色 */
  --ct-text-primary: #1D1D1F;
  --ct-text-secondary: #6B7280;
  --ct-text-tertiary: #9CA3AF;
  --ct-border: #E5E7EB;
  --ct-border-light: #F3F4F6;
  --ct-divider: #F0EDE8;

  /* 背景 */
  --ct-bg-page: #F7F6F3;
  --ct-bg-card: #FFFFFF;
  --ct-bg-elevated: #FFFFFF;
  --ct-bg-warm: #FFFBF7;

  /* 阴影 */
  --ct-shadow-sm: 0 1px 2px rgba(0,0,0,0.04), 0 1px 3px rgba(0,0,0,0.06);
  --ct-shadow-md: 0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -2px rgba(0,0,0,0.05);
  --ct-shadow-lg: 0 10px 25px -3px rgba(0,0,0,0.06), 0 4px 6px -4px rgba(0,0,0,0.04);
  --ct-shadow-xl: 0 20px 40px -4px rgba(0,0,0,0.08), 0 8px 16px -6px rgba(0,0,0,0.04);
  --ct-shadow-glow: 0 0 20px rgba(232,93,58,0.15);

  /* 圆角 */
  --ct-radius-sm: 8px;
  --ct-radius-md: 12px;
  --ct-radius-lg: 16px;
  --ct-radius-xl: 24px;
  --ct-radius-full: 9999px;

  /* 字体 */
  --ct-font-sans: 'DM Sans', 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  --ct-font-display: 'DM Sans', 'Noto Sans SC', sans-serif;

  /* 过渡 */
  --ct-transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  scroll-behavior: smooth;
}

body {
  background: var(--ct-bg-page);
  overflow-x: hidden;
}

#app {
  font-family: var(--ct-font-sans);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: var(--ct-text-primary);
  min-height: 100vh;
}

/* 页面切换动画 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.page-fade-enter {
  opacity: 0;
  transform: translateY(8px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* ============ Element UI 全局覆写 ============ */

/* 按钮 */
.el-button--primary {
  background: var(--ct-primary) !important;
  border-color: var(--ct-primary) !important;
  border-radius: var(--ct-radius-sm) !important;
  font-weight: 500 !important;
  letter-spacing: 0.3px;
  transition: var(--ct-transition) !important;
}
.el-button--primary:hover,
.el-button--primary:focus {
  background: var(--ct-primary-light) !important;
  border-color: var(--ct-primary-light) !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(232,93,58,0.35) !important;
}

/* 输入框 */
.el-input__inner {
  border-radius: var(--ct-radius-sm) !important;
  border-color: var(--ct-border) !important;
  transition: var(--ct-transition) !important;
  font-family: var(--ct-font-sans) !important;
}
.el-input__inner:focus {
  border-color: var(--ct-primary) !important;
  box-shadow: 0 0 0 3px rgba(232,93,58,0.1) !important;
}

/* 卡片 */
.el-card {
  border-radius: var(--ct-radius-lg) !important;
  border: 1px solid var(--ct-border-light) !important;
  box-shadow: var(--ct-shadow-sm) !important;
  transition: var(--ct-transition) !important;
}
.el-card:hover {
  box-shadow: var(--ct-shadow-md) !important;
}

/* 消息提示 */
.el-message {
  border-radius: var(--ct-radius-md) !important;
  border: none !important;
  box-shadow: var(--ct-shadow-lg) !important;
  font-family: var(--ct-font-sans) !important;
}

/* 对话框 */
.el-dialog {
  border-radius: var(--ct-radius-xl) !important;
  box-shadow: var(--ct-shadow-xl) !important;
  overflow: hidden;
}
.el-dialog__header {
  padding: 24px 28px 16px !important;
}
.el-dialog__body {
  padding: 16px 28px !important;
}
.el-dialog__title {
  font-weight: 600 !important;
  font-size: 18px !important;
  color: var(--ct-text-primary) !important;
}

/* 分页 */
.el-pagination {
  font-family: var(--ct-font-sans) !important;
}
.el-pagination .el-pager li.active {
  background: var(--ct-primary) !important;
  border-radius: var(--ct-radius-sm) !important;
  color: #fff !important;
}
.el-pagination .el-pager li {
  color: #fff !important;
}

/* 表格 */
.el-table {
  font-family: var(--ct-font-sans) !important;
}
.el-table th {
  background: var(--ct-bg-page) !important;
  color: var(--ct-text-secondary) !important;
  font-weight: 600 !important;
  font-size: 13px !important;
  text-transform: uppercase !important;
  letter-spacing: 0.5px !important;
}

/* Tag */
.el-tag {
  border-radius: var(--ct-radius-full) !important;
  font-weight: 500 !important;
  font-size: 12px !important;
  padding: 0 12px !important;
  border: none !important;
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #D1D5DB;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: #9CA3AF;
}

/* 选中文本颜色 */
::selection {
  background: rgba(232,93,58,0.15);
  color: var(--ct-text-primary);
}
</style>