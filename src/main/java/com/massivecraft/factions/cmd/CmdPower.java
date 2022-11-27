package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;

public class CmdPower extends FCommand {

    public CmdPower() {
        super();
        this.aliases.add("power");
        this.aliases.add("pow");

        this.optionalArgs.put("player", "you");

        this.requirements = new CommandRequirements.Builder(Permission.EVERYONE).noDisableOnLock().build();
    }

    @Override
    public void perform(CommandContext context) {
        FPlayer target = context.argAsBestFPlayerMatch(0, context.fPlayer);
        if (target == null) {
            return;
        }

        double powerBoost = target.getPowerBoost();
        String boost = (powerBoost == 0.0) ? "" : (powerBoost > 0.0 ? TL.COMMAND_POWER_BONUS.toString() : TL.COMMAND_POWER_PENALTY.toString()) + powerBoost + ")";
        context.msg(TL.COMMAND_POWER_POWER, target.describeTo(context.fPlayer, true), target.getPowerRounded(), target.getPowerMaxRounded(), boost);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_POWER_DESCRIPTION;
    }

}
