package br.com.battlebits.commons.bukkit.command.register;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.bukkit.command.BukkitCommandArgs;
import br.com.battlebits.commons.bukkit.menu.account.AccountMenu;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.T;

public class AccountCommand implements CommandClass {

	@Command(name = "account", aliases = { "acc", "conta" })
	public void account(BukkitCommandArgs args) {
		BattlePlayer player = BattlePlayer.getPlayer(args.getPlayer().getUniqueId());
		new AccountMenu(args.getPlayer(), player, player);
	}

	@Command(name = "language", aliases = { "lang" })
	public void language(BukkitCommandArgs args) {
		if (!args.isPlayer()) {
			args.getSender().sendMessage("Apenas jogadores");
			return;
		}
		Player p = args.getPlayer();
		if (args.getArgs().length < 1) {
			p.sendMessage("§%command-language-prefix%§ §%command-language-usage%§");
			return;
		}
		Language lang;
		try {
			lang = Language.valueOf(args.getArgs()[0]);
		} catch (Exception e) {
			p.sendMessage("§%command-language-prefix%§ §%command-language-not-language%§");
			return;
		}
		BattlePlayer player = BattlePlayer.getPlayer(p.getUniqueId());
		if (player.getLanguage() == lang) {
			p.sendMessage("§%command-language-prefix%§ §%command-language-already-using%§");
			return;
		}
		player.setLanguage(lang);
		p.sendMessage("§%command-language-prefix%§ " + T.t(BukkitMain.getInstance(), lang, "command-language-success", new String[] { "%language%", lang.toString() }));
	}

	@Command(name = "preferences", aliases = { "prefs", "preferencias" })
	public void preferences(BukkitCommandArgs args) {

	}

}
