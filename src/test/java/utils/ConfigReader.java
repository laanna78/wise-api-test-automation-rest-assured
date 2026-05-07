package utils;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    static {
        try {
            properties = new Properties();
            FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        String value = System.getenv(key.toUpperCase());
        if (value == null) {
            value = System.getenv(key);
        }
        if (value == null && properties != null) {
            value = properties.getProperty(key);
        }
        return value;
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream("src/test/resources/config.properties")) {
            properties.store(fos, "Frissítve a következő tesztfutás során");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
