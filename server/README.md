# miribar-ordering-server

MiriBar 点单服务的后端模块（Spring Boot），位于仓库的 `server/` 目录。

## 技术栈

| 组件 | 版本 |
| --- | --- |
| Java | 17 |
| Spring Boot | 3.5.3 |
| 构建工具 | Maven |

## 目录结构

```
server
├── pom.xml
└── src
    ├── main
    │   ├── java/com/miribar/ordering
    │   │   └── MiribarOrderingApplication.java   # 启动类
    │   └── resources
    │       └── application.yml                   # 配置文件
    └── test
        └── java/com/miribar/ordering
            └── MiribarOrderingApplicationTests.java
```

## 本地运行

```bash
cd server
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/miribar-ordering-0.0.1-SNAPSHOT.jar
```

启动成功后访问 <http://localhost:8080>。

Maven 本地仓库位于 `D:\maven_repo\repository`，镜像与仓库配置见 `D:\maven_repo\settings.xml`。
