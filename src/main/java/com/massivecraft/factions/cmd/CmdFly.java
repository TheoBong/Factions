package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.FlightUtil;
import com.massivecraft.factions.util.TL;
import com.massivecraft.factions.util.WarmUpUtil;

public class CmdFly extends FCommand {

    public CmdFly() {
        super();
        this.aliases.add("fly");

        this.requirements = new CommandRequirements.Builder(Permission.EVERYONE)
                .memberOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        toggleFlight(context, !context.fPlayer.isFlying());
    }

    private void toggleFlight(final CommandContext context, final boolean toggle) {
        // If false do nothing besides set
        if (!toggle) {
            context.fPlayer.setFlying(false);
            return;
        }
        // Do checks if true
        if (!flyTest(context)) {
            return;
        }

        context.doWarmUp(WarmUpUtil.Warmup.FLIGHT, TL.WARMUPS_NOTIFY_FLIGHT, "Fly", () -> {
            if (flyTest(context)) {
                context.fPlayer.setFlying(true);
            }
        }, this.plugin.conf().commands().fly().getDelay());
    }

    private boolean flyTest(final CommandContext context) {
        if (!context.fPlayer.canFlyAtLocation()) {
            Faction factionAtLocation = Board.getInstance().getFactionAt(context.fPlayer.getLastStoodAt());
            context.msg(TL.COMMAND_FLY_NO_ACCESS, factionAtLocation.getTag(context.fPlayer));
            return false;
        } else if (FlightUtil.instance().enemiesNearby(context.fPlayer, FactionsPlugin.getInstance().conf().commands().fly().getEnemyRadius())) {
            context.msg(TL.COMMAND_FLY_ENEMY_NEARBY);
            return false;
        }
        return true;
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_FLY_DESCRIPTION;
    }

}
