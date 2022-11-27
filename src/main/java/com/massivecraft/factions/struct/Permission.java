package com.massivecraft.factions.struct;

import com.massivecraft.factions.FactionsPlugin;
import org.bukkit.command.CommandSender;

public enum Permission {
    ADMIN("admin"),
    EVERYONE("everyone");

    public final String node;

    Permission(final String node) {
        this.node = "factions." + node;
    }

    @Override
    public String toString() {
        return this.node;
    }

    public boolean has(CommandSender sender, boolean informSenderIfNot) {
        return FactionsPlugin.getInstance().getPermUtil().has(sender, this.node, informSenderIfNot);
    }

    public boolean has(CommandSender sender) {
        return has(sender, false);
    }
}
