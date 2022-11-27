package com.massivecraft.factions.listeners;

import com.massivecraft.factions.*;
import com.massivecraft.factions.config.file.MainConfig;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public abstract class AbstractListener implements Listener {
    public boolean playerCanInteractHere(Player player, Location location) {
        return canInteractHere(player, location);
    }

    public static boolean canInteractHere(Player player, Location location) {
        String name = player.getName();
        if (FactionsPlugin.getInstance().conf().factions().protection().getPlayersWhoBypassAllProtection().contains(name)) {
            return true;
        }

        FPlayer me = FPlayers.getInstance().getByPlayer(player);
        if (me.isAdminBypassing()) {
            return true;
        }

        FLocation loc = new FLocation(location);
        Faction otherFaction = Board.getInstance().getFactionAt(loc);

        if (FactionsPlugin.getInstance().getLandRaidControl().isRaidable(otherFaction)) {
            return true;
        }

        MainConfig.Factions.Protection protection = FactionsPlugin.getInstance().conf().factions().protection();
        if (otherFaction.isWilderness()) {
            if (!protection.isWildernessDenyUsage() || protection.getWorldsNoWildernessProtection().contains(location.getWorld().getName())) {
                return true; // This is not faction territory. Use whatever you like here.
            }
            me.msg(TL.PLAYER_USE_WILDERNESS, "this");
            return false;
        } else if (otherFaction.isSafeZone()) {
            if (!protection.isSafeZoneDenyUsage() || Permission.ADMIN.has(player)) {
                return true;
            }
            me.msg(TL.PLAYER_USE_SAFEZONE, "this");
            return false;
        } else if (otherFaction.isWarZone()) {
            if (!protection.isWarZoneDenyUsage() || Permission.ADMIN.has(player)) {
                return true;
            }
            me.msg(TL.PLAYER_USE_WARZONE, "this");

            return false;
        }

        boolean access = otherFaction.hasAccess(me, PermissibleActions.ITEM, loc);

        // Cancel if we are not in our own territory
        if (!access) {
            me.msg(TL.PLAYER_USE_TERRITORY, "this", otherFaction.getTag(me.getFaction()));
            return false;
        }

        return true;
    }

    protected void handleExplosion(Location loc, Entity boomer, Cancellable event, List<Block> blockList) {
        if (!FactionsPlugin.getInstance().worldUtil().isEnabled(loc.getWorld())) {
            return;
        }

        if (explosionDisallowed(boomer, new FLocation(loc))) {
            event.setCancelled(true);
            return;
        }

        List<Chunk> chunks = blockList.stream().map(Block::getChunk).distinct().collect(Collectors.toList());
        if (chunks.removeIf(chunk -> explosionDisallowed(boomer, new FLocation(chunk)))) {
            blockList.removeIf(block -> !chunks.contains(block.getChunk()));
        }

        if ((boomer instanceof TNTPrimed || boomer instanceof ExplosiveMinecart) && FactionsPlugin.getInstance().conf().exploits().isTntWaterlog()) {
            // TNT in water/lava doesn't normally destroy any surrounding blocks, which is usually desired behavior, but...
            // this change below provides workaround for waterwalling providing perfect protection,
            // and makes cheap (non-obsidian) TNT cannons require minor maintenance between shots
            Block center = loc.getBlock();
            if (center.isLiquid()) {
                // a single surrounding block in all 6 directions is broken if the material is weak enough
                List<Block> targets = new ArrayList<>();
                targets.add(center.getRelative(0, 0, 1));
                targets.add(center.getRelative(0, 0, -1));
                targets.add(center.getRelative(0, 1, 0));
                targets.add(center.getRelative(0, -1, 0));
                targets.add(center.getRelative(1, 0, 0));
                targets.add(center.getRelative(-1, 0, 0));
                for (Block target : targets) {
                    // TODO get resistance value via NMS for future-proofing
                    switch (target.getType()) {
                        case AIR:
                        case BEDROCK:
                        case WATER:
                        case LAVA:
                        case OBSIDIAN:
                        case PORTAL:
                        case ENCHANTMENT_TABLE:
                        case ANVIL:
                        case ENDER_PORTAL:
                        case ENDER_PORTAL_FRAME:
                        case ENDER_CHEST:
                            continue;
                    }
                    if (!explosionDisallowed(boomer, new FLocation(target.getLocation()))) {
                        target.breakNaturally();
                    }
                }
            }
        }
    }

    public static boolean explosionDisallowed(Entity boomer, FLocation location) {
        Faction faction = Board.getInstance().getFactionAt(location);
        boolean online = faction.hasPlayersOnline();
        if (faction.noExplosionsInTerritory() || (faction.isPeaceful() && FactionsPlugin.getInstance().conf().factions().specialCase().isPeacefulTerritoryDisableBoom())) {
            // faction is peaceful and has explosions set to disabled
            return true;
        }
        MainConfig.Factions.Protection protection = FactionsPlugin.getInstance().conf().factions().protection();
        if (boomer instanceof Creeper && ((faction.isWilderness() && protection.isWildernessBlockCreepers() && !protection.getWorldsNoWildernessProtection().contains(location.getWorldName())) ||
                (faction.isNormal() && (online ? protection.isTerritoryBlockCreepers() : protection.isTerritoryBlockCreepersWhenOffline())) ||
                (faction.isWarZone() && protection.isWarZoneBlockCreepers()) ||
                faction.isSafeZone())) {
            // creeper which needs prevention
            return true;
        } else if (
                (boomer instanceof Fireball || boomer instanceof WitherSkull || boomer instanceof Wither) && ((faction.isWilderness() && protection.isWildernessBlockFireballs() && !protection.getWorldsNoWildernessProtection().contains(location.getWorldName())) ||
                        (faction.isNormal() && (online ? protection.isTerritoryBlockFireballs() : protection.isTerritoryBlockFireballsWhenOffline())) ||
                        (faction.isWarZone() && protection.isWarZoneBlockFireballs()) ||
                        faction.isSafeZone())) {
            // ghast fireball which needs prevention
            // it's a bit crude just using fireball protection for Wither boss too, but I'd rather not add in a whole new set of xxxBlockWitherExplosion or whatever
            return true;
        } else if ((boomer instanceof TNTPrimed || boomer instanceof ExplosiveMinecart) && ((faction.isWilderness() && protection.isWildernessBlockTNT() && !protection.getWorldsNoWildernessProtection().contains(location.getWorldName())) ||
                (faction.isNormal() && (online ? protection.isTerritoryBlockTNT() : protection.isTerritoryBlockTNTWhenOffline())) ||
                (faction.isWarZone() && protection.isWarZoneBlockTNT()) ||
                (faction.isSafeZone() && protection.isSafeZoneBlockTNT()))) {
            // TNT which needs prevention
            return true;
        } else if (((faction.isWilderness() && protection.isWildernessBlockOtherExplosions() && !protection.getWorldsNoWildernessProtection().contains(location.getWorldName())) ||
                (faction.isNormal() && (online ? protection.isTerritoryBlockOtherExplosions() : protection.isTerritoryBlockOtherExplosionsWhenOffline())) ||
                (faction.isWarZone() && protection.isWarZoneBlockOtherExplosions()) ||
                (faction.isSafeZone() && protection.isSafeZoneBlockOtherExplosions()))) {
            return true;
        }
        return false;
    }

    public boolean canPlayerUseBlock(Player player, Material material, Location location, boolean justCheck) {
        return canUseBlock(player, material, location, justCheck);
    }

    public static boolean canUseBlock(Player player, Material material, Location location, boolean justCheck) {
        if (FactionsPlugin.getInstance().conf().factions().protection().getPlayersWhoBypassAllProtection().contains(player.getName())) {
            return true;
        }

        FPlayer me = FPlayers.getInstance().getByPlayer(player);
        if (me.isAdminBypassing()) {
            return true;
        }

        FLocation loc = new FLocation(location);
        Faction otherFaction = Board.getInstance().getFactionAt(loc);

        // no door/chest/whatever protection in wilderness, war zones, or safe zones
        if (!otherFaction.isNormal()) {
            switch (material) {
                case ITEM_FRAME:
                case ARMOR_STAND:
                    return canInteractHere(player, location);
            }
            return true;
        }

        if (FactionsPlugin.getInstance().getLandRaidControl().isRaidable(otherFaction)) {
            return true;
        }

        PermissibleAction action = null;

        switch (material) {
            case LEVER:
                action = PermissibleActions.LEVER;
                break;
            case STONE_BUTTON:
                action = PermissibleActions.BUTTON;
                break;
            case DARK_OAK_DOOR:
            case ACACIA_DOOR:
            case BIRCH_DOOR:
            case IRON_DOOR:
            case JUNGLE_DOOR:
            case SPRUCE_DOOR:
            case IRON_TRAPDOOR:
                action = PermissibleActions.DOOR;
                break;
            case CHEST:
            case ENDER_CHEST:
            case TRAPPED_CHEST:
            case FURNACE:
            case DROPPER:
            case DISPENSER:
            case HOPPER:
            case CAULDRON:
            case BREWING_STAND:
            case ITEM_FRAME:
            case JUKEBOX:
            case ARMOR_STAND:
            case DIODE:
            case ENCHANTMENT_TABLE:
            case BEACON:
            case ANVIL:
            case FLOWER_POT:
                action = PermissibleActions.CONTAINER;
                break;
            default:
                // Check for doors that might have diff material name in old version.
                if (material.name().contains("DOOR") || material.name().contains("GATE")) {
                    action = PermissibleActions.DOOR;
                }
                if (material.name().contains("BUTTON")) {
                    action = PermissibleActions.BUTTON;
                }
                if (material.name().contains("FURNACE")) {
                    action = PermissibleActions.CONTAINER;
                }
                if (FactionsPlugin.getInstance().conf().factions().protection().getCustomContainers().contains(material)) {
                    action = PermissibleActions.CONTAINER;
                }
                // Lazier than checking all the combinations
                if (material.name().contains("SHULKER") || material.name().contains("ANVIL") || material.name().startsWith("POTTED")) {
                    action = PermissibleActions.CONTAINER;
                }
                if (material.name().endsWith("_PLATE")) {
                    action = PermissibleActions.PLATE;
                }
                if (material.name().contains("SIGN")) {
                    action = PermissibleActions.ITEM;
                }
                break;
        }

        if (action == null) {
            return true;
        }

        // Ignored types
        if (action == PermissibleActions.CONTAINER &&
                (
                        FactionsPlugin.getInstance().conf().factions().protection().getContainerExceptions().contains(material) ||
                                (
                                        otherFaction.isNormal() &&
                                                material.name().equals("LECTERN") &&
                                                FactionsPlugin.getInstance().conf().factions().protection().isTerritoryAllowLecternReading()
                                )
                )) {
            return true;
        }

        // F PERM check runs through before other checks.
        if (!otherFaction.hasAccess(me, action, loc)) {
            if (action != PermissibleActions.PLATE) {
                me.msg(TL.GENERIC_NOPERMISSION, action.getShortDescription());
            }
            return false;
        }

        // Dupe fix.
        Faction myFaction = me.getFaction();
        Relation rel = myFaction.getRelationTo(otherFaction);
        if (FactionsPlugin.getInstance().conf().exploits().doPreventDuping() && !rel.isMember()) {
            Material mainHand = player.getItemInHand().getType();

            // Check if material is at risk for dupe in either hand.
            return !isDupeMaterial(mainHand);
        }

        return true;
    }

    private static boolean isDupeMaterial(Material material) {
        if (material.name().toUpperCase().contains("SIGN")) {
            return true;
        }

        switch (material) {
            case CHEST:
            case TRAPPED_CHEST:
            case DARK_OAK_DOOR:
            case ACACIA_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case WOOD_DOOR:
            case SPRUCE_DOOR:
            case IRON_DOOR:
                return true;
            default:
                break;
        }

        return false;
    }
}
