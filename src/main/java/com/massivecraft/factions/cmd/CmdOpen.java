package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;

public class CmdOpen extends FCommand {

    public CmdOpen() {
        super();
        this.aliases.add("open");

        this.optionalArgs.put("yes/no", "flip");

        this.requirements = new CommandRequirements.Builder(Permission.EVERYONE)
                .playerOnly()
                .noDisableOnLock()
                .withRole(Role.MODERATOR)
                .build();
    }

    @Override
    public void perform(CommandContext context) {

        context.faction.setOpen(context.argAsBool(0, !context.faction.getOpen()));

        String open = context.faction.getOpen() ? TL.COMMAND_OPEN_OPEN.toString() : TL.COMMAND_OPEN_CLOSED.toString();

        // Inform
        for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
            if (fplayer.getFactionId().equals(context.faction.getId())) {
                fplayer.msg(TL.COMMAND_OPEN_CHANGES, context.fPlayer.getName(), open);
                continue;
            }
            fplayer.msg(TL.COMMAND_OPEN_CHANGED, context.faction.getTag(fplayer.getFaction()), open);
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_OPEN_DESCRIPTION;
    }

}
