<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<style>
  html,body,#app,.common-layout{
    margin: 0;
    padding: 0;
    height:100%;
    background-color: #f5f7fa;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    color: #2c3e50;
    line-height: 1.6;
  }
  .xiaoshi {
    display: none;
  }
  body::-webkit-scrollbar {
      width: 0px;
      height: 0px;
  }
-- 滚动条的宽度
  ::-webkit-scrollbar {
    width: 8px;
    height: 8px;
    background-color: #e4e4e4;
    border-radius: 6px;
  }
-- 滚动条的滑块
  ::-webkit-scrollbar-thumb {
    background-color: #a1a3a9;
    border-radius: 6px;
  }
  .el-pagination {
    padding-bottom:10px!important;
    padding-top:10px!important;
    display:flex;
    justify-content: flex-end
  }
-- 对话框圆角
  .el-dialog__wrapper {
    .el-dialog {
      border-radius: 8px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    }
  }
  .el-dialog__title {
    font-size: 16px !important;
    font-weight: 600;
    color: #2c3e50;
  }
-- 对话框样式
  .el-dialog__header {
    border-bottom: 1px solid #e3e8ee;
    padding: 20px 24px;
  }
  .el-dialog__body {
    padding: 24px;
  }
  .el-button {
    border-radius: 4px;
    font-weight: 500;
    transition: all 0.3s;
  }
  .el-button--primary {
    background-color: #409EFF;
    border-color: #409EFF;
  }
  .el-button--primary:hover {
    background-color: #66b1ff;
    border-color: #66b1ff;
  }
  .el-input__inner {
    border-radius: 4px;
    transition: all 0.3s;
  }
  .el-input__inner:focus {
    border-color: #409EFF;
    box-shadow: 0 0 0 2px rgba(64,158,255,.2);
  }
</style>
