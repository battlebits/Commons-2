package br.com.battlebits.commons.bukkit.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import br.com.battlebits.commons.core.command.CommandArgs;

public class BukkitCommandArgs extends CommandArgs {

	protected BukkitCommandArgs(CommandSender sender, String label, String[] args, int subCommand) {
		super(new BukkitCommandSender(sender), label, args, subCommand);
	}

	@Override
	public boolean isPlayer() {
		return ((BukkitCommandSender) getSender()).getSender() instanceof Player;
	}

	public Player getPlayer() {
		if (!isPlayer())
			return null;
		return (Player) ((BukkitCommandSender) getSender()).getSender();
	}

}
