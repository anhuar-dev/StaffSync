package dev.anhuar.staffSync;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class StaffSync extends JavaPlugin {

    @Getter
    @Setter
    private static StaffSync instance;

    @Override
    public void onEnable() {

        instance = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
