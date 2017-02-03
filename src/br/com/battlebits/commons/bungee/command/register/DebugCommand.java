package br.com.battlebits.commons.bungee.command.register;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.ClearRedis;
import br.com.battlebits.commons.TranslationLoad;
import br.com.battlebits.commons.bungee.command.BungeeCommandArgs;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.data.DataServer;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.Translate;
import net.md_5.bungee.api.chat.TextComponent;

public class DebugCommand implements CommandClass {
	@Command(name = "reloadbungeetranslation", usage = "/<command>", aliases = { "rlbungeetranslations",
			"rlbungee" }, groupToUse = Group.DONO, noPermMessageId = "command-no-access", runAsync = true)
	public void reloadbungeetranslation(BungeeCommandArgs cmdArgs) {
		for (Language lang : Language.values()) {
			Translate.loadTranslations(BattlebitsAPI.TRANSLATION_ID, lang, DataServer.loadTranslation(lang));
		}
		cmdArgs.getSender().sendMessage(TextComponent.fromLegacyText("Traduções BUNGEE recarregadas"));
	}

	@Command(name = "installtranslations", usage = "/<command>", aliases = {}, groupToUse = Group.DONO, noPermMessageId = "command-no-access")
	public void installtranslations(BungeeCommandArgs cmdArgs) {
		TranslationLoad.main(new String[0]);
	}

	@Command(name = "clearredis", usage = "/<command>", aliases = {}, groupToUse = Group.DONO, noPermMessageId = "command-no-access")
	public void clearredis(BungeeCommandArgs cmdArgs) {
		ClearRedis.main(new String[0]);
	}
}
