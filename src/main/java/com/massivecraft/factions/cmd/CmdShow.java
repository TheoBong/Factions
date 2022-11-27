package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.tag.FactionTag;
import com.massivecraft.factions.tag.FancyTag;
import com.massivecraft.factions.tag.Tag;
import com.massivecraft.factions.util.MiscUtil;
import com.massivecraft.factions.util.TL;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class CmdShow extends FCommand {

    final List<String> defaults = new ArrayList<>();

    public CmdShow() {
        this.aliases.add("show");
        this.aliases.add("who");

        // add defaults to /f show in case config doesnt have it
        defaults.add("{header}");
        defaults.add("<a>Description: <i>{description}");
        defaults.add("<a>Joining: <i>{joining}    {peaceful}");
        defaults.add("<a>Land / Power / Maxpower: <i> {chunks} / {power} / {maxPower}");
        defaults.add("<a>Founded: <i>{create-date}");
        defaults.add("<a>This faction is permanent, remaining even with no members.");
        defaults.add("<a>Allies(<i>{allies}<a>/<i>{max-allies}<a>): {allies-list}");
        defaults.add("<a>Online: (<i>{online}<a>/<i>{members}<a>): {online-list}");
        defaults.add("<a>Offline: (<i>{offline}<a>/<i>{members}<a>): {offline-list}");

        this.optionalArgs.put("faction tag", "yours");

        this.requirements = new CommandRequirements.Builder(Permission.EVERYONE).noDisableOnLock().build();
    }

    @Override
    public void perform(CommandContext context) {
        Faction faction = context.faction;
        if (context.argIsSet(0)) {
            faction = context.argAsFaction(0);
        }
        if (faction == null) {
            return;
        }

        if (context.fPlayer != null && !context.player.hasPermission(Permission.ADMIN.toString())
                && FactionsPlugin.getInstance().conf().commands().show().getExempt().contains(faction.getTag())) {
            context.msg(TL.COMMAND_SHOW_EXEMPT);
            return;
        }

        List<String> show = FactionsPlugin.getInstance().conf().commands().show().getFormat();
        if (show == null || show.isEmpty()) {
            show = defaults;
        }

        if (!faction.isNormal()) {
            String tag = faction.getTag(context.fPlayer);
            // send header and that's all
            String header = show.get(0);
            if (FactionTag.HEADER.foundInString(header)) {
                context.msg(plugin.txt().titleize(tag));
            } else {
                String message = header.replace(FactionTag.FACTION.getTag(), tag);
                message = Tag.parsePlain(faction, context.fPlayer, message);
                context.msg(plugin.txt().parse(message));
            }
            return; // we only show header for non-normal factions
        }

        List<String> messageList = new ArrayList<>();
        for (String raw : show) {
            String parsed = Tag.parsePlain(faction, context.fPlayer, raw); // use relations
            if (parsed == null) {
                continue; // Due to minimal f show.
            }

            if (context.fPlayer != null) {
                parsed = Tag.parsePlaceholders(context.fPlayer.getPlayer(), parsed);
            }

            if (!parsed.contains("{notFrozen}") && !parsed.contains("{notPermanent}")) {
                if (parsed.contains("{ig}")) {
                    // replaces all variables with no home TL
                    parsed = parsed.substring(0, parsed.indexOf("{ig}")) + TL.COMMAND_SHOW_NOHOME.toString();
                }
                parsed = parsed.replace("%", ""); // Just in case it got in there before we disallowed it.
                messageList.add(parsed);
            }
        }

        this.sendMessages(messageList, context.sender, faction, context.fPlayer);
    }

    private void sendMessages(List<String> messageList, CommandSender recipient, Faction faction, FPlayer player) {
        this.sendMessages(messageList, recipient, faction, player, null);
    }

    private void sendMessages(List<String> messageList, CommandSender recipient, Faction faction, FPlayer player, Map<UUID, String> groupMap) {
        FancyTag tag;
        for (String parsed : messageList) {
            if ((tag = FancyTag.getMatch(parsed)) != null) {
                if (player != null) {
                    List<FancyMessage> fancy = FancyTag.parse(parsed, faction, player, groupMap);
                    if (fancy != null) {
                        for (FancyMessage fancyMessage : fancy) {
                            fancyMessage.send(recipient);
                        }
                    }
                } else {
                    StringBuilder builder = new StringBuilder();
                    builder.append(parsed.replace(tag.getTag(), ""));
                    switch (tag) {
                        case ONLINE_LIST:
                            this.onOffLineMessage(builder, recipient, faction, true);
                            break;
                        case OFFLINE_LIST:
                            this.onOffLineMessage(builder, recipient, faction, false);
                            break;
                        case ALLIES_LIST:
                            this.relationMessage(builder, recipient, faction, Relation.ALLY);
                            break;
                        case ENEMIES_LIST:
                            this.relationMessage(builder, recipient, faction, Relation.ENEMY);
                            break;
                        case TRUCES_LIST:
                            this.relationMessage(builder, recipient, faction, Relation.TRUCE);
                            break;
                        default:
                            // NO
                    }
                }
            } else {
                recipient.sendMessage(FactionsPlugin.getInstance().txt().parse(parsed));
            }
        }
    }

    private void onOffLineMessage(StringBuilder builder, CommandSender recipient, Faction faction, boolean online) {
        boolean first = true;
        for (FPlayer p : MiscUtil.rankOrder(faction.getFPlayersWhereOnline(online))) {
            String name = p.getNameAndTitle();
            builder.append(first ? name : ", " + name);
            first = false;
        }
        recipient.sendMessage(FactionsPlugin.getInstance().txt().parse(builder.toString()));
    }

    private void relationMessage(StringBuilder builder, CommandSender recipient, Faction faction, Relation relation) {
        boolean first = true;
        for (Faction otherFaction : Factions.getInstance().getAllFactions()) {
            if (otherFaction != faction && otherFaction.getRelationTo(faction) == relation) {
                String s = otherFaction.getTag();
                builder.append(first ? s : ", " + s);
                first = false;
            }
        }
        recipient.sendMessage(FactionsPlugin.getInstance().txt().parse(builder.toString()));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_SHOW_COMMANDDESCRIPTION;
    }

}