package com.massivecraft.factions.tag;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.landraidcontrol.PowerControl;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.util.TL;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum FactionTag implements Tag {
    @SuppressWarnings("Convert2MethodRef")
    INTERNAL_ID("faction-internal-id", (fac) -> fac.getId()),
    HOME_X("x", (fac) -> fac.hasHome() ? String.valueOf(fac.getHome().getBlockX()) : Tag.isMinimalShow() ? null : "{ig}"),
    HOME_Y("y", (fac) -> fac.hasHome() ? String.valueOf(fac.getHome().getBlockY()) : Tag.isMinimalShow() ? null : "{ig}"),
    HOME_Z("z", (fac) -> fac.hasHome() ? String.valueOf(fac.getHome().getBlockZ()) : Tag.isMinimalShow() ? null : "{ig}"),
    CHUNKS("chunks", (fac) -> String.valueOf(fac.getLandRounded())),
    HEADER("header", (fac, fp) -> FactionsPlugin.getInstance().txt().titleize(fac.getTag(fp))),
    POWER("power", (fac) -> String.valueOf(fac.getPowerRounded())),
    MAX_POWER("maxPower", (fac) -> String.valueOf(fac.getPowerMaxRounded())),
    POWER_BOOST("power-boost", (fac) -> {
        double powerBoost = fac.getPowerBoost();
        return (powerBoost == 0.0) ? "" : (powerBoost > 0.0 ? TL.COMMAND_SHOW_BONUS.toString() : TL.COMMAND_SHOW_PENALTY.toString() + powerBoost + ")");
    }),
    LEADER("leader", (fac) -> {
        FPlayer fAdmin = fac.getFPlayerAdmin();
        return fAdmin == null ? TL.TAG_LEADER_OWNERLESS.toString() : fAdmin.getName().substring(0, fAdmin.getName().length() > 14 ? 13 : fAdmin.getName().length());
    }),
    JOINING("joining", (fac) -> (fac.getOpen() ? TL.COMMAND_SHOW_UNINVITED.toString() : TL.COMMAND_SHOW_INVITATION.toString())),
    @SuppressWarnings("Convert2MethodRef")
    FACTION("faction", (fac) -> fac.getTag()),
    FACTION_RELATION_COLOR("faction-relation-color", (fac, fp) -> fp == null ? "" : fp.getColorTo(fac).toString()),
    HOME_WORLD("world", (fac) -> fac.hasHome() ? fac.getHome().getWorld().getName() : Tag.isMinimalShow() ? null : "{ig}"),
    RAIDABLE("raidable", (fac) -> {
        boolean raid = FactionsPlugin.getInstance().getLandRaidControl().isRaidable(fac);
        return raid ? TL.RAIDABLE_TRUE.toString() : TL.RAIDABLE_FALSE.toString();
    }),
    MAX_CHUNKS("max-chunks", (fac -> String.valueOf(FactionsPlugin.getInstance().getLandRaidControl().getLandLimit(fac)))),
    PEACEFUL("peaceful", (fac) -> fac.isPeaceful() ? FactionsPlugin.getInstance().conf().colors().relations().getPeaceful() + TL.COMMAND_SHOW_PEACEFUL.toString() : ""),
    PERMANENT("permanent", (fac) -> fac.isPermanent() ? "permanent" : "{notPermanent}"), // no braces needed
    DESCRIPTION("description", Faction::getDescription),
    CREATE_DATE("create-date", (fac) -> TL.sdf.format(fac.getFoundedDate())),
    ALLIES_COUNT("allies", (fac) -> String.valueOf(fac.getRelationCount(Relation.ALLY))),
    ENEMIES_COUNT("enemies", (fac) -> String.valueOf(fac.getRelationCount(Relation.ENEMY))),
    TRUCES_COUNT("truces", (fac) -> String.valueOf(fac.getRelationCount(Relation.TRUCE))),
    ONLINE_COUNT("online", (fac, fp) -> {
        if (fp != null && fp.isOnline()) {
            return String.valueOf(fac.getFPlayersWhereOnline(true, fp).size());
        } else {
            // Only console should ever get here.
            return String.valueOf(fac.getFPlayersWhereOnline(true).size());
        }
    }),
    OFFLINE_COUNT("offline", (fac, fp) -> {
        if (fp != null && fp.isOnline()) {
            return String.valueOf(fac.getFPlayers().size() - fac.getFPlayersWhereOnline(true, fp).size());
        } else {
            // Only console should ever get here.
            return String.valueOf(fac.getFPlayersWhereOnline(false).size());
        }
    }),
    FACTION_SIZE("members", (fac) -> String.valueOf(fac.getFPlayers().size())),
    FACTION_KILLS("faction-kills", (fac) -> String.valueOf(fac.getKills())),
    FACTION_DEATHS("faction-deaths", (fac) -> String.valueOf(fac.getDeaths())),
    ;

    private final String tag;
    private final BiFunction<Faction, FPlayer, String> biFunction;
    private final Function<Faction, String> function;

    public static String parse(String text, Faction faction, FPlayer player) {
        for (FactionTag tag : FactionTag.values()) {
            text = tag.replace(text, faction, player);
        }
        return text;
    }

    public static String parse(String text, Faction faction) {
        for (FactionTag tag : FactionTag.values()) {
            text = tag.replace(text, faction);
        }
        return text;
    }

    FactionTag(String tag, BiFunction<Faction, FPlayer, String> function) {
        this(tag, null, function);
    }

    FactionTag(String tag, Function<Faction, String> function) {
        this(tag, function, null);
    }

    FactionTag(String tag, Function<Faction, String> function, BiFunction<Faction, FPlayer, String> biFunction) {
        if (tag.equalsIgnoreCase("permanent")) {
            this.tag = tag;
        } else {
            this.tag = '{' + tag + '}';
        }
        this.biFunction = biFunction;
        this.function = function;
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    @Override
    public boolean foundInString(String test) {
        return test != null && test.contains(this.tag);
    }

    public String replace(String text, Faction faction, FPlayer player) {
        if (!this.foundInString(text)) {
            return text;
        }
        String result = this.function == null ? this.biFunction.apply(faction, player) : this.function.apply(faction);
        return result == null ? null : text.replace(this.tag, result);
    }

    public String replace(String text, Faction faction) {
        return this.replace(text, faction, null);
    }
}
