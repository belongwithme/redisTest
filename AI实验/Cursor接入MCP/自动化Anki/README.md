# Cursor Markdown 到 Anki 导入工具

这个项目提供了两种方式将Cursor中的Markdown内容导入到Anki中：Java实现和Python实现。

## 前提条件

1. **安装Anki软件**
2. **安装AnkiConnect插件**
   - 在Anki中，选择 `工具` > `插件` > `获取插件`
   - 输入代码：`2055492159`
   - 重启Anki
   - 确保Anki在后台运行，访问 `http://localhost:8765` 验证AnkiConnect是否正常工作

## 支持的Markdown格式

### 基础问答卡片
```markdown
## 问题标题
答案内容可以包含多行
支持**粗体**、*斜体*等格式
```

### 填空题（Cloze）
```markdown
## 填空题示例
{{c1::罗马}}是意大利的首都。
这里有{{c2::多个}}填空{{c2::内容}}。
```

## Python实现（推荐）

### 方式一：完整版本（需要安装依赖）
```bash
# 安装依赖
pip install requests markdown beautifulsoup4

# 使用
python markdown_to_anki.py input.md
```

### 方式二：简化版本（无需外部依赖）
```bash
# 直接使用，仅需Python标准库
python simple_markdown_to_anki.py input.md
```

## Java实现

### 编译和运行
```bash
# 编译
javac -cp ".:lib/*" MarkdownToAnki.java

# 运行
java -cp ".:lib/*" MarkdownToAnki input.md
```

### 依赖库
- Jackson (JSON处理)
- OkHttp (HTTP客户端)

## 功能特性

- ✅ 支持基础问答卡片
- ✅ 支持填空题（Cloze）
- ✅ 支持Markdown格式渲染
- ✅ 自动创建牌组
- ✅ 避免重复导入
- ✅ 支持中文内容
- ✅ 错误处理和日志记录

## 示例Markdown文件

参见 `example.md` 文件，包含了各种类型的卡片示例。

## 注意事项

1. 确保Anki在运行且AnkiConnect插件已启用
2. Markdown文件需要使用UTF-8编码
3. 每个二级标题（##）会被识别为一张卡片的问题
4. 支持嵌套的Markdown语法，如列表、代码块等

## 故障排除

1. **连接失败**：确保Anki正在运行且AnkiConnect插件已安装
2. **中文乱码**：确保Markdown文件使用UTF-8编码
3. **重复卡片**：程序会自动检查并避免创建重复卡片

## 扩展功能

可以根据需要扩展以下功能：
- 支持图片和音频
- 自定义卡片模板
- 批量处理多个文件
- 与Cursor编辑器集成 