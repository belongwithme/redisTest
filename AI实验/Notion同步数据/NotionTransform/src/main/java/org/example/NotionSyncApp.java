package AI实验.Notion同步数据.NotionTransform.src.main.java.org.example;

import org.example.NotionConfig;

/**
 * Notion同步工具主应用类
 */
public class NotionSyncApp {

    public static void main(String[] args) {
        System.out.println("🚀 Notion文件夹同步工具 - Java版");
        System.out.println("=".repeat(50));

        // 加载配置
        NotionConfig config = NotionConfig.loadFromFile();

        // 验证配置
        if (!config.isValid()) {
            System.out.println("\n请按照以下步骤配置：");
            System.out.println("1. 编辑 notion-sync.properties 文件");
            System.out.println("2. 填入你的Notion Token和数据库ID");
            System.out.println("3. 确认本地文件夹路径");
            System.out.println("4. 重新运行程序");
            return;
        }

        // 创建同步器
        NotionSync syncer = new NotionSync(
                config.getNotionToken(),
                config.getDatabaseId()
        );

        // 显示配置信息
        System.out.println("\n📋 配置信息：");
        System.out.println("   本地文件夹: " + config.getLocalFolder());
        System.out.println("   数据库ID: " + config.getDatabaseId().substring(0, 8) + "...");
        System.out.println("   API延迟: " + config.getApiDelayMs() + "ms");
        System.out.println("   最大文件大小: " + config.getMaxFileSizeMb() + "MB");

        // 开始同步
        System.out.println("\n📁 开始同步...");
        System.out.println("-".repeat(50));

        long startTime = System.currentTimeMillis();

        try {
            syncer.syncFolder(config.getLocalFolder(), null);

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;

            System.out.println("\n✅ 同步完成!");
            System.out.println("   总耗时: " + duration + " 秒");

        } catch (Exception e) {
            System.err.println("\n❌ 同步失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
