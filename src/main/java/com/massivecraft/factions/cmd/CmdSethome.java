package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;

public class CmdSethome extends FCommand {

    public CmdSethome() {
        this.aliases.add("sethome");

        this.requirements = new CommandRequirements.Builder(Permission.EVERYONE)
                .memberOnly()
                .withAction(PermissibleActions.SETHOME)
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        if (!FactionsPlugin.getInstance().conf().factions().homes().isEnabled()) {
            context.msg(TL.COMMAND_SETHOME_DISABLED);
            return;
        }

        // Can the player set the faction home HERE?
        if (!Permission.ADMIN.has(context.player) &&
                FactionsPlugin.getInstance().conf().factions().homes().isMustBeInClaimedTerritory() &&
                Board.getInstance().getFactionAt(new FLocation(context.player)) != context.faction) {
            context.msg(TL.COMMAND_SETHOME_NOTCLAIMED);
            return;
        }

        context.faction.setHome(context.player.getLocation());

        context.faction.msg(TL.COMMAND_SETHOME_SET, context.fPlayer.describeTo(context.faction, true));
        context.faction.sendMessage(FCmdRoot.getInstance().cmdHome.getUsageTemplate(context));
        /*
        if (faction != context.faction) {
            context.msg(TL.COMMAND_SETHOME_SETOTHER, faction.getTag(context.fPlayer));
        }*/
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_SETHOME_DESCRIPTION;
    }

}
