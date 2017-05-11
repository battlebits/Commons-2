package br.com.battlebits.commons.bungee.command.register;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.bungee.command.BungeeCommandArgs;
import br.com.battlebits.commons.bungee.party.BungeeParty;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.data.DataParty;
import br.com.battlebits.commons.core.translate.T;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PartyCommand implements CommandClass {
	@Command(name = "party", usage = "</command>", aliases = "p", runAsync = true)
	public void party(BungeeCommandArgs cmdArgs) {
		if (cmdArgs.isPlayer()) {
			String[] args = cmdArgs.getArgs();

			ProxiedPlayer player = cmdArgs.getPlayer();
			String prefix = T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-prefix") + " ";

			/* Party: Accept */
			if (args.length == 2 && (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("aceitar"))) {
				BungeeParty party = (BungeeParty) BattlebitsAPI.getPartyCommon().getParty(player.getUniqueId());

				if (party == null) {
					ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);

					if (target != null) {
						party = (BungeeParty) BattlebitsAPI.getPartyCommon().getParty(target.getUniqueId());

						if (party != null && party.isPromoted(target.getUniqueId())) {
							if (party.getInviteQueue().containsKey(player.getUniqueId())) {
								int task = party.getInviteQueue().remove(player.getUniqueId());
								ProxyServer.getInstance().getScheduler().cancel(task);

								party.addMember(player.getUniqueId());
								DataParty.saveRedisParty(party);
								DataParty.saveRedisPartyField(party, "members");

								party.sendMessage(prefix, "command-party-new-member", new String[] { "%player%", player.getName() });
							} else {
								player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-not-invited")));
							}
						} else {
							player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-not-invited")));
						}
					} else {
						player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-player-offline", new String[] { "%player%", args[1] })));
					}
				} else {
					player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-already-in-party")));
				}

				return;
			}

			/* Party: Invite */
			if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("convidar"))) {
				BungeeParty party = (BungeeParty) BattlebitsAPI.getPartyCommon().getByOwner(player.getUniqueId());

				if (party == null) {
					party = (BungeeParty) BattlebitsAPI.getPartyCommon().getParty(player.getUniqueId());

					if (party == null) {
						party = new BungeeParty(player.getUniqueId());
						BattlebitsAPI.getPartyCommon().loadParty(party);
						DataParty.saveRedisParty(party);
						DataParty.loadParty(party);
					} else if (!party.isPromoted(player.getUniqueId())) {
						player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-promoted-member-only")));
						return;
					}
				}

				if (party != null) {
					ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);

					if (target != null) {
						if (!target.getUniqueId().equals(player.getUniqueId())) {
							if (!party.getInviteQueue().containsKey(target.getUniqueId())) {
								/** String tag = ""; **/
								party.addInvite(target);

								player.sendMessage(TextComponent.fromLegacyText("§6§lPARTY §fVocê convidou §e" + target.getName() + " §fpara entrar em sua §6§lPARTY§f."));
								target.sendMessage(TextComponent.fromLegacyText("§6§lPARTY §fVocê foi convidado para entrar na §6§lPARTY §fde §e" + player.getName() + "§f."));

								TextComponent clickHere = new TextComponent("aqui");
								clickHere.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept " + player.getName()));
								clickHere.setColor(ChatColor.YELLOW);
								clickHere.setUnderlined(true);
								clickHere.setBold(true);

								TextComponent extraText = new TextComponent(TextComponent.fromLegacyText(" §fpara aceitar o pedido de §e" + player.getName() + "§f."));
								TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText("§6§lPARTY §fClique "));
								textComponent.addExtra(clickHere);
								textComponent.addExtra(extraText);
								target.sendMessage(textComponent);
							} else {
								player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-already-invited")));
							}
						} else {
							player.sendMessage(TextComponent.fromLegacyText("§6§lPARTY §fVocê não pode se convidar!"));
						}
					} else {
						player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-player-offline", new String[] { "%player%", args[1] })));
					}
				}

				return;
			}

			/* Party: Remove */
			if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("remover"))) {
				BungeeParty party = (BungeeParty) BattlebitsAPI.getPartyCommon().getParty(player.getUniqueId());

				if (party != null) {
					if (party.isPromoted(player.getUniqueId())) {
						ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);

						if (target != null) {
							if (!player.getUniqueId().equals(target.getUniqueId())) {
								if (party.getMembers().contains(target.getUniqueId())) {
									party.getMembers().remove(target.getUniqueId());
									party.getPromoted().remove(target.getUniqueId());

									DataParty.saveRedisPartyField(party, "members");
									DataParty.saveRedisPartyField(party, "promoted");
									DataParty.saveRedisParty(party);

									player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-player-removed", new String[] { "%player%", target.getName() })));
									target.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-player-removed-target", new String[] { "%player%", player.getName() })));
								} else {
									player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-player-is-not-in-party", new String[] { "%player%", target.getName() })));
								}
							} else {
								player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-not-remove-yourself")));
							}
						} else {
							player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-player-offline", new String[] { "%player%", args[1] })));
						}
					} else {
						player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-promoted-member-only")));
					}
				} else {
					player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-is-not-in-party")));
				}

				return;
			}

			/* Party: Promote */
			if (args.length == 2 && (args[0].equalsIgnoreCase("promote") || args[0].equalsIgnoreCase("promover"))) {
				return;
			}

			/* Party: Leave */
			if (args.length == 1 && (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("sair"))) {
				BungeeParty party = (BungeeParty) BattlebitsAPI.getPartyCommon().getParty(player.getUniqueId());

				if (party != null) {
					if (!party.getOwner().equals(player.getUniqueId())) {
						party.getMembers().remove(player.getUniqueId());
						party.getPromoted().remove(player.getUniqueId());

						DataParty.saveRedisPartyField(party, "members");
						DataParty.saveRedisPartyField(party, "promoted");
						DataParty.saveRedisParty(party);
					} else {
						party.sendMessage(prefix, "command-party-owner-leave", new String[] { "%player%", player.getName() });
						BattlebitsAPI.getPartyCommon().removeParty(party);
						DataParty.unloadParty(party);
						DataParty.disbandParty(party);
					}
				} else {
					player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-is-not-in-party")));
				}

				return;
			}

			/* Party: List */
			if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
				BungeeParty party = (BungeeParty) BattlebitsAPI.getPartyCommon().getParty(player.getUniqueId());

				if (party != null) {
					List<String> names = new ArrayList<>();

					for (UUID uuid : Stream.concat(Stream.of(party.getOwner()), party.getMembers().stream()).toArray(UUID[]::new)) {
						ProxiedPlayer member = ProxyServer.getInstance().getPlayer(uuid);

						if (member != null)
							names.add(member.getName());
					}

					if (!names.isEmpty()) {
						Collections.sort(names, String.CASE_INSENSITIVE_ORDER);

						// show player list
					}
				} else {
					player.sendMessage(TextComponent.fromLegacyText(prefix + T.t(BungeeMain.getPlugin(), cmdArgs.getLanguage(), "command-party-is-not-in-party")));
				}

				return;
			}
		}
	}
}
