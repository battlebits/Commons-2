package br.com.battlebits.commons.bungee.command.register;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.bungee.command.BungeeCommandArgs;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.commons.core.translate.Translate;
import br.com.battlebits.commons.core.twitter.Twitter;
import br.com.battlebits.commons.core.twitter.TwitterAccount;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import twitter4j.TwitterException;

public class TwitterCommand implements CommandClass {

	@Command(name = "broadcast", aliases = { "bc", "alert",
			"tweet" }, groupToUse = Group.MODPLUS, noPermMessageId = "command-broadcast-no-access")
	public void tweet(BungeeCommandArgs cmdArgs) {
		Language language = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			language = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId())
					.getLanguage();
		}
		String[] args = cmdArgs.getArgs();
		if (args.length <= 0) {
			cmdArgs.getSender().sendMessage(TextComponent.fromLegacyText(Translate
					.getTranslation(language, "command-broadcast-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			String espaco = " ";
			if (i >= args.length - 1)
				espaco = "";
			builder.append(args[i] + espaco);
		}
		BungeeMain.getPlugin().getProxy().broadcast(TextComponent.fromLegacyText(""));
		BungeeMain.getPlugin().getProxy().broadcast(TextComponent.fromLegacyText(
				Translate.getTranslation(language, "broadcast") + " " + ChatColor.WHITE + builder.toString()));
		BungeeMain.getPlugin().getProxy().broadcast(TextComponent.fromLegacyText(""));
		try {
			if (Twitter.tweet(TwitterAccount.BATTLEBITSMC, builder.toString())) {
				cmdArgs.getSender().sendMessage(TextComponent.fromLegacyText(
						T.t(language, "tweet-success", new String[] { "%message%", builder.toString() })));
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}

	}

}
