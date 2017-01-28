package br.com.battlebits.commons.core.command;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.translate.Language;

public abstract class CommandArgs {

	private final CommandSender sender;
	private final String label;
	private final String[] args;

	protected CommandArgs(CommandSender sender, String label, String[] args, int subCommand) {
		String[] modArgs = new String[args.length - subCommand];
		System.arraycopy(args, 0 + subCommand, modArgs, 0, args.length - subCommand);

		StringBuilder buffer = new StringBuilder();
		buffer.append(label);
		for (int x = 0; x < subCommand; x++) {
			buffer.append(".").append(args[x]);
		}
		String cmdLabel = buffer.toString();
		this.sender = sender;
		this.label = cmdLabel;
		this.args = modArgs;
	}

	public CommandSender getSender() {
		return sender;
	}

	public String getLabel() {
		return label;
	}

	public String[] getArgs() {
		return args;
	}

	public abstract boolean isPlayer();

	public Language getLanguage() {
		if (isPlayer()) {
			return BattlePlayer.getLanguage(getSender().getUniqueId());
		}
		return BattlebitsAPI.getDefaultLanguage();
	}

}
