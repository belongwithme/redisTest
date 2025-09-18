#!/bin/bash

# MinIO 初始化脚本
# 创建存储桶和设置策略

echo "开始初始化 MinIO..."

# 等待 MinIO 启动
echo "等待 MinIO 服务启动..."
while ! curl -f http://localhost:9000/minio/health/live 2>/dev/null; do
    echo "等待 MinIO 启动中..."
    sleep 2
done

echo "MinIO 服务已启动，开始创建存储桶..."

# 使用 mc (MinIO Client) 配置别名
docker run --rm --net=host minio/mc:latest alias set myminio http://localhost:9000 minioadmin minioadmin

# 创建文件存储桶
docker run --rm --net=host minio/mc:latest mb myminio/file-storage

# 设置存储桶策略为公开读取
docker run --rm --net=host minio/mc:latest policy set public myminio/file-storage

echo "MinIO 初始化完成！"
echo "访问地址："
echo "  MinIO 控制台: http://localhost:9001"
echo "  用户名: minioadmin"
echo "  密码: minioadmin"
echo "  默认存储桶: file-storage"