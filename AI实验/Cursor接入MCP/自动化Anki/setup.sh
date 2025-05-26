#!/bin/bash

echo "正在安装Python依赖..."
pip install -r requirements.txt

echo ""
echo "安装完成！"
echo ""
echo "使用方法："
echo "  python markdown_to_anki.py example.md"
echo ""
echo "注意：请确保Anki正在运行且已安装AnkiConnect插件" 