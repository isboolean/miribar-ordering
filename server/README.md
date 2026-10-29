# miribar-ordering-server

MiriBar 点单服务后端（Spring Cloud 微服务架构），位于仓库的 `server/` 目录。

## 技术栈

| 组件 | 版本 |
| --- | --- |
| Java | 17 |
| Spring Boot | 3.3.4 |
| Spring Cloud | 2023.0.3 |
| Spring Cloud Alibaba | 2023.0.1.2（Nacos 注册/配置中心） |
| 构建 | Maven 多模块 |

## 模块结构

```
server
├── pom.xml                        # 父 POM（聚合各模块）
├── miribar-common/                # 公共模块（实体、DTO、工具）
├── miribar-gateway/               # 网关，端口 8080（前端 /api、/ws 统一入口）
├── miribar-auth-service/          # 认证服务，端口 9001
├── miribar-store-service/         # 门店/桌台服务，端口 9002
├── miribar-product-service/       # 商品/菜单服务，端口 9003
├── miribar-order-service/         # 订单服务，端口 9004（含 WebSocket）
├── miribar-notification-service/  # 通知服务，端口 9005
├── docker-compose.yml             # 中间件：MySQL、Redis、Nacos、Kafka、ES、MongoDB、Sentinel
└── sql/                           # 各服务建库脚本
```

## 本地运行

```bash
# 1. 启动中间件（需先启动 Docker Desktop）
docker-compose up -d

# 2. 编译
mvn clean install -DskipTests

# 3. 依次启动各服务（或在 IDE 中运行各模块的 *Application 主类）
mvn -pl miribar-gateway spring-boot:run
mvn -pl miribar-auth-service spring-boot:run
mvn -pl miribar-store-service spring-boot:run
mvn -pl miribar-product-service spring-boot:run
mvn -pl miribar-order-service spring-boot:run
mvn -pl miribar-notification-service spring-boot:run
```

Maven 本地仓库位于 `D:\maven_repo\repository`，镜像与仓库配置见 `D:\maven_repo\settings.xml`。

## 注意事项

- 数据库连接默认账号为 `Admin / 199811`，与 docker-compose 中 MySQL（root / root123）不一致，需创建该用户或通过环境变量 `MYSQL_USER` / `MYSQL_PASSWORD` 覆盖。
- Nacos 需要 `nacos_config` 库，首次启动前需手动创建。
