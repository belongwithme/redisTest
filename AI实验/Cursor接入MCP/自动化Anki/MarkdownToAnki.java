package AI实验.Cursor接入MCP.自动化Anki;
import java.io.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Markdown到Anki导入工具 - Java实现
 * 将Cursor中的Markdown内容导入到Anki中
 * 注意：此版本使用原生Java，不依赖外部库
 */
public class MarkdownToAnki {
    
    private static final String ANKI_CONNECT_URL = "http://localhost:8765";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    
    /**
     * 简单的JSON构建器
     */
    static class JsonBuilder {
        private StringBuilder sb = new StringBuilder();
        
        public JsonBuilder() {
            sb.append("{");
        }
        
        public JsonBuilder add(String key, String value) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"").append(key).append("\":\"").append(escapeJson(value)).append("\"");
            return this;
        }
        
        public JsonBuilder add(String key, int value) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"").append(key).append("\":").append(value);
            return this;
        }
        
        public JsonBuilder addObject(String key, String objectJson) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"").append(key).append("\":").append(objectJson);
            return this;
        }
        
        public JsonBuilder addArray(String key, String arrayJson) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"").append(key).append("\":").append(arrayJson);
            return this;
        }
        
        public String build() {
            sb.append("}");
            return sb.toString();
        }
        
        private String escapeJson(String str) {
            return str.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("\t", "\\t");
        }
    }
    
    /**
     * AnkiConnect API连接器
     */
    static class AnkiConnector {
        private final String url;
        
        public AnkiConnector(String url) {
            this.url = url;
        }
        
        /**
         * 发送请求到AnkiConnect
         */
        public String request(String action, String params) throws Exception {
            String requestJson = new JsonBuilder()
                .add("action", action)
                .add("version", 6)
                .addObject("params", params != null ? params : "{}")
                .build();
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                .build();
            
            try {
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                
                String responseBody = response.body();
                
                // 简单的错误检查
                if (responseBody.contains("\"error\":") && !responseBody.contains("\"error\":null")) {
                    throw new Exception("AnkiConnect错误: " + responseBody);
                }
                
                return responseBody;
                
            } catch (Exception e) {
                throw new Exception("无法连接到AnkiConnect。请确保Anki正在运行且AnkiConnect插件已安装。错误: " + e.getMessage());
            }
        }
        
        /**
         * 创建牌组
         */
        public void createDeck(String deckName) {
            try {
                String params = new JsonBuilder().add("deck", deckName).build();
                request("createDeck", params);
                System.out.println("牌组 '" + deckName + "' 创建成功或已存在");
            } catch (Exception e) {
                System.err.println("创建牌组失败: " + e.getMessage());
            }
        }
        
        /**
         * 添加笔记到Anki
         */
        public boolean addNote(String noteJson) {
            try {
                // 检查是否可以添加笔记（避免重复）
                String canAddParams = new JsonBuilder()
                    .addArray("notes", "[" + noteJson + "]")
                    .build();
                
                String canAddResponse = request("canAddNotes", canAddParams);
                if (canAddResponse.contains("[false]")) {
                    System.out.println("跳过重复卡片");
                    return false;
                }
                
                String addParams = new JsonBuilder()
                    .addObject("note", noteJson)
                    .build();
                
                String result = request("addNote", addParams);
                System.out.println("成功添加卡片");
                return true;
                
            } catch (Exception e) {
                System.err.println("添加卡片失败: " + e.getMessage());
                return false;
            }
        }
    }
    
    /**
     * 卡片数据类
     */
    static class Card {
        private final String type;
        private final String question;
        private final String answer;
        
        public Card(String type, String question, String answer) {
            this.type = type;
            this.question = question;
            this.answer = answer;
        }
        
        public String getType() { return type; }
        public String getQuestion() { return question; }
        public String getAnswer() { return answer; }
    }
    
    /**
     * Markdown解析器
     */
    static class MarkdownParser {
        
        /**
         * 解析Markdown文件，提取卡片内容
         */
        public List<Card> parseFile(String filePath) throws Exception {
            String content;
            try {
                content = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new Exception("读取文件失败: " + e.getMessage());
            }
            
            List<Card> cards = new ArrayList<>();
            
            // 按二级标题分割内容
            Pattern pattern = Pattern.compile("^## (.+)$", Pattern.MULTILINE);
            String[] sections = pattern.split(content);
            Matcher matcher = pattern.matcher(content);
            
            List<String> questions = new ArrayList<>();
            while (matcher.find()) {
                questions.add(matcher.group(1).trim());
            }
            
            // 处理每个部分
            for (int i = 0; i < questions.size() && i + 1 < sections.length; i++) {
                String question = questions.get(i);
                String answer = sections[i + 1].trim();
                
                if (!question.isEmpty() && !answer.isEmpty()) {
                    // 检查是否是填空题
                    if (isCloze(answer)) {
                        cards.add(new Card("cloze", question, answer));
                    } else {
                        cards.add(new Card("basic", question, answer));
                    }
                }
            }
            
            System.out.println("从文件中解析出 " + cards.size() + " 张卡片");
            return cards;
        }
        
        /**
         * 检查文本是否包含填空语法
         */
        private boolean isCloze(String text) {
            Pattern clozePattern = Pattern.compile("\\{\\{c\\d+::[^}]+\\}\\}");
            return clozePattern.matcher(text).find();
        }
        
        /**
         * 简单的Markdown到HTML转换
         */
        public String renderMarkdown(String text) {
            // 基础的Markdown转换
            text = text.replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>"); // 粗体
            text = text.replaceAll("\\*(.+?)\\*", "<em>$1</em>"); // 斜体
            text = text.replaceAll("`(.+?)`", "<code>$1</code>"); // 行内代码
            text = text.replaceAll("\\n", "<br>"); // 换行
            
            // 处理列表
            text = text.replaceAll("(?m)^- (.+)$", "<li>$1</li>");
            text = text.replaceAll("(<li>.*</li>)", "<ul>$1</ul>");
            
            return text;
        }
    }
    
    private final AnkiConnector anki;
    private final MarkdownParser parser;
    
    public MarkdownToAnki() {
        this.anki = new AnkiConnector(ANKI_CONNECT_URL);
        this.parser = new MarkdownParser();
    }
    
    /**
     * 转换Markdown文件到Anki
     */
    public void convertFile(String filePath, String deckName) throws Exception {
        System.out.println("开始处理文件: " + filePath);
        
        // 创建牌组
        anki.createDeck(deckName);
        
        // 解析Markdown文件
        List<Card> cards = parser.parseFile(filePath);
        
        if (cards.isEmpty()) {
            System.out.println("没有找到任何卡片内容");
            return;
        }
        
        // 添加卡片到Anki
        int successCount = 0;
        for (Card card : cards) {
            if (addCardToAnki(card, deckName)) {
                successCount++;
            }
        }
        
        System.out.println("成功导入 " + successCount + "/" + cards.size() + " 张卡片到牌组 '" + deckName + "'");
    }
    
    /**
     * 添加单张卡片到Anki
     */
    private boolean addCardToAnki(Card card, String deckName) {
        try {
            String fieldsJson;
            String modelName;
            
            if ("cloze".equals(card.getType())) {
                // 填空题
                modelName = "Cloze";
                fieldsJson = new JsonBuilder()
                    .add("Text", parser.renderMarkdown(card.getAnswer()))
                    .add("Extra", parser.renderMarkdown(card.getQuestion()))
                    .build();
            } else {
                // 基础问答题
                modelName = "Basic";
                fieldsJson = new JsonBuilder()
                    .add("Front", parser.renderMarkdown(card.getQuestion()))
                    .add("Back", parser.renderMarkdown(card.getAnswer()))
                    .build();
            }
            
            String noteJson = new JsonBuilder()
                .add("deckName", deckName)
                .add("modelName", modelName)
                .addObject("fields", fieldsJson)
                .addArray("tags", "[\"cursor-import\",\"markdown\"]")
                .build();
            
            return anki.addNote(noteJson);
            
        } catch (Exception e) {
            System.err.println("添加卡片失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 主函数
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("使用方法: java MarkdownToAnki <markdown文件路径>");
            System.out.println("示例: java MarkdownToAnki example.md");
            System.exit(1);
        }
        
        String filePath = args[0];
        
        try {
            MarkdownToAnki converter = new MarkdownToAnki();
            converter.convertFile(filePath, "Cursor导入");
            System.out.println("转换完成！请检查Anki中的卡片。");
            
        } catch (Exception e) {
            System.err.println("转换失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
} 