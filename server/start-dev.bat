@echo off
REM ============================================================
REM MiriBar dev startup - run this on the DEV machine (with MySQL)
REM 1. Change VM_IP below to your VM's IP address
REM 2. Start middleware on the VM first (see server/docker-compose.yml)
REM 3. Double-click this file, or run from terminal
REM ============================================================

set VM_IP=192.168.1.100

REM ---------- middleware endpoints (all on the VM) ----------
set NACOS_ADDR=%VM_IP%:8848
set REDIS_HOST=%VM_IP%
set KAFKA_SERVERS=%VM_IP%:9092
set ES_URIS=http://%VM_IP%:9200
set MONGODB_URI=mongodb://%VM_IP%:27017/miribar_notification
REM Sentinel dashboard is optional; comment out if not deployed
set SENTINEL_DASHBOARD=%VM_IP%:8858

REM ---------- MySQL stays local ----------
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=Admin
set MYSQL_PASSWORD=199811

set BASE=%~dp0
set MVN=D:\soft\maven\apache-maven-3.9.16\bin\mvn.cmd

echo [1/6] starting gateway ...
start "miribar-gateway" cmd /c "cd /d %BASE% && %MVN% -pl miribar-gateway spring-boot:run -q"
timeout /t 5 /nobreak >nul

echo [2/6] starting auth-service ...
start "miribar-auth" cmd /c "cd /d %BASE% && %MVN% -pl miribar-auth-service spring-boot:run -q"
timeout /t 5 /nobreak >nul

echo [3/6] starting store-service ...
start "miribar-store" cmd /c "cd /d %BASE% && %MVN% -pl miribar-store-service spring-boot:run -q"
timeout /t 5 /nobreak >nul

echo [4/6] starting product-service ...
start "miribar-product" cmd /c "cd /d %BASE% && %MVN% -pl miribar-product-service spring-boot:run -q"
timeout /t 5 /nobreak >nul

echo [5/6] starting order-service ...
start "miribar-order" cmd /c "cd /d %BASE% && %MVN% -pl miribar-order-service spring-boot:run -q"
timeout /t 5 /nobreak >nul

echo [6/6] starting notification-service ...
start "miribar-notification" cmd /c "cd /d %BASE% && %MVN% -pl miribar-notification-service spring-boot:run -q"

echo.
echo All services launching in separate windows. Gateway will be at http://localhost:8080
pause
