package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;

public class CmdBypass extends FCommand {

    public CmdBypass() {
        super();
        this.aliases.add("bypass");

        this.optionalArgs.put("on/off", "flip");

        this.requirements = new CommandRequirements.Builder(Permission.ADMIN)
                .playerOnly()
                .noDisableOnLock()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        context.fPlayer.setIsAdminBypassing(context.argAsBool(0, !context.fPlayer.isAdminBypassing()));

        // TODO: Move this to a transient field in the model??
        if (context.fPlayer.isAdminBypassing()) {
            context.fPlayer.msg(TL.COMMAND_BYPASS_ENABLE.toString());
            FactionsPlugin.getInstance().log(context.fPlayer.getName() + TL.COMMAND_BYPASS_ENABLELOG.toString());
        } else {
            context.fPlayer.msg(TL.COMMAND_BYPASS_DISABLE.toString());
            FactionsPlugin.getInstance().log(context.fPlayer.getName() + TL.COMMAND_BYPASS_DISABLELOG.toString());
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_BYPASS_DESCRIPTION;
    }
}
