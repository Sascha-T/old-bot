package de.saschat.bot.module;

import java.io.File;

public class ModuleWrapper {
    public Module module;
    public String source;
    public SourceType type;
    public String version;

    public enum SourceType {
        BUILTIN,
        ADDON
    }

    public ModuleWrapper(Module module) {
        this.module = module;
        this.source = "BUILTIN";
        this.type = SourceType.BUILTIN;
    }

    public ModuleWrapper(Module module, File file, String version) {
        this.module = module;
        this.source = file.getAbsolutePath();
        this.type = SourceType.ADDON;
        this.version = version;
    }
}
