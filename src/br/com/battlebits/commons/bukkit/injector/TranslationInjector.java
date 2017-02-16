package br.com.battlebits.commons.bukkit.injector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.google.common.base.Splitter;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.commons.util.string.StringLoreUtils;

public class TranslationInjector implements Injector {
	private Pattern finder = Pattern.compile("§%(([\\S^)]+)%§)");

	@Override
	public void inject(BukkitMain plugin) {
		plugin.getProcotolManager()
				.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, //
						PacketType.Play.Server.CHAT, //
						PacketType.Play.Server.WINDOW_ITEMS, //
						PacketType.Play.Server.SET_SLOT, //
						PacketType.Play.Server.OPEN_WINDOW, //
						PacketType.Play.Server.SCOREBOARD_OBJECTIVE, //
						PacketType.Play.Server.SCOREBOARD_TEAM, //
						PacketType.Play.Server.SCOREBOARD_SCORE, //
						PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER, //
						PacketType.Play.Server.SPAWN_ENTITY_LIVING, //
						PacketType.Play.Server.ENTITY_METADATA, //
						PacketType.Play.Server.TITLE) {
					@SuppressWarnings("deprecation")
					@Override
					public void onPacketSending(PacketEvent event) {
						if (event.getPlayer() == null)
							return;
						if (event.getPlayer().getUniqueId() == null)
							return;
						if (event.getPacket() == null)
							return;
						if (event.isReadOnly())
							return;
						Language lang = BattlePlayer.getLanguage(event.getPlayer().getUniqueId());
						if (event.getPacketType() == PacketType.Play.Server.CHAT) {
							PacketContainer packet = event.getPacket().deepClone();
							for (int i = 0; i < packet.getChatComponents().size(); i++) {
								WrappedChatComponent chatComponent = packet.getChatComponents().read(i);
								if (chatComponent != null) {
									packet.getChatComponents().write(i,
											WrappedChatComponent.fromJson(translate(chatComponent.getJson(), lang)));
								}
							}
							event.setPacket(packet);
						} else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
							PacketContainer packet = event.getPacket().deepClone();
							for (ItemStack item : packet.getItemArrayModifier().read(0)) {
								if (item == null) {
									continue;
								}
								translateItemStack(item, lang);
							}
							event.setPacket(packet);
						} else if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
							PacketContainer packet = event.getPacket().deepClone();
							ItemStack item = packet.getItemModifier().read(0);
							packet.getItemModifier().write(0, translateItemStack(item, lang));
							event.setPacket(packet);
						} else if (event.getPacketType() == PacketType.Play.Server.TITLE) {
							PacketContainer packet = event.getPacket().deepClone();
							WrappedChatComponent component = event.getPacket().getChatComponents().read(0);
							if (component == null)
								return;
							packet.getChatComponents().write(0,
									WrappedChatComponent.fromJson(translate(component.getJson(), lang)));
							event.setPacket(packet);
							return;
						} else if (event.getPacketType() == PacketType.Play.Server.SCOREBOARD_SCORE) {
							PacketContainer packet = event.getPacket().deepClone();
							String message = event.getPacket().getStrings().read(0);
							packet.getStrings().write(0, translate(message, lang));
							event.setPacket(packet);
						} else if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
							PacketContainer packet = event.getPacket().deepClone();
							WrappedChatComponent component = event.getPacket().getChatComponents().read(0);
							String message = translate(
									BattlebitsAPI.getParser().parse(component.getJson()).getAsString(), lang);
							message = message.substring(0, message.length() > 32 ? 32 : message.length());
							packet.getChatComponents().write(0, WrappedChatComponent.fromText(message));
							event.setPacket(packet);
						} else if (event.getPacketType() == PacketType.Play.Server.SCOREBOARD_OBJECTIVE) {
							PacketContainer packet = event.getPacket().deepClone();
							String message = event.getPacket().getStrings().read(1);
							packet.getStrings().write(1, translate(message, lang));
							event.setPacket(packet);
						} else if (event.getPacketType() == PacketType.Play.Server.SCOREBOARD_TEAM) {
							PacketContainer packet = event.getPacket().deepClone();
							String pre = packet.getStrings().read(2);
							String su = packet.getStrings().read(3);
							boolean matched = false;
							Matcher matcher = finder.matcher(pre);
							while (matcher.find()) {
								pre = pre.replace(matcher.group(), T.t(lang, matcher.group(2)));
								matched = true;
							}
							matcher = finder.matcher(su);
							while (matcher.find()) {
								su = su.replace(matcher.group(), T.t(lang, matcher.group(2)));
								matched = true;
							}
							if (matched) {
								if (pre.length() <= 16 && su.length() <= 16) {
									packet.getStrings().write(2, pre);
									packet.getStrings().write(3, su);
									event.setPacket(packet);
									return;
								}
							}
							String text = packet.getStrings().read(2) + packet.getStrings().read(3);
							matcher = finder.matcher(text);
							matched = false;
							while (matcher.find()) {
								text = text.replace(matcher.group(), T.t(lang, matcher.group(2)));
								matched = true;
							}
							if (!matched)
								return;
							String prefix = "";
							String suffix = "";
							Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
							String str = iterator.next();
							if (str.endsWith("§")) {
								str = str.substring(0, str.length() - 1);
								prefix = str;
								if (iterator.hasNext()) {
									String next = iterator.next();
									if (!next.startsWith("§")) {
										String str2 = "§" + next;
										if (str2.length() > 16)
											str2 = str2.substring(0, 16);
										suffix = str2;
									} else {
										suffix = next;
									}
								} else {
									suffix = "";
								}
							} else if (iterator.hasNext()) {
								String next = iterator.next();
								if (!next.startsWith("§")) {
									String colors = ChatColor.getLastColors(str);
									String str3 = colors + next;
									if (str3.length() > 16)
										str3 = str3.substring(0, 16);
									prefix = str;
									suffix = str3;
								} else {
									prefix = str;
									suffix = next;
								}
							} else {
								prefix = str;
								suffix = "";
							}
							packet.getStrings().write(2, prefix);
							packet.getStrings().write(3, suffix);
							event.setPacket(packet);
						} else if (event.getPacketType() == PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER) {
							PacketContainer packet = event.getPacket().deepClone();
							WrappedChatComponent header = packet.getChatComponents().read(0);
							WrappedChatComponent footer = packet.getChatComponents().read(1);
							if (header != null)
								packet.getChatComponents().write(0,
										WrappedChatComponent.fromJson(translate(header.getJson(), lang)));
							if (footer != null)
								packet.getChatComponents().write(1,
										WrappedChatComponent.fromJson(translate(footer.getJson(), lang)));
							event.setPacket(packet);
						} else if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
							PacketContainer packet = event.getPacket().deepClone();
							List<WrappedWatchableObject> objects = packet.getWatchableCollectionModifier().read(0);
							for (WrappedWatchableObject obj : objects) {
								if (obj.getIndex() == 2) {
									String str = (String) obj.getRawValue();
									str = translate(str, lang);
									obj.setValue(str);
									break;
								}
							}
							event.setPacket(packet);
						} else if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY_LIVING) {
							PacketContainer packet = event.getPacket();

							int type = packet.getIntegers().read(1);

							if (type != EntityType.ARMOR_STAND.getTypeId()) {
								return;
							}
							PacketContainer packetClone = event.getPacket().deepClone();
							List<WrappedWatchableObject> objects = packetClone.getDataWatcherModifier().read(0)
									.getWatchableObjects();
							for (WrappedWatchableObject obj : objects) {
								if (obj.getIndex() == 2) {
									String str = (String) obj.getRawValue();
									str = translate(str, lang);
									obj.setValue(str);
									break;
								}
							}
							event.setPacket(packetClone);
						}
					}

				});
	}

	private ItemStack translateItemStack(ItemStack iS, Language lang) {
		if (iS == null)
			return iS;
		ItemMeta meta = iS.getItemMeta();
		if (meta != null) {
			if (meta.hasDisplayName()) {
				String message = meta.getDisplayName();
				message = translate(message, lang);
				meta.setDisplayName(message);
			}
			if (meta.hasLore()) {
				List<String> newlore = new ArrayList<>();
				for (String message : meta.getLore()) {
					message = translate(message, lang);
					if (message.contains("\n")) {
						for (String s : message.split("\n"))
							newlore.addAll(StringLoreUtils.formatForLore(s));
					} else {
						newlore.addAll(StringLoreUtils.formatForLore(message));
					}
					message = null;
				}
				meta.setLore(newlore);
				newlore = null;
			}
			iS.setItemMeta(meta);
		}
		return iS;
	}

	private String translate(String message, Language lang) {
		if (message == null || message.isEmpty())
			return "";
		Matcher matcher = finder.matcher(message);
		while (matcher.find()) {
			message = message.replace(matcher.group(), T.t(lang, matcher.group(2).toLowerCase()));
		}
		return message;
	}

}
