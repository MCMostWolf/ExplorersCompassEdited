package com.chaosthedude.explorerscompass.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class BetterUI {

    private static List<Entry<String, Integer>> entries;

    public static void loadConfig(String modId) {
        Gson gson = new Gson();
        Path configDir = FMLPaths.CONFIGDIR.get().resolve(modId); // 获取模组的配置目录
        Path configPath = configDir.resolve("config.json"); // 构建配置文件路径

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir); // 如果目录不存在，则创建
            }
            if (!Files.exists(configPath)) {
                // 如果配置文件不存在，可以创建一个默认的配置文件或记录日志
                System.out.println("Config file does not exist, creating a default one.");
                createDefaultConfig(configPath);
            }

            String content = Files.readString(configPath);
            List<Map<String, Object>> jsonEntries = gson.fromJson(content, new TypeToken<List<Map<String, Object>>>() {}.getType());
            entries = jsonEntries.stream()
                    .map(entry -> Map.entry(
                            (String) entry.get("key"),
                            ((Number) entry.get("value")).intValue()
                    ))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            entries = new ArrayList<>(); // 提供一个空列表作为默认值
        }
    }

    private static void createDefaultConfig(Path configPath) throws IOException {
        // 创建默认配置文件的内容
        String defaultContent = "[{\"key\":\"minecraft:village_plains\",\"value\":1}]"; // 示例内容
        Files.writeString(configPath, defaultContent);
    }

    public static List<Entry<String, Integer>> getEntries() {
        return entries;
    }
}