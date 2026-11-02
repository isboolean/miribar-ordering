# MiriBar Ordering 部署手册（Ubuntu 22.04/24.04 + systemd + Caddy）

## 一、运行环境要求

| 组件 | 版本要求 | 用途 |
|---|---|---|
| JDK | 17+（项目基于 Spring Boot 3.5.3） | 后端运行 |
| Maven | 3.8+ | 后端构建 |
| Node.js | 18+（前端 Vite 6，服务器实测 22.x 可用） | 前端构建 |
| MySQL | 8.0+（utf8mb4） | 数据库 |
| Caddy / Nginx | 任一 | 反向代理 + SPA 兜底（可选，Spring Boot 也可直接托管静态资源） |

服务器资源建议：2C / 2G 以上（JVM `-Xmx640m` + MySQL 同机已验证可运行）。

## 二、目录约定

```
/opt/src/miribar      源码（git clone 或 tarball 解压）
/opt/miribar/app.jar  后端运行包
/opt/miribar/static/  前端构建产物（client/dist/*）
/etc/miribar/db.env   数据库凭据（root:600，勿提交 git）
/etc/systemd/system/miribar.service  systemd 单元
/etc/caddy/Caddyfile  反向代理配置
```

## 三、部署步骤

### 1. 安装依赖

```bash
sudo apt-get update
sudo apt-get install -y git curl openjdk-17-jdk-headless maven nodejs npm mysql-server
```

### 2. 获取代码并构建

```bash
sudo mkdir -p /opt/src && cd /opt/src
sudo git clone --depth 1 <你的仓库地址> miribar   # 或下载 tarball 解压

# 后端（国内服务器建议配阿里云 Maven 镜像）
sudo mkdir -p /root/.m2 && cat > /root/.m2/settings.xml <<'EOS'
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
  <mirrors><mirror><id>aliyun</id><mirrorOf>*</mirrorOf>
  <url>https://maven.aliyun.com/repository/public</url></mirror></mirrors>
</settings>
EOS
cd /opt/src/miribar/server && sudo mvn -B -DskipTests package
sudo cp target/miribar-ordering-0.0.1-SNAPSHOT.jar /opt/miribar/app.jar

# 前端（国内服务器用 npmmirror）
cd /opt/src/miribar/client
sudo npm install --registry=https://registry.npmmirror.com --no-audit --no-fund
sudo npm run build
sudo mkdir -p /opt/miribar/static && sudo cp -r dist/* /opt/miribar/static/
```

### 3. 初始化数据库

> 注意：`server/sql/schema.sql` 曾使用 MariaDB 的 `ADD COLUMN IF NOT EXISTS` 语法，
> MySQL 8 需先去掉 `IF NOT EXISTS`（当前仓库已修复，若用旧版本需手动 sed）。

```bash
sudo bash <<'EOF'
set -e
PASS=$(openssl rand -hex 12)
mysql -e "CREATE DATABASE IF NOT EXISTS miribar_ordering_single DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -e "CREATE USER IF NOT EXISTS 'miribar'@'localhost' IDENTIFIED BY '$PASS'; GRANT ALL PRIVILEGES ON miribar_ordering_single.* TO 'miribar'@'localhost'; FLUSH PRIVILEGES;"
for f in schema.sql migrate_org_code.sql migrate_org_hq_division.sql migrate_staff_no.sql update_permission.sql seed_demo_org_staff.sql; do
  mysql miribar_ordering_single < /opt/src/miribar/server/sql/$f || echo "warn: $f 跳过"
done
mkdir -p /etc/miribar; umask 077
printf 'DB_NAME=miribar_ordering_single\nDB_USER=miribar\nDB_PASSWORD=%s\n' "$PASS" > /etc/miribar/db.env
chmod 600 /etc/miribar/db.env
echo DB-READY
EOF
```

### 4. systemd 服务单元

`/etc/systemd/system/miribar.service`：

