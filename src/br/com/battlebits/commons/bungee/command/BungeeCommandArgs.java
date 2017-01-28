package br.com.battlebits.commons.bungee.command;

import br.com.battlebits.commons.core.command.CommandArgs;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeCommandArgs extends CommandArgs {

	protected BungeeCommandArgs(CommandSender sender, String label, String[] args, int subCommand) {
		super(new BungeeCommandSender(sender), label, args, subCommand);
	}

	@Override
	public boolean isPlayer() {
		return ((BungeeCommandSender) getSender()).getSender() instanceof ProxiedPlayer;
	}

	public ProxiedPlayer getPlayer() {
		if (!isPlayer())
			return null;
		return (ProxiedPlayer) ((BungeeCommandSender) getSender()).getSender();
	}

}
