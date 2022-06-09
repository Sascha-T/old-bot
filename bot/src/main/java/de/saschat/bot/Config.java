package de.saschat.bot;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Config {
    private File file;
    public ConfigData data;
    public Logger logger = LoggerFactory.getLogger("Config");

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

    public Config() throws IOException {
        this(new File("config.json"));
    }
    public Config(File file) throws IOException {
        this.file = file;
        load();
    }

    public void newConfig() throws IOException {
        data = new ConfigData();
        data.checkDefaults();
        save();
    }
    public void load() throws IOException {
        if(!file.exists()) {
            logger.warn("Creating new config...");
            newConfig();
            System.exit(-1);
        }
        data = GSON.fromJson(new FileReader(file), ConfigData.class);
        data.checkDefaults();
    }
    public void save() throws IOException {
        data.checkDefaults();
        String data1 = GSON.toJson(data);
        FileWriter writer = new FileWriter(file);
        writer.write(data1);
        writer.flush();
        writer.close();
    }


    public String getToken() {
        return data.token;
    }

    public boolean getDebug() {
        return data.debug;
    }

    public List<Long> getAdmins() {
        return Arrays.stream(data.admins).boxed().collect(Collectors.toList());
    }
    public File getModulesFolder() {
        File file = new File(data.modulesFolder);
        file.mkdirs();
        return file;
    }
    public String[] getDisabledModules() {
        return data.modulesDisabled;
    }

    private List<File> getForceLoadedModulesFromEnv() {
        String pathSBOT = System.getenv("SBOT_FORCE_LOADED_MODULES");
        if(pathSBOT == null)
            return new LinkedList<>();
        String[] files = pathSBOT.split(";");
        List<File> ret = new LinkedList<>();
        Arrays.stream(files).forEach(a -> {
            if(new File(a).exists())
                ret.add(new File(a));
            else
                logger.warn("Found invalid module path in environment variable: " + a);
        });
        return ret;
    }
    public File[] getForceLoadedModules() {
        List<File> files = getForceLoadedModulesFromEnv();
        for (String a: data.modulesForced) {
            File b = new File(a);
            if(b.exists())
                files.add(b);
            else
                logger.warn("Could not find module jar " + b.getAbsolutePath());
        }
        return files.toArray(new File[0]);
    }

    public void addDisabledModule(String name) {
        List<String> collect = Arrays.stream(data.modulesDisabled).collect(Collectors.toList());
        collect.add(name);
        data.modulesDisabled = collect.toArray(new String[0]);
    }

    public static class ConfigData {
        @Expose
        public String token;
        @Expose
        public Boolean debug;
        @Expose
        public long[] admins;
        @Expose
        public String modulesFolder;
        @Expose
        public String[] modulesForced;
        @Expose
        public String[] modulesDisabled;

        public void checkDefaults() {
            token = setIfNull(token,"BOT TOKEN HERE");
            debug = setIfNull(debug,false);
            admins = setIfNull(admins, new long[0]);
            modulesFolder = setIfNull(modulesFolder, "modules");
            modulesForced = setIfNull(modulesForced, new String[0]);
            modulesDisabled = setIfNull(modulesDisabled, new String[0]);
        }
        public <T> T setIfNull(T object, T value2) {
            return object != null ? object : value2;
        }
    }
}
