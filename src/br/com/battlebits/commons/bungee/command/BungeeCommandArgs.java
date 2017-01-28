package br.com.battlebits.commons.bungee.command;

import br.com.battlebits.commons.core.command.CommandArgs;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeCommandArgs extends CommandArgs {

	private CommandSender sender;

	protected BungeeCommandArgs(CommandSender sender, String label, String[] args, int subCommand) {
		super(new BungeeCommandSender(sender), label, args, subCommand);
	}

	@Override
	public boolean isPlayer() {
		return sender instanceof ProxiedPlayer;
	}

	public ProxiedPlayer getPlayer() {
		if (!isPlayer())
			return null;
		return (ProxiedPlayer) sender;
	}

}
