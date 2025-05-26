package AI实验.Notion同步数据.NotionTransform.src.main.java.org.example;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Notion文件夹同步工具 - Java版本
 *
 * @author NotionSync
 * @version 1.0
 */
public class NotionSync {

    private static final String NOTION_API_VERSION = "2022-06-28";
    private static final String NOTION_BASE_URL = "https://api.notion.com/v1";
    private static final int MAX_BLOCK_SIZE = 2000;
    private static final long API_DELAY_MS = 300;

    private final String notionToken;
    private final String databaseId;
    private final OkHttpClient httpClient;
    private final Gson gson;

    // 支持的文件扩展名
    private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(Arrays.asList(
        ".md", ".txt", ".java", ".py", ".js", ".ts", ".html", ".css",
        ".json", ".xml", ".yaml", ".yml", ".properties", ".sql"
    ));

    /**
     * 构造函数
     *
     * @param notionToken Notion Integration Token
     * @param databaseId 目标数据库ID
     */
    public NotionSync(String notionToken, String databaseId) {
        this.notionToken = notionToken;
        this.databaseId = databaseId;
        this.gson = new Gson();

        // 配置HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    /**
     * 创建Notion页面
     *
     * @param parentId 父页面ID或数据库ID
     * @param title 页面标题
     * @param content 页面内容
     * @param filePath 文件路径
     * @param folderPath 文件夹路径
     * @return 创建的页面ID，失败返回null
     */
    public String createPage(String parentId, String title, String content,
                           String filePath, String folderPath) {
        try {
            JsonObject requestBody = new JsonObject();

            // 设置父级
            JsonObject parent = new JsonObject();
            if (parentId.equals(databaseId)) {
                parent.addProperty("database_id", parentId);
            } else {
                parent.addProperty("page_id", parentId);
            }
            requestBody.add("parent", parent);

            // 设置属性
            JsonObject properties = new JsonObject();

            // 标题属性
            JsonObject titleProp = new JsonObject();
            JsonArray titleArray = new JsonArray();
            JsonObject titleText = new JsonObject();
            JsonObject titleContent = new JsonObject();
            titleContent.addProperty("content", title);
            titleText.add("text", titleContent);
            titleArray.add(titleText);
            titleProp.add("title", titleArray);
            properties.add("title", titleProp);

            // 如果是数据库，添加额外属性
            if (parentId.equals(databaseId)) {
                // 文件路径
                properties.add("文件路径", createRichTextProperty(filePath));

                // 文件夹
                properties.add("文件夹", createRichTextProperty(folderPath));

                // 同步时间
                JsonObject dateProp = new JsonObject();
                JsonObject dateObj = new JsonObject();
                dateObj.addProperty("start", LocalDateTime.now().format(
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                dateProp.add("date", dateObj);
                properties.add("同步时间", dateProp);
            }

            requestBody.add("properties", properties);

            // 发送请求
            RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                .url(NOTION_BASE_URL + "/pages")
                .addHeader("Authorization", "Bearer " + notionToken)
                .addHeader("Notion-Version", NOTION_API_VERSION)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    JsonObject responseJson = gson.fromJson(
                        response.body().string(), JsonObject.class);
                    String pageId = responseJson.get("id").getAsString();
                    System.out.println("✅ 创建页面成功: " + title);

                    // 如果有内容，添加内容块
                    if (content != null && !content.isEmpty()) {
                        addContentToPage(pageId, content);
                    }

                    return pageId;
                } else {
                    System.err.println("❌ 创建页面失败: " + title);
                    System.err.println("   错误信息: " + response.body().string());
                    return null;
                }
            }

        } catch (Exception e) {
            System.err.println("❌ 创建页面异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 创建富文本属性
     */
    private JsonObject createRichTextProperty(String text) {
        JsonObject prop = new JsonObject();
        JsonArray richTextArray = new JsonArray();
        JsonObject richText = new JsonObject();
        JsonObject textObj = new JsonObject();
        textObj.addProperty("content", text != null ? text : "");
        richText.add("text", textObj);
        richTextArray.add(richText);
        prop.add("rich_text", richTextArray);
        return prop;
    }

    /**
     * 向页面添加内容
     *
     * @param pageId 页面ID
     * @param content 内容（Markdown格式）
     */
    public void addContentToPage(String pageId, String content) {
        try {
            List<JsonObject> blocks = markdownToNotionBlocks(content);

            // 分批添加块（每次最多100个）
            for (int i = 0; i < blocks.size(); i += 100) {
                List<JsonObject> batch = blocks.subList(i,
                    Math.min(i + 100, blocks.size()));

                JsonObject requestBody = new JsonObject();
                JsonArray children = new JsonArray();
                batch.forEach(children::add);
                requestBody.add("children", children);

                RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                    .url(NOTION_BASE_URL + "/blocks/" + pageId + "/children")
                    .addHeader("Authorization", "Bearer " + notionToken)
                    .addHeader("Notion-Version", NOTION_API_VERSION)
                    .addHeader("Content-Type", "application/json")
                    .patch(body)
                    .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        System.err.println("❌ 添加内容失败: " + response.body().string());
                    }
                }

                // 避免速率限制
                Thread.sleep(API_DELAY_MS);
            }

        } catch (Exception e) {
            System.err.println("❌ 添加内容异常: " + e.getMessage());
        }
    }

    /**
     * 将Markdown内容转换为Notion块
     *
     * @param markdownContent Markdown内容
     * @return Notion块列表
     */
    private List<JsonObject> markdownToNotionBlocks(String markdownContent) {
        List<JsonObject> blocks = new ArrayList<>();
        String[] lines = markdownContent.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // 一级标题
            if (line.startsWith("# ")) {
                blocks.add(createHeadingBlock(line.substring(2).trim(), 1));
            }
            // 二级标题
            else if (line.startsWith("## ")) {
                blocks.add(createHeadingBlock(line.substring(3).trim(), 2));
            }
            // 三级标题
            else if (line.startsWith("### ")) {
                blocks.add(createHeadingBlock(line.substring(4).trim(), 3));
            }
            // 代码块
            else if (line.startsWith("```")) {
                String language = line.length() > 3 ? line.substring(3).trim() : "plain text";
                StringBuilder codeContent = new StringBuilder();
                i++;

                while (i < lines.length && !lines[i].startsWith("```")) {
                    if (codeContent.length() > 0) {
                        codeContent.append("\n");
                    }
                    codeContent.append(lines[i]);
                    i++;
                }

                // 处理长代码块
                String code = codeContent.toString();
                if (code.length() <= MAX_BLOCK_SIZE) {
                    blocks.add(createCodeBlock(code, language));
                } else {
                    // 分割长代码块
                    int chunks = (int) Math.ceil((double) code.length() / MAX_BLOCK_SIZE);
                    for (int j = 0; j < chunks; j++) {
                        int start = j * MAX_BLOCK_SIZE;
                        int end = Math.min(start + MAX_BLOCK_SIZE, code.length());
                        String chunk = code.substring(start, end);
                        blocks.add(createCodeBlock(chunk, language,
                            String.format("Part %d/%d", j + 1, chunks)));
                    }
                }
            }
            // 无序列表
            else if (line.startsWith("- ") || line.startsWith("* ")) {
                blocks.add(createBulletedListItem(line.substring(2).trim()));
            }
            // 有序列表
            else if (line.matches("^\\d+\\.\\s+.*")) {
                String content = line.replaceFirst("^\\d+\\.\\s+", "");
                blocks.add(createNumberedListItem(content));
            }
            // 引用
            else if (line.startsWith("> ")) {
                blocks.add(createQuoteBlock(line.substring(2).trim()));
            }
            // 普通段落
            else if (!line.trim().isEmpty()) {
                if (line.length() <= MAX_BLOCK_SIZE) {
                    blocks.add(createParagraphBlock(line));
                } else {
                    // 分割长段落
                    for (int j = 0; j < line.length(); j += MAX_BLOCK_SIZE) {
                        int end = Math.min(j + MAX_BLOCK_SIZE, line.length());
                        blocks.add(createParagraphBlock(line.substring(j, end)));
                    }
                }
            }
        }

        return blocks;
    }

    /**
     * 创建标题块
     */
    private JsonObject createHeadingBlock(String text, int level) {
        JsonObject block = new JsonObject();
        String type = "heading_" + level;
        block.addProperty("type", type);

        JsonObject heading = new JsonObject();
        heading.add("rich_text", createRichTextArray(text));
        block.add(type, heading);

        return block;
    }

    /**
     * 创建代码块
     */
    private JsonObject createCodeBlock(String code, String language) {
        return createCodeBlock(code, language, null);
    }

    private JsonObject createCodeBlock(String code, String language, String caption) {
        JsonObject block = new JsonObject();
        block.addProperty("type", "code");

        JsonObject codeObj = new JsonObject();
        codeObj.add("rich_text", createRichTextArray(code));
        codeObj.addProperty("language", language);

        if (caption != null) {
            codeObj.add("caption", createRichTextArray(caption));
        }

        block.add("code", codeObj);
        return block;
    }

    /**
     * 创建段落块
     */
    private JsonObject createParagraphBlock(String text) {
        JsonObject block = new JsonObject();
        block.addProperty("type", "paragraph");

        JsonObject paragraph = new JsonObject();
        paragraph.add("rich_text", createRichTextArray(text));
        block.add("paragraph", paragraph);

        return block;
    }

    /**
     * 创建无序列表项
     */
    private JsonObject createBulletedListItem(String text) {
        JsonObject block = new JsonObject();
        block.addProperty("type", "bulleted_list_item");

        JsonObject item = new JsonObject();
        item.add("rich_text", createRichTextArray(text));
        block.add("bulleted_list_item", item);

        return block;
    }

    /**
     * 创建有序列表项
     */
    private JsonObject createNumberedListItem(String text) {
        JsonObject block = new JsonObject();
        block.addProperty("type", "numbered_list_item");

        JsonObject item = new JsonObject();
        item.add("rich_text", createRichTextArray(text));
        block.add("numbered_list_item", item);

        return block;
    }

    /**
     * 创建引用块
     */
    private JsonObject createQuoteBlock(String text) {
        JsonObject block = new JsonObject();
        block.addProperty("type", "quote");

        JsonObject quote = new JsonObject();
        quote.add("rich_text", createRichTextArray(text));
        block.add("quote", quote);

        return block;
    }

    /**
     * 创建富文本数组
     */
    private JsonArray createRichTextArray(String text) {
        JsonArray array = new JsonArray();
        JsonObject richText = new JsonObject();
        JsonObject textObj = new JsonObject();
        textObj.addProperty("content", text);
        richText.add("text", textObj);
        array.add(richText);
        return array;
    }

    /**
     * 同步文件夹
     *
     * @param folderPath 文件夹路径
     * @param parentId 父页面ID（可选）
     */
    public void syncFolder(String folderPath, String parentId) {
        if (parentId == null) {
            parentId = databaseId;
        }

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("❌ 文件夹不存在: " + folderPath);
            return;
        }

        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            // 跳过隐藏文件
            if (file.getName().startsWith(".")) {
                continue;
            }

            try {
                if (file.isFile()) {
                    // 同步文件
                    syncFile(file, parentId, folderPath);
                } else if (file.isDirectory()) {
                    // 为子文件夹创建页面
                    String folderPageId = createPage(
                        parentId,
                        "📁 " + file.getName(),
                        null,
                        null,
                        folderPath
                    );

                    if (folderPageId != null) {
                        System.out.println("📂 进入文件夹: " + file.getName());
                        syncFolder(file.getAbsolutePath(), folderPageId);
                    }
                }

                // 避免API速率限制
                Thread.sleep(API_DELAY_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("❌ 同步被中断");
                return;
            }
        }
    }

