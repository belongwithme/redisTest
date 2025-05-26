#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Markdown到Anki导入工具 - Python实现
将Cursor中的Markdown内容导入到Anki中
"""

import json
import re
import sys
import urllib.request
import urllib.parse
import urllib.error
from typing import List, Dict, Any, Optional
import markdown
from bs4 import BeautifulSoup
import logging

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class AnkiConnector:
    """AnkiConnect API连接器"""
    
    def __init__(self, url: str = "http://localhost:8765"):
        self.url = url
    
    def request(self, action: str, params: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """发送请求到AnkiConnect"""
        request_data = {
            "action": action,
            "version": 6,
            "params": params or {}
        }
        
        try:
            request_json = json.dumps(request_data).encode('utf-8')
            request_obj = urllib.request.Request(self.url, request_json)
            request_obj.add_header('Content-Type', 'application/json')
            
            with urllib.request.urlopen(request_obj) as response:
                response_data = json.loads(response.read().decode('utf-8'))
                
            if response_data.get('error'):
                raise Exception(f"AnkiConnect错误: {response_data['error']}")
                
            return response_data.get('result')
            
        except urllib.error.URLError as e:
            raise Exception(f"无法连接到AnkiConnect。请确保Anki正在运行且AnkiConnect插件已安装。错误: {e}")
        except Exception as e:
            raise Exception(f"请求失败: {e}")
    
    def create_deck(self, deck_name: str) -> None:
        """创建牌组"""
        try:
            self.request("createDeck", {"deck": deck_name})
            logger.info(f"牌组 '{deck_name}' 创建成功或已存在")
        except Exception as e:
            logger.warning(f"创建牌组失败: {e}")
    
    def add_note(self, note_data: Dict[str, Any]) -> Optional[int]:
        """添加笔记到Anki"""
        try:
            # 检查是否可以添加笔记（避免重复）
            can_add = self.request("canAddNotes", {"notes": [note_data]})
            if not can_add[0]:
                logger.info(f"跳过重复卡片: {note_data['fields']['Front'][:50]}...")
                return None
            
            note_id = self.request("addNote", {"note": note_data})
            logger.info(f"成功添加卡片: {note_data['fields']['Front'][:50]}...")
            return note_id
            
        except Exception as e:
            logger.error(f"添加卡片失败: {e}")
            return None

class MarkdownParser:
    """Markdown解析器"""
    
    def __init__(self):
        self.md = markdown.Markdown(extensions=['extra', 'codehilite'])
    
    def parse_file(self, file_path: str) -> List[Dict[str, str]]:
        """解析Markdown文件，提取卡片内容"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
        except Exception as e:
            raise Exception(f"读取文件失败: {e}")
        
        cards = []
        
        # 按二级标题分割内容
        sections = re.split(r'^## (.+)$', content, flags=re.MULTILINE)
        
        # 第一个元素是标题前的内容，跳过
        for i in range(1, len(sections), 2):
            if i + 1 < len(sections):
                question = sections[i].strip()
                answer = sections[i + 1].strip()
                
                if question and answer:
                    # 检查是否是填空题
                    if self._is_cloze(answer):
                        cards.append({
                            'type': 'cloze',
                            'question': question,
                            'answer': answer
                        })
                    else:
                        cards.append({
                            'type': 'basic',
                            'question': question,
                            'answer': answer
                        })
        
        logger.info(f"从文件中解析出 {len(cards)} 张卡片")
        return cards
    
    def _is_cloze(self, text: str) -> bool:
        """检查文本是否包含填空语法"""
        return bool(re.search(r'\{\{c\d+::[^}]+\}\}', text))
    
    def render_markdown(self, text: str) -> str:
        """将Markdown文本渲染为HTML"""
        return self.md.convert(text)

class MarkdownToAnki:
    """主要的转换类"""
    
    def __init__(self, anki_url: str = "http://localhost:8765"):
        self.anki = AnkiConnector(anki_url)
        self.parser = MarkdownParser()
    
    def convert_file(self, file_path: str, deck_name: str = "Cursor导入") -> None:
        """转换Markdown文件到Anki"""
        logger.info(f"开始处理文件: {file_path}")
        
        # 创建牌组
        self.anki.create_deck(deck_name)
        
        # 解析Markdown文件
        cards = self.parser.parse_file(file_path)
        
        if not cards:
            logger.warning("没有找到任何卡片内容")
            return
        
        # 添加卡片到Anki
        success_count = 0
        for card in cards:
            if self._add_card_to_anki(card, deck_name):
                success_count += 1
        
        logger.info(f"成功导入 {success_count}/{len(cards)} 张卡片到牌组 '{deck_name}'")
    
    def _add_card_to_anki(self, card: Dict[str, str], deck_name: str) -> bool:
        """添加单张卡片到Anki"""
        try:
            if card['type'] == 'cloze':
                # 填空题
                note_data = {
                    "deckName": deck_name,
                    "modelName": "Cloze",
                    "fields": {
                        "Text": self.parser.render_markdown(card['answer']),
                        "Extra": self.parser.render_markdown(card['question'])
                    },
                    "tags": ["cursor-import", "markdown"]
                }
            else:
                # 基础问答题
                note_data = {
                    "deckName": deck_name,
                    "modelName": "Basic",
                    "fields": {
                        "Front": self.parser.render_markdown(card['question']),
                        "Back": self.parser.render_markdown(card['answer'])
                    },
                    "tags": ["cursor-import", "markdown"]
                }
            
            result = self.anki.add_note(note_data)
            return result is not None
            
        except Exception as e:
            logger.error(f"添加卡片失败: {e}")
            return False

def main():
    """主函数"""
    if len(sys.argv) != 2:
        print("使用方法: python markdown_to_anki.py <markdown文件路径>")
        print("示例: python markdown_to_anki.py example.md")
        sys.exit(1)
    
    file_path = sys.argv[1]
    
    try:
        converter = MarkdownToAnki()
        converter.convert_file(file_path)
        print("转换完成！请检查Anki中的卡片。")
        
    except Exception as e:
        logger.error(f"转换失败: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main() 