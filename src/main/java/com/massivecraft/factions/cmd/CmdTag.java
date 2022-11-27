package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.event.FactionRenameEvent;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.scoreboards.FTeamWrapper;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.MiscUtil;
import com.massivecraft.factions.util.TL;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class CmdTag extends FCommand {

    public CmdTag() {
        this.aliases.add("tag");
        this.aliases.add("rename");

        this.requiredArgs.add("faction tag");

        this.requirements = new CommandRequirements.Builder(Permission.EVERYONE)
                .memberOnly()
                .withRole(Role.MODERATOR)
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        String tag = context.argAsString(0);

        // TODO does not first shouldCancel cover selfcase?
        if (Factions.getInstance().isTagTaken(tag) && !MiscUtil.getComparisonString(tag).equals(context.faction.getComparisonTag())) {
            context.msg(TL.COMMAND_TAG_TAKEN);
            return;
        }

        ArrayList<String> errors = MiscUtil.validateTag(tag);
        if (errors.size() > 0) {
            context.sendMessage(errors);
            return;
        }

        // trigger the faction rename event (cancellable)
        FactionRenameEvent renameEvent = new FactionRenameEvent(context.fPlayer, tag);
        Bukkit.getServer().getPluginManager().callEvent(renameEvent);
        if (renameEvent.isCancelled()) {
            return;
        }

        String oldtag = context.faction.getTag();
        context.faction.setTag(tag);

        // Inform
        for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
            if (fplayer.getFactionId().equals(context.faction.getId())) {
                fplayer.msg(TL.COMMAND_TAG_FACTION, context.fPlayer.describeTo(context.faction, true), context.faction.getTag(context.faction));
                continue;
            }

            // Broadcast the tag change (if applicable)
            if (FactionsPlugin.getInstance().conf().factions().chat().isBroadcastTagChanges()) {
                Faction faction = fplayer.getFaction();
                fplayer.msg(TL.COMMAND_TAG_CHANGED, context.fPlayer.getColorTo(faction) + oldtag, context.faction.getTag(faction));
            }
        }

        FTeamWrapper.updatePrefixes(context.faction);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_TAG_DESCRIPTION;
    }

}
