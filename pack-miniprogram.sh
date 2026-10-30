#!/usr/bin/env bash
#
# MiriBar 顾客端小程序一键打包脚本
# 用途：将 miribar-miniprogram/ 打成提审/交付压缩包，排除本地私有配置等不入库文件
# 用法：
#   ./pack-miniprogram.sh            # 版本号默认 1.0.0
#   ./pack-miniprogram.sh 1.1.0      # 指定版本号
# 产物：release/miribar-miniprogram_v{版本}_{时间戳}.zip
#
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$ROOT_DIR/miribar-miniprogram"
OUT_DIR="$ROOT_DIR/release"
VERSION="${1:-1.0.0}"
STAMP="$(date +%Y%m%d-%H%M)"
NAME="miribar-miniprogram_v${VERSION}_${STAMP}"
STAGE="$(mktemp -d)"

# ---- 入口校验 ----
if [ ! -f "$SRC_DIR/app.json" ] || [ ! -f "$SRC_DIR/project.config.json" ]; then
  echo "[错误] 未找到 $SRC_DIR/app.json 或 project.config.json，请确认目录结构" >&2
  exit 1
fi

# ---- 排除清单（打包不入包的文件）----
# project.private.config.json：开发者个人本地配置，绝不能进提审包
# node_modules/.git 等：体积与安全考虑
EXCLUDES=(
  "project.private.config.json"
  "node_modules"
  ".git"
  ".gitignore"
  ".DS_Store"
  "Thumbs.db"
)

echo "==> 暂存拷贝（排除 ${#EXCLUDES[@]} 项）..."
mkdir -p "$STAGE/$NAME"
if command -v rsync >/dev/null 2>&1; then
  EXCLUDE_ARGS=()
  for e in "${EXCLUDES[@]}"; do EXCLUDE_ARGS+=(--exclude "$e"); done
  rsync -a "${EXCLUDE_ARGS[@]}" "$SRC_DIR/" "$STAGE/$NAME/"
else
  # Git Bash 自带 cp，用 find 过滤实现排除
  (cd "$SRC_DIR" && find . -type f \
    ! -path "./project.private.config.json" \
    ! -path "*/node_modules/*" \
    ! -path "*/.git/*" \
    ! -name ".gitignore" \
    ! -name ".DS_Store" \
    ! -name "Thumbs.db" \
    -exec cp --parents {} "$STAGE/$NAME/" \;)
fi

echo "==> 文件数：$(find "$STAGE/$NAME" -type f | wc -l)"

mkdir -p "$OUT_DIR"
OUT_FILE="$OUT_DIR/$NAME.zip"
cd "$STAGE"

# ---- 压缩：zip 优先，Windows 无 zip 时用 PowerShell Compress-Archive，最后 tar 兜底 ----
if command -v zip >/dev/null 2>&1; then
  zip -qr "$OUT_FILE" "$NAME"
elif command -v powershell.exe >/dev/null 2>&1; then
  # Git Bash 路径转 Windows 路径
  WIN_OUT="$(cygpath -w "$OUT_FILE")"
  powershell.exe -NoProfile -Command "Compress-Archive -Path '$(cygpath -w "$STAGE")\\$NAME' -DestinationPath '$WIN_OUT' -Force" >/dev/null
else
  OUT_FILE="$OUT_DIR/$NAME.tar.gz"
  tar -czf "$OUT_FILE" "$NAME"
  echo "[提示] 未找到 zip，已改用 tar.gz（微信开发者工具请直接导入源码目录）"
fi

rm -rf "$STAGE"

SIZE="$(du -h "$OUT_FILE" | cut -f1)"
echo "==> 打包完成：$OUT_FILE ($SIZE)"
echo "    提审前请确认：utils/config.js 的 BASE_URL 已改为生产域名；project.config.json 已填正式 AppID"
