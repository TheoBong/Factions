package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FactionAttemptCreateEvent;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.MiscUtil;
import com.massivecraft.factions.util.TL;
import org.bukkit.Bukkit;

import java.util.ArrayList;


public class CmdCreate extends FCommand {

    public CmdCreate() {
        super();
        this.aliases.add("create");

        this.requiredArgs.add("faction tag");

        this.requirements = new CommandRequirements.Builder(Permission.EVERYONE)
                .playerOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        String tag = context.argAsString(0);

        if (context.fPlayer.hasFaction()) {
            context.msg(TL.COMMAND_CREATE_MUSTLEAVE);
            return;
        }

        if (Factions.getInstance().isTagTaken(tag)) {
            context.msg(TL.COMMAND_CREATE_INUSE);
            return;
        }

        ArrayList<String> tagValidationErrors = MiscUtil.validateTag(tag);
        if (tagValidationErrors.size() > 0) {
            context.sendMessage(tagValidationErrors);
            return;
        }

        FactionAttemptCreateEvent attemptEvent = new FactionAttemptCreateEvent(context.player, tag);
        Bukkit.getServer().getPluginManager().callEvent(attemptEvent);
        if (attemptEvent.isCancelled()) {
            return;
        }

        Faction faction = Factions.getInstance().createFaction();

        // TODO: Why would this even happen??? Auto increment clash??
        if (faction == null) {
            context.msg(TL.COMMAND_CREATE_ERROR);
            return;
        }

        // finish setting up the Faction
        faction.setTag(tag);

        // trigger the faction join event for the creator
        FPlayerJoinEvent joinEvent = new FPlayerJoinEvent(FPlayers.getInstance().getByPlayer(context.player), faction, FPlayerJoinEvent.PlayerJoinReason.CREATE);
        Bukkit.getServer().getPluginManager().callEvent(joinEvent);
        // join event cannot be cancelled or you'll have an empty faction

        // finish setting up the FPlayer
        context.fPlayer.setRole(Role.ADMIN);
        context.fPlayer.setFaction(faction);

        // trigger the faction creation event
        FactionCreateEvent createEvent = new FactionCreateEvent(context.player, tag, faction);
        Bukkit.getServer().getPluginManager().callEvent(createEvent);

        for (FPlayer follower : FPlayers.getInstance().getOnlinePlayers()) {
            follower.msg(TL.COMMAND_CREATE_CREATED, context.fPlayer.describeTo(follower, true), faction.getTag(follower));
        }

        context.msg(TL.COMMAND_CREATE_YOUSHOULD, FCmdRoot.getInstance().cmdDescription.getUsageTemplate(context));

        if (FactionsPlugin.getInstance().conf().logging().isFactionCreate()) {
            FactionsPlugin.getInstance().log(context.fPlayer.getName() + TL.COMMAND_CREATE_CREATEDLOG.toString() + tag);
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_CREATE_DESCRIPTION;
    }

}
