#!/usr/bin/env bash
# 启动绿循环后端服务
cd "$(dirname "$0")"
pip install -r requirements.txt -q
exec uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
