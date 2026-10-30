# MiriBar 扫码点餐 — 顾客端微信小程序

MiriBar 在线扫码点餐系统的**顾客端微信小程序**（微信原生开发，无第三方框架依赖）。
本项目为独立前端工程，与后端 `server/`（Spring Boot）、H5 端 `client/`（Vue 3）同仓不同目录，可单独打包提审部署。

## 功能

- **扫码点餐**：扫桌台小程序码（`scene=qrToken`）自动识别门店与桌位，兼容存量 H5 桌码（普通链接二维码）与站内扫码
- **菜单浏览**：分类侧栏联动、多规格选择、售罄置灰、下拉刷新
- **购物车与下单**：单品/整单备注、3 秒防重提交、线下结账模式
- **订单跟踪**：本桌订单 15 秒轮询、桌台账单合计、加单、取消待接单
- **可选登录**：`wx.login` 静默登录，手机号验证码绑定可选，不登录可点餐

## 目录结构

```
miribar-miniprogram/
├── app.js / app.json / app.wxss   # 全局入口与配置
├── project.config.json            # 开发者工具项目配置
├── sitemap.json
├── api/index.js                   # 接口层（复用后端 /api/** 顾客端接口）
├── utils/
│   ├── config.js                  # 后端 BASE_URL（部署时改这里）
│   ├── request.js                 # 请求封装（401 静默重登）
│   ├── auth.js                    # 登录态
│   ├── context.js                 # 桌位上下文持久化
│   ├── cart.js                    # 购物车
│   └── format.js                  # 金额/状态/时间格式化
└── pages/
    ├── index/    # 扫码落地页
    ├── menu/     # 菜单页
    ├── confirm/  # 确认订单页
    ├── orders/   # 订单跟踪页
    └── login/    # 会员登录页（可选）
```

## 快速开始

1. 微信开发者工具 → 导入项目 → 选择本目录（`miribar-miniprogram/`），AppID 填自己的（测试可用测试号）
2. 启动后端（见仓库根 README），确认 `utils/config.js` 的 `BASE_URL` 指向后端地址
3. 开发者工具「详情 → 本地设置」勾选「不校验合法域名」（仅开发期）
4. 模拟扫码联调：编译模式 → 添加编译模式 → 启动页面 `pages/index/index`，启动参数填 `scene=桌码qrToken`

## 打包部署（提审）

1. **改配置**：`utils/config.js` 的 `BASE_URL` 改为生产 HTTPS 域名；`project.config.json` 填正式 AppID
2. **后台配置**（微信公众平台）：
   - 开发管理 → 服务器域名：request 合法域名 = 后端 API 域名；downloadFile 合法域名 = 商品图片域名
   - 若需扫存量 H5 桌码：配置「扫普通链接二维码打开小程序」规则
3. **上传**：开发者工具右上角「上传」，填版本号与备注 → mp 后台「版本管理」提交审核
4. **桌码生成**：后端员工接口 `GET /api/table/{id}/wxa-qrcode` 生成桌台小程序码（scene=qrToken），打印张贴

> 审核提醒：酒水类目需相应资质；需在 mp 后台配置《用户隐私保护指引》。以官方最新审核规范为准。

## 相关文档

- 仓库根《小程序项目档案.md》：页面进度、接口约定、待办
- 仓库根 `docs/requirements.md`：产品需求文档
