package br.com.battlebits.commons.bukkit.command;

import org.bukkit.command.CommandSender;

import br.com.battlebits.commons.core.command.CommandArgs;

public class BukkitCommandArgs extends CommandArgs {

	protected BukkitCommandArgs(CommandSender sender, String label, String[] args, int subCommand) {
		super(new BukkitCommandSender(sender), label, args, subCommand);
	}

	@Override
	public boolean isPlayer() {
		return false;
	}

}
