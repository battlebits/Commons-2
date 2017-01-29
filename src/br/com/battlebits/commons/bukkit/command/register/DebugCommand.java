package br.com.battlebits.commons.bukkit.command.register;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.command.CommandArgs;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.data.DataServer;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.Translate;

public class DebugCommand implements CommandClass {
	@Command(name = "reloadbukkittranslation", usage = "/<command>", aliases = { "rlbukkittranslations",
			"rlbukkit" }, groupToUse = Group.DONO, noPermMessageId = "command-no-access", runAsync = true)
	public void reloadbungeetranslation(CommandArgs cmdArgs) {
		try {
			for (Language lang : Language.values()) {
				Translate.loadTranslations(BattlebitsAPI.TRANSLATION_ID, lang, DataServer.loadTranslation(lang));
			}
			cmdArgs.getSender().sendMessage("Traduções BUKKIT recarregadas");
		} catch (Exception e) {
			e.printStackTrace();
			cmdArgs.getSender().sendMessage("Falha ao recarregar traduções");
		}
	}

	@Command(name = "raminfo", usage = "/<command>", groupToUse = Group.DONO, noPermMessageId = "command-no-access")
	public void raminfo(CommandArgs cmdArgs) {
		double total = Runtime.getRuntime().maxMemory();
		double free = Runtime.getRuntime().freeMemory();
		double used = total - free;

		double divisor = 1024 * 1024 * 1024;
		double usedPercentage = (used / total) * 100;

		cmdArgs.getSender().sendMessage((total / divisor) + "GB de memoria RAM Maxima");

		cmdArgs.getSender().sendMessage((used / divisor) + "GB de memoria RAM Usada");
		cmdArgs.getSender().sendMessage((free / divisor) + "GB de memoria RAM Livre");
		cmdArgs.getSender().sendMessage(usedPercentage + "% da memoria RAM");
	}
}
