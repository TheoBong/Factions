package com.massivecraft.factions.cmd;

import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;

public class CmdLock extends FCommand {

    /*
       factions.lock:
	description: use the /f lock [on/off] command to temporarily lock the data files from being overwritten
	default: op
	 */

    public CmdLock() {
        super();
        this.aliases.add("lock");

        this.optionalArgs.put("on/off", "flip");

        this.requirements = new CommandRequirements.Builder(Permission.ADMIN).noDisableOnLock().build();
    }

    @Override
    public void perform(CommandContext context) {
        plugin.setLocked(context.argAsBool(0, !plugin.getLocked()));
        context.msg(plugin.getLocked() ? TL.COMMAND_LOCK_LOCKED : TL.COMMAND_LOCK_UNLOCKED);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_LOCK_DESCRIPTION;
    }

}
