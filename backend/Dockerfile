# syntax=docker/dockerfile:1.6
# 绿循环 Green Cycle 后端（FastAPI + SQLite）
# 构建：docker build -t greencycle-api:1.3.0 .
# 运行：见 docker-compose.yml / docker-compose.prod.yml

FROM python:3.12-slim AS base
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1

# ---- 依赖层（单独缓存） ----
FROM base AS deps
WORKDIR /app
COPY requirements.txt .
RUN pip install --prefix=/install -r requirements.txt

# ---- 运行层 ----
FROM base AS runtime
RUN apt-get update && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd -r app && useradd -r -g app -d /app -s /sbin/nologin app
WORKDIR /app
COPY --from=deps /install /usr/local
COPY app ./app
COPY static ./static
# 数据目录：SQLite 数据库 + 运行期上传文件，挂载为卷持久化
RUN mkdir -p /data && chown -R app:app /app /data
USER app

ENV GC_ENV=production \
    GREEN_CYCLE_DB=/data/green_cycle.db \
    GC_PORT=8000 \
    GC_WORKERS=1
# 注意：SQLite 下 GC_WORKERS 保持 1；换 PostgreSQL 后再增加 worker 数
EXPOSE 8000
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD curl -fsS http://127.0.0.1:${GC_PORT}/api/health || exit 1

CMD ["sh", "-c", "uvicorn app.main:app --host 0.0.0.0 --port ${GC_PORT} --workers ${GC_WORKERS} --proxy-headers --forwarded-allow-ips='*'"]
