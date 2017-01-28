package br.com.battlebits.commons.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.battlebits.commons.core.permission.Group;

public interface CommandFramework {

	public void registerCommands(CommandClass commandClass);

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Command {

		public String name();

		public Group groupToUse() default Group.NORMAL;

		public String noPermMessageId() default "command-no-access";

		public String[] aliases() default {};

		public String description() default "";

		public String usage() default "";

		public boolean runAsync() default false;
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Completer {

		/**
		 * The command that this completer completes. If it is a sub command
		 * then its values would be separated by periods. ie. a command that
		 * would be a subcommand of test would be 'test.subcommandname'
		 * 
		 * @return
		 */
		String name();

		/**
		 * A list of alternate names that the completer is executed under. See
		 * name() for details on how names work
		 * 
		 * @return
		 */
		String[] aliases() default {};

	}

}
