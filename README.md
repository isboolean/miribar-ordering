# MiriBar 在线扫码点餐系统

MiriBar 是一款专为现代酒吧场景（含 Livehouse、餐酒吧）打造的智能扫码点餐系统。顾客扫码即可进入点餐页面，无需下载 APP，下单后自动同步至吧台/后厨看板，提升点餐效率与经营体验。

## 功能简介

### 顾客端（H5）
- **扫码点餐**：扫描桌台二维码直接进入菜单页，无需下载 APP
- **菜单浏览**：按分类浏览商品，支持多规格（杯型/份量）、实时库存与售罄状态
- **购物车与下单**：加购商品、填写备注、提交订单，支持同桌多设备同时点餐
- **订单跟踪**：实时查看订单状态（待接单 → 制作中 → 已完成）
- **加单**：同一桌可继续追加订单，自动并入本桌账单

### 吧台/后厨端
- **实时订单看板**：新订单实时推送，按商品类型自动拆单（酒水→吧台，小食→后厨）
- **状态流转**：接单 → 制作中 → 已完成，状态实时同步顾客端
- **桌台管理**：开台、并台、换台、清台操作
- **线下结账**：按桌合并账单，支持折扣与抹零

### 商家管理端
- **商品管理**：维护分类、商品、多规格、图片、库存与上下架状态
- **门店管理**：门店信息配置、桌台管理、员工账号与角色权限
- **经营报表**：营收统计、商品分析、时段销售分布（P1）

## 技术栈

### 前端
- Vue 3 + Vite
- Vue Router
- Pinia（状态管理）
- WebSocket（实时通信）

### 后端
- Java + Spring Boot
- MyBatis
- MySQL
- WebSocket

## 项目结构

```
miribar-ordering/
├── client/                 # 前端项目（Vue 3）
│   ├── src/
│   │   ├── views/          # 页面视图
│   │   │   ├── customer/   # 顾客端（菜单、订单状态）
│   │   │   ├── kitchen/    # 后厨/吧台看板
│   │   │   └── admin/      # 商家管理端
│   │   ├── components/     # 公共组件
│   │   ├── stores/         # Pinia 状态管理
│   │   ├── composables/    # 组合式函数
│   │   ├── api/            # API 接口
│   │   └── router/         # 路由配置
│   ├── package.json
│   └── vite.config.js
── server/                 # 后端项目（Spring Boot）
│   ├── src/main/java/
│   │   └── com/miribar/ordering/
│   │       ├── controller/ # 控制器
│   │       ├── service/    # 业务逻辑
│   │       ├── mapper/     # MyBatis Mapper
│   │       ├── entity/     # 实体类
│   │       ├── dto/        # 数据传输对象
│   │       ├── config/     # 配置类
│   │       └── websocket/  # WebSocket 处理
│   ├── src/main/resources/
│   │   ├── mapper/         # MyBatis XML 映射
│   │   ├── application.yml # 应用配置
│   │   └── schema.sql      # 数据库脚本
│   ├── sql/                # SQL 脚本目录
│   │   └── schema.sql      # 建表与示例数据
│   └── pom.xml
└── docs/                   # 文档
    └── requirements.md     # 产品需求文档
```

## 快速开始

### 环境要求
- Node.js 16+（前端）
- JDK 17+（后端）
- MySQL 8.0+

### 前端启动

```bash
cd client
npm install
npm run dev
```

### 后端启动

1. 创建数据库并导入 SQL 脚本：
```bash
# 在 MySQL 中执行
source server/sql/schema.sql
```

2. 修改 `server/src/main/resources/application.yml` 中的数据库配置

3. 启动 Spring Boot 应用：
```bash
cd server
mvn spring-boot:run
```

## 数据库表结构

| 表名 | 说明 |
|------|------|
| t_store | 门店 |
| t_dining_table | 桌台 |
| t_category | 商品分类 |
| t_product | 商品 |
| t_sku | 商品规格 |
| t_customer | 顾客 |
| t_order | 订单 |
| t_order_item | 订单项 |
| t_staff | 员工 |

## 版本规划

| 版本 | 范围 |
|------|------|
| V1.0（MVP） | 扫码点餐闭环（H5）：菜单浏览、购物车、下单、线下结账；吧台/后厨看板；桌台管理；商品管理 |
| V1.1 | 微信小程序端；在线支付；会员登录、积分、优惠券；推荐 v1；经营报表；小票打印 |
| V1.2 | 智能推荐 v2；储值卡；营销活动；报表增强；店内播报 |
| V2 | 多门店集团化管理、开放 API、第三方 POS 对接 |

## License

MIT
