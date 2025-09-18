#!/bin/bash

# 文件上传测试脚本

API_BASE="http://localhost:8080/api/files"

echo "=== 文件上传业务测试 ==="

# 1. 健康检查
echo "1. 检查服务健康状态..."
curl -s "${API_BASE}/health" | jq '.' || echo "服务未启动或jq未安装"
echo ""

# 2. 获取支持的文件类型
echo "2. 获取支持的文件类型..."
curl -s "${API_BASE}/supported-types" | jq '.supportedContentTypes' || echo "无法获取支持的文件类型"
echo ""

# 3. 创建测试文件
echo "3. 创建测试文件..."
echo "这是一个测试文件内容" > test-file.txt
echo "测试文件已创建: test-file.txt"
echo ""

# 4. 上传文件
echo "4. 上传测试文件..."
curl -X POST "${API_BASE}/upload" \
  -F "file=@test-file.txt" \
  -F "uploadUserId=test-user-123" \
  -F "businessTag=test" \
  -F "description=测试文件上传" \
  -F "enableAsyncProcess=true" \
  | jq '.' || echo "文件上传失败"
echo ""

echo "5. 如果需要手动触发处理，可以使用以下命令:"
echo "curl -X POST '${API_BASE}/process/file-storage/test/2024-xx-xx/test-file.txt?processType=SECURITY_SCAN&priority=10'"
echo ""

echo "=== 测试完成 ==="
echo "请检查："
echo "1. RabbitMQ 管理界面: http://localhost:15672 (admin/admin123)"
echo "2. MinIO 控制台: http://localhost:9001 (minioadmin/minioadmin)"
echo "3. 应用日志查看消息处理情况"

# 清理测试文件
rm -f test-file.txt