#!/usr/bin/env bash
# SQLite 在线一致性备份（容器内外均可）：./backup_db.sh [输出目录]
set -euo pipefail
OUT="${1:-./backups}"; mkdir -p "$OUT"
TS=$(date +%Y%m%d_%H%M%S)
if docker ps --format '{{.Names}}' | grep -q '^greencycle-api$'; then
  docker exec greencycle-api python3 -c "import sqlite3;s=sqlite3.connect('/data/green_cycle.db');d=sqlite3.connect('/data/backup_$TS.db');s.backup(d);d.close()"
  docker cp "greencycle-api:/data/backup_$TS.db" "$OUT/green_cycle_$TS.db"
  docker exec greencycle-api rm -f "/data/backup_$TS.db"
else
  python3 -c "import sqlite3,os;s=sqlite3.connect(os.environ.get('GREEN_CYCLE_DB','green_cycle.db'));d=sqlite3.connect('$OUT/green_cycle_$TS.db');s.backup(d);d.close()"
fi
gzip -f "$OUT/green_cycle_$TS.db"
echo "✅ 备份完成: $OUT/green_cycle_$TS.db.gz"
# 保留最近 14 份
ls -1t "$OUT"/green_cycle_*.db.gz 2>/dev/null | tail -n +15 | xargs -r rm -f