    /**
     * 同步单个文件
     *
     * @param file 文件对象
     * @param parentId 父页面ID
     * @param folderPath 所在文件夹路径
     */
    private void syncFile(File file, String parentId, String folderPath) {
        String fileName = file.getName();
        String extension = "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = fileName.substring(lastDot).toLowerCase();
        }

        // 检查是否支持的文件类型
        if (SUPPORTED_EXTENSIONS.contains(extension)) {
            try {
                // 读取文件内容
                String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");

                // 创建页面
                createPage(
                    parentId,
                    fileName,
                    content,
                    file.getAbsolutePath(),
                    folderPath
                );

            } catch (IOException e) {
                System.err.println("❌ 读取文件失败: " + file.getAbsolutePath());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️  跳过不支持的文件类型: " + fileName);
        }
    }

    /**
     * 主函数
     */
    public static void main(String[] args) {
        System.out.println("🚀 Notion文件夹同步工具 - Java版");
        System.out.println("=".repeat(50));

        // 配置信息
        String NOTION_TOKEN = "your_notion_integration_token";  // 需要替换
        String DATABASE_ID = "your_database_id";  // 需要替换
        String LOCAL_FOLDER = "D:\\准备";  // 本地文件夹路径

        // 检查配置
        if (NOTION_TOKEN.equals("your_notion_integration_token")) {
            System.err.println("❌ 请先配置Notion Integration Token!");
            System.out.println("\n获取步骤：");
            System.out.println("1. 访问 https://www.notion.so/my-integrations");
            System.out.println("2. 创建新的Integration");
            System.out.println("3. 复制Secret Token");
            return;
        }

        if (DATABASE_ID.equals("your_database_id")) {
            System.err.println("❌ 请先配置目标数据库ID!");
            System.out.println("\n获取步骤：");
            System.out.println("1. 在Notion中创建一个数据库");
            System.out.println("2. 分享给你的Integration");
            System.out.println("3. 复制数据库ID（URL中的32位字符串）");
            return;
        }

        // 创建同步器
        NotionSync syncer = new NotionSync(NOTION_TOKEN, DATABASE_ID);

        // 开始同步
        System.out.println("\n📁 开始同步文件夹: " + LOCAL_FOLDER);
        System.out.println("📍 目标数据库ID: " + DATABASE_ID);
        System.out.println("-".repeat(50));

        try {
            syncer.syncFolder(LOCAL_FOLDER, null);
            System.out.println("\n✅ 同步完成!");
        } catch (Exception e) {
            System.err.println("\n❌ 同步失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
