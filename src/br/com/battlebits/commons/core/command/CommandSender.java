package br.com.battlebits.commons.core.command;

import java.util.UUID;

import net.md_5.bungee.api.chat.BaseComponent;

public interface CommandSender {

	public abstract UUID getUniqueId();
	
	public abstract void sendMessage(String str);
	
	public abstract void sendMessage(BaseComponent str);

	public abstract void sendMessage(BaseComponent[] fromLegacyText);
}
