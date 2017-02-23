package br.com.battlebits.commons.util.string;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

public class StringLoreUtils {

	public static List<String> formatForLore(String text) {
		return getLore(30, text);
	}

	public static List<String> getLore(int max, String text) {
		List<String> lore = new ArrayList<>();
		text = ChatColor.translateAlternateColorCodes('&', text);
		String[] split = text.split(" ");
		String color = "";
		text = "";
		for (int i = 0; i < split.length; i++) {
			if (ChatColor.stripColor(text).length() >= max || ChatColor.stripColor(text).endsWith(".")
					|| ChatColor.stripColor(text).endsWith("!")) {
				lore.add(text);
				if (text.endsWith(".") || text.endsWith("!"))
					lore.add("");
				text = color;
			}
			String toAdd = split[i];
			if (toAdd.contains("§"))
				color = ChatColor.getLastColors(toAdd.toLowerCase());
			if (toAdd.contains("\n")) {
				toAdd = toAdd.substring(0, toAdd.indexOf("\n"));
				split[i] = split[i].substring(toAdd.length() + 1);
				lore.add(text + (text.length() == 0 ? "" : " ") + toAdd);
				text = color;
				i--;
			} else {
				text += (ChatColor.stripColor(text).length() == 0 ? "" : " ") + toAdd;
			}
		}
		lore.add(text);
		return lore;
	}

	public static void main(String[] args) {
		String str = "§7§l➟ ✪ LEGENDARY\n§7§l➟ ✫ MASTER\n§7§l➟ ✹ ELITE\n§7§l➟ ✦ DIAMOND\n§7§l➟ ✷ GOLD\n§7§l➟ ✶ SILVER\n§7§l➟ ☷ EXPERT\n§7§l➟ ☲ ADVANCED\n§7§l➟ ☰ PRIMARY\n§6§l➟ §f- UNRANKED";
		for(String s : getLore(30, str))
			System.out.println(s);
	}

}
