package br.com.battlebits.commons.core.command;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.util.ClassGetter;

public class CommandLoader {
	private CommandFramework framework;

	public CommandLoader(CommandFramework framework) {
		this.framework = framework;
	}

	public int loadCommandsFromPackage(String packageName) {
		int i = 0;
		for (Class<?> commandClass : ClassGetter.getClassesForPackage(framework.getJarClass(), packageName)) {
			if (CommandClass.class.isAssignableFrom(commandClass)) {
				try {
					CommandClass commands = (CommandClass) commandClass.newInstance();
					framework.registerCommands(commands);
				} catch (Exception e) {
					e.printStackTrace();
					BattlebitsAPI.getLogger()
							.warning("Erro ao carregar comandos da classe " + commandClass.getSimpleName() + "!");
				}
				i++;
			}
		}
		return i;
	}
}
