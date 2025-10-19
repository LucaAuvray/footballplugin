package fr.codinbox.footballplugin.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

public class ConfigLoader {

    private static final Logger LOGGER = Bukkit.getLogger();

    public static void loadConfig(File configFile) {

        if(!configFile.exists())
            saveConfig(configFile);

        Gson gson = new Gson();

        try {
            JsonObject object = gson.fromJson(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8), JsonObject.class);
            for(Map.Entry<String, JsonElement> entry : object.entrySet()) {
                for(Field field : FootConfig.class.getDeclaredFields()) {
                    if(entry.getKey().equals(field.getName())) {
                        field.setAccessible(true);

                        Object value = null;

                        if(field.getType().isAssignableFrom(Integer.class))
                            value = entry.getValue().getAsInt();
                        else if(field.getType().isAssignableFrom(Double.class))
                            value = entry.getValue().getAsDouble();
                        else if(field.getType().isAssignableFrom(Float.class))
                            value = entry.getValue().getAsFloat();
                        else if(field.getType().isAssignableFrom(Long.class))
                            value = entry.getValue().getAsLong();
                        else if(field.getType().isAssignableFrom(String.class))
                            value = entry.getValue().getAsString();

                        field.set(null, value);
                    }
                }
            }

            saveConfig(configFile);

            LOGGER.info("[Football] Config loaded");
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    private static void saveConfig(File configFile) {

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        try {
            JsonObject object = new JsonObject();
            for (Field field : FootConfig.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    Object obj = field.get(null);
                    if(field.getType().isAssignableFrom(Integer.class))
                        object.addProperty(field.getName(), Integer.valueOf(field.get(null).toString()));
                    else if(field.getType().isAssignableFrom(Double.class))
                        object.addProperty(field.getName(), Double.valueOf(field.get(null).toString()));
                    else if(field.getType().isAssignableFrom(Float.class))
                        object.addProperty(field.getName(), Float.valueOf(field.get(null).toString()));
                    else if(field.getType().isAssignableFrom(Long.class))
                        object.addProperty(field.getName(), Long.valueOf(field.get(null).toString()));
                    else if(field.getType().isAssignableFrom(String.class))
                        object.addProperty(field.getName(), String.valueOf(field.get(null)));
                }
            }

            Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8);
            writer.write(gson.toJson(object));
            writer.flush();
            writer.close();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

    }

}
