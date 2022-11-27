package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;


public class CmdMap extends FCommand {

    public CmdMap() {
        super();
        this.aliases.add("map");

        this.optionalArgs.put("on/off", "once");

        this.requirements = new CommandRequirements.Builder(Permission.EVERYONE)
                .playerOnly()
                .noDisableOnLock()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        if (context.argIsSet(0)) {
            if (context.argAsBool(0, !context.fPlayer.isMapAutoUpdating())) {
                // Turn on

                context.fPlayer.setMapAutoUpdating(true);
                context.msg(TL.COMMAND_MAP_UPDATE_ENABLED);

                // And show the map once
                showMap(context);
            } else {
                // Turn off
                context.fPlayer.setMapAutoUpdating(false);
                context.msg(TL.COMMAND_MAP_UPDATE_DISABLED);
            }
        } else {
            showMap(context);
        }
    }

    public void showMap(CommandContext context) {
        context.sendFancyMessage(Board.getInstance().getMap(context.fPlayer, new FLocation(context.fPlayer), context.fPlayer.getPlayer().getLocation().getYaw()));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_MAP_DESCRIPTION;
    }

}
