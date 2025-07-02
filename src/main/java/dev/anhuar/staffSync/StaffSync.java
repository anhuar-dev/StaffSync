package dev.anhuar.staffSync;

import dev.anhuar.staffSync.handler.*;
import dev.anhuar.staffSync.menu.DemoteMenu;
import dev.anhuar.staffSync.menu.MainMenu;
import dev.anhuar.staffSync.menu.PromoteMenu;
import dev.anhuar.staffSync.menu.TimeMenu;
import dev.anhuar.staffSync.util.ConfigUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class StaffSync extends JavaPlugin {

    @Getter
    @Setter
    private static StaffSync instance;

    private ConfigUtil setting, message, staffMenu, staffPromoteMenu, staffDemoteMenu, staffTimeMenu;

    private MongoHandler mongoHandler;
    private RedisHandler redisHandler;
    private CommandHandler commandHandler;
    private ListenerHandler listenerHandler;
    private ManagerHandler managerHandler;
    private MainMenu mainMenu;
    private DemoteMenu demoteMenu;
    private PromoteMenu promoteMenu;
    private TimeMenu timeMenu;

    @Override
    public void onEnable() {

        instance = this;

        setting = new ConfigUtil(this, "setting.yml");
        message = new ConfigUtil(this, "message.yml");
        staffMenu = new ConfigUtil(this, "menu/main-menu.yml");
        staffPromoteMenu = new ConfigUtil(this, "menu/promote-menu.yml");
        staffDemoteMenu = new ConfigUtil(this, "menu/demote-menu.yml");
        staffTimeMenu = new ConfigUtil(this, "menu/time-menu.yml");

        mongoHandler = new MongoHandler(this);
        redisHandler = new RedisHandler(this);

        managerHandler = new ManagerHandler(this);
        listenerHandler = new ListenerHandler(this);
        commandHandler = new CommandHandler(this);

        mainMenu = new MainMenu(this);

    }


    @Override
    public void onDisable() {
        if (this.mongoHandler != null) {
            this.mongoHandler.close();
        }
        if (this.redisHandler != null) {
            this.redisHandler.disconnect();
        }
    }
}
