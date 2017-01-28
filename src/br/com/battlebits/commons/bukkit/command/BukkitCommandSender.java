package br.com.battlebits.commons.bukkit.command;

import java.util.UUID;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.core.command.CommandSender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;

@AllArgsConstructor
@Getter
public class BukkitCommandSender implements CommandSender {

	private org.bukkit.command.CommandSender sender;

	@Override
	public void sendMessage(String str) {
		sender.sendMessage(str);
	}

	@Override
	public void sendMessage(BaseComponent str) {
		sender.sendMessage(str.toLegacyText());
	}

	@Override
	public void sendMessage(BaseComponent[] fromLegacyText) {

	}

	@Override
	public UUID getUniqueId() {
		if (sender instanceof Player)
			return ((Player) sender).getUniqueId();
		return UUID.randomUUID();
	}

}