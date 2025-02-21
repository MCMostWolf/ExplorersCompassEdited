package com.chaosthedude.explorerscompass.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class BetterUI {

    private static List<Entry<String, Integer>> entries;

    public static void loadConfig(String modId) {
        Gson gson = new Gson();
        Path configDir = FMLPaths.CONFIGDIR.get().resolve(modId);
        Path configPath = configDir.resolve("config.json");

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            if (!Files.exists(configPath)) {
                System.out.println("Config file does not exist, creating a default one.");
                createDefaultConfig(configPath);
            }

            String content = new String(Files.readAllBytes(configPath), "UTF-8");
            List<Map<String, Object>> jsonEntries = gson.fromJson(content, new TypeToken<List<Map<String, Object>>>() {}.getType());
            entries = jsonEntries.stream()
                    .map(entry -> new AbstractMap.SimpleEntry<>(
                            (String) entry.get("key"),
                            ((Number) entry.get("value")).intValue()
                    ))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            entries = new ArrayList<>();
        }
    }

    private static void createDefaultConfig(Path configPath) throws IOException {
        String defaultContent = "[{\"key\":\"minecraft:village_plains\",\"value\":1}]"; // 示例内容
        Files.write(configPath, defaultContent.getBytes());
    }

    public static List<Entry<String, Integer>> getEntries() {
        return entries;
    }
}