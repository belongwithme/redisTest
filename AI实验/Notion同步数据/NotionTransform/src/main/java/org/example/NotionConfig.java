package AI实验.Notion同步数据.NotionTransform.src.main.java.org.example;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Notion同步配置管理类
 */
public class NotionConfig {

    private static final String CONFIG_FILE = "notion-sync.properties";

    private String notionToken;
    private String databaseId;
    private String localFolder;
    private long apiDelayMs = 300;
    private int maxFileSizeMb = 10;
    private boolean verbose = true;

    /**
     * 从配置文件加载配置
     */
    public static NotionConfig loadFromFile() {
        NotionConfig config = new NotionConfig();
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);

            config.notionToken = props.getProperty("notion.token", "");
            config.databaseId = props.getProperty("notion.database.id", "");
            config.localFolder = props.getProperty("local.folder", "D:\\准备");
            config.apiDelayMs = Long.parseLong(props.getProperty("api.delay.ms", "300"));
            config.maxFileSizeMb = Integer.parseInt(props.getProperty("max.file.size.mb", "10"));
            config.verbose = Boolean.parseBoolean(props.getProperty("verbose", "true"));

            System.out.println("✅ 配置文件加载成功: " + CONFIG_FILE);

        } catch (IOException e) {
            System.out.println("⚠️  配置文件不存在，将创建默认配置文件");
            config.createDefaultConfigFile();
        } catch (Exception e) {
            System.err.println("❌ 加载配置文件失败: " + e.getMessage());
        }

        return config;
    }

    /**
     * 创建默认配置文件
     */
    private void createDefaultConfigFile() {
        Properties props = new Properties();

        props.setProperty("# Notion API配置", "");
        props.setProperty("notion.token", "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");
        props.setProperty("notion.database.id", "1ff3b97b107f800eb239f65e766225af");

        props.setProperty("# 本地文件夹配置", "");
        props.setProperty("local.folder", "D:\\准备/Java/JUC/Fork&&join框架");

        props.setProperty("# 高级配置", "");
        props.setProperty("api.delay.ms", "300");
        props.setProperty("max.file.size.mb", "10");
        props.setProperty("verbose", "true");

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Notion Sync Configuration File\n" +
                "请修改此文件中的配置信息后重新运行程序");
            System.out.println("✅ 已创建默认配置文件: " + CONFIG_FILE);
            System.out.println("   请编辑配置文件后重新运行程序");
        } catch (IOException e) {
            System.err.println("❌ 创建配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 验证配置是否有效
     */
    public boolean isValid() {
        if (notionToken == null || notionToken.isEmpty() ||
            notionToken.equals("your_notion_integration_token")) {
            System.err.println("❌ 请配置有效的Notion Token!");
            return false;
        }

        if (databaseId == null || databaseId.isEmpty() ||
            databaseId.equals("your_database_id")) {
            System.err.println("❌ 请配置有效的数据库ID!");
            return false;
        }

        return true;
    }

    // Getter方法
    public String getNotionToken() {
        return notionToken;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public String getLocalFolder() {
        return localFolder;
    }

    public long getApiDelayMs() {
        return apiDelayMs;
    }

    public int getMaxFileSizeMb() {
        return maxFileSizeMb;
    }

    public boolean isVerbose() {
        return verbose;
    }
}