```ini
[Unit]
Description=MiriBar Ordering Service
After=network.target mysql.service
Wants=mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/miribar
EnvironmentFile=/etc/miribar/db.env
ExecStart=/usr/bin/java -Xms256m -Xmx640m -XX:+UseSerialGC -jar /opt/miribar/app.jar --server.port=8080 "--spring.datasource.url=jdbc:mysql://127.0.0.1:3306/${DB_NAME}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" --spring.datasource.username=${DB_USER} --spring.datasource.password=${DB_PASSWORD} --spring.web.resources.static-locations=file:/opt/miribar/static/
Restart=on-failure
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now miribar
```

### 5. Caddy 反向代理（含 SPA 兜底）

`/etc/caddy/Caddyfile`：

```caddyfile
:80 {
    encode gzip
    handle /api/* {
        reverse_proxy 127.0.0.1:8080
    }
    handle {
        root * /opt/miribar/static
        try_files {path} /index.html
        file_server
    }
}
```

```bash
sudo caddy validate --config /etc/caddy/Caddyfile
sudo systemctl reload caddy
```

云服务器还需在防火墙/安全组放通 TCP 80（HTTPS 则 443）。

## 四、必要配置项

| 配置 | 位置 | 说明 |
|---|---|---|
| 服务端口 | `--server.port=8080` | 后端 HTTP 端口，仅本机监听即可 |
| 数据库连接 | `--spring.datasource.url/username/password`（由 `/etc/miribar/db.env` 注入） | 库名 `miribar_ordering_single`，账号 `miribar` |
| 静态资源目录 | `--spring.web.resources.static-locations=file:/opt/miribar/static/` | 前端产物；由 Caddy 托管时此项仅为兜底 |
| JVM 内存 | `-Xmx640m` | 2G 内存机器的建议值，可按机器调整 |
| 管理端账号 | 数据库种子数据 | `admin / admin123`（登录需图形验证码），**上线后请立即改密** |
| 对外入口 | Caddyfile `:80` | 绑定域名时把 `:80` 换成域名，Caddy 自动签发 HTTPS 证书 |

## 五、常见问题排查

| 现象 | 原因 | 处理 |
|---|---|---|
| `ERROR 1045 Access denied for user 'ubuntu'@'localhost'` | Ubuntu 下 mysql root 走 auth_socket，普通用户连不上 | 命令加 `sudo`（或 `sudo bash` 整段执行） |
| 导入 schema.sql 报 `ADD COLUMN IF NOT EXISTS` 语法错误 | 该语法是 MariaDB 专属 | 去掉 `IF NOT EXISTS`（新版仓库已修） |
| 服务起不来 / 日志报 Access denied for 'miribar' | db.env 凭据与数据库不一致 | 重新执行建库脚本，或手动 `ALTER USER` 后更新 db.env |
| Maven 下载极慢 | 直连 Maven Central | 使用阿里云镜像 settings.xml |
| `/login`、`/admin` 刷新返回 500 JSON | Spring Boot 未配置 SPA 路由转发 | 用 Caddy `try_files {path} /index.html`，或升级到含 `/login` 转发的 SpaForwardController |
| 接口全部返回 401 | 未登录或 token 过期 | 先走 `/api/auth/login`（带图形验证码）拿 token |
| 页面白屏但接口正常 | static 目录未更新或 assets 路径不对 | 重新拷贝 `client/dist/*`，强刷浏览器 |
| 2G 内存机器构建/启动 OOM | Maven 与 JVM 同时占内存 | 先构建后启动，避免并行；JVM 用 `-Xmx640m` + SerialGC |

## 六、日常运维

```bash
systemctl status miribar          # 服务状态
journalctl -u miribar -f          # 实时日志
sudo systemctl restart miribar    # 重启
# 更新发版：重新构建 jar/dist 后覆盖 /opt/miribar/ 下对应文件，再 restart
```
