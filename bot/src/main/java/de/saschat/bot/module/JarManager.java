package de.saschat.bot.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import de.saschat.bot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class JarManager {
    public Logger logger = LoggerFactory.getLogger("JarManager");
    HashMap<String, ModulesData> JARS = new HashMap<>();
    ModuleManager manager;

    private final static Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public JarManager(ModuleManager man) {
        this.manager = man;
    }

    public void addFolder(File file) {
        try {
            logger.info("Loading folder: " + file.getAbsolutePath());
            for (File child : file.listFiles()) {
                addJar(child);
            }
        } catch (Exception ex) {
            logger.warn("Failed to load folder " + file.getAbsolutePath() + ": " + ex);
        }
    }

    public boolean reloadJar(ModuleWrapper wr) {
        return reloadJar(wr.source);
    }
    public boolean reloadJar(String a) {
        removeJar(a);
        return addJar(new File(a));
    }

    public void removeJar(String a) {
        try {
            ModulesData dat = JARS.get(a);
            List<ModuleWrapper> collect = dat.modules.stream().map(b -> b.instance).collect(Collectors.toList());
            manager.removeModules(collect.toArray(new ModuleWrapper[0]));
            dat.classloader.close();
            JARS.remove(a);
            logger.info("Successfully unloaded jar: " + a);
        } catch (IOException e) {
            logger.warn("Failed unloading jar: " + e);
        }
    }

    public boolean addJar(File a) {
        try {
            ZipFile file = new ZipFile(a);
            byte[] data = file.getInputStream(file.getEntry("modules.json")).readAllBytes();
            ModulesData mod = GSON.fromJson(new String(data), ModulesData.class);
            mod.file = a;
            mod.classloader = new URLClassLoader(new URL[]{a.toURI().toURL()});
            logger.info("Loaded jar: " + a.getAbsolutePath());
            List<ModuleData> remove = new LinkedList<>();
            for (ModuleData module : mod.modules) {
                logger.info("Loading module: " + module.mainClass + "v" + module.version);

                try {
                    module.main = (Class<? extends Module>) mod.classloader.loadClass(module.mainClass);
                    module.instance = new ModuleWrapper(module.main.getConstructor().newInstance(), a, module.version);
                    logger.info("Loaded module: " + module.instance.module.getName() + " (" + module.instance.module.getFriendlyName() + ")");
                } catch (Exception ex) {
                    logger.warn("Failed loading module: ");
                    ex.printStackTrace();
                    remove.add(module);
                }
            }
            mod.modules.removeAll(remove);
            JARS.put(a.getAbsolutePath(), mod);
            manager.addModule(mod.modules.stream().map(z ->
                z.instance
            ).toArray(ModuleWrapper[]::new));
        } catch (Exception ex) {
            logger.warn("Failed loading modules from jar: " + a.getAbsolutePath());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public void addGeneric(File file) {
        if(file.isDirectory())
            addFolder(file);
        else if(file.isFile())
            addJar(file);
        else
            throw new RuntimeException("Not a directory or a file???");
        // Impossible!
    }

    public static class ModulesData {
        @Expose
        public List<ModuleData> modules;

        public File file;
        public URLClassLoader classloader;
    }

    public static class ModuleData {
        @Expose
        @SerializedName("class")
        public String mainClass;
        @Expose
        public String version;

        public Class<? extends Module> main;
        public ModuleWrapper instance;
    }
}
