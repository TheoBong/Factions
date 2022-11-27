package com.massivecraft.factions.util;

import com.massivecraft.factions.FactionsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import java.util.HashMap;
import java.util.Map;


public class PermUtil {

    public final Map<String, String> permissionDescriptions = new HashMap<>();

    protected final FactionsPlugin plugin;

    public PermUtil(FactionsPlugin plugin) {
        this.plugin = plugin;
        this.setup();
    }

    /**
     * This method hooks into all permission plugins we are supporting
     */
    public final void setup() {
        for (Permission permission : plugin.getDescription().getPermissions()) {
            //p.log("\""+permission.getName()+"\" = \""+permission.getDescription()+"\"");
            this.permissionDescriptions.put(permission.getName(), permission.getDescription());
        }
    }

    public boolean has(CommandSender me, String perm, boolean informSenderIfNot) {
        if (me == null) {
            return false; // What? How?
        }
        if (me.hasPermission(perm)) {
            return true;
        } else if (informSenderIfNot) {
            me.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
        }
        return false;
    }
}
