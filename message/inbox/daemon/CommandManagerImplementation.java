package wbs.sms.message.inbox.daemon;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.database.Database;
import wbs.platform.exception.logic.ExceptionLogLogic;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.command.model.CommandTypeRec;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

@SingletonComponent ("commandManager")
public
class CommandManagerImplementation
	implements CommandManager {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Map <String, Provider <CommandHandler>> commandTypeHandlersByBeanName;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogLogic exceptionLogic;

	// state

	Map <String, String> commandTypeHandlerBeanNamesByCommandType;

	// life cycle

	@NormalLifecycleSetup
	public
	void init ()
		throws Exception {

		commandTypeHandlerBeanNamesByCommandType =
			new HashMap <String, String> ();

		for (
			Map.Entry <String, Provider <CommandHandler>> entry
				: commandTypeHandlersByBeanName.entrySet ()
		) {

			String beanName =
				entry.getKey ();

			CommandHandler commandTypeHandler =
				entry.getValue ().get ();

			String[] commandTypes =
				commandTypeHandler.getCommandTypes ();

			if (commandTypes == null) {

				throw new NullPointerException (
					stringFormat (
						"Command type handler factory %s returned null from ",
						beanName,
						"getCommandTypes ()"));

			}

			for (
				String commandType
					: commandTypeHandler.getCommandTypes ()
			) {

				commandTypeHandlerBeanNamesByCommandType.put (
					commandType,
					beanName);

			}

		}

	}

	// implementation

	public
	CommandHandler getHandler (
			CommandTypeRec commandType) {

		return getHandler (
			commandType.getParentType ().getCode (),
			commandType.getCode ());

	}

	public
	CommandHandler getHandler (
			String parentObjectTypeCode,
			String commandTypeCode) {

		String key =
			parentObjectTypeCode + "." + commandTypeCode;

		if (! commandTypeHandlerBeanNamesByCommandType.containsKey (key)) {

			throw new RuntimeException (
				stringFormat (
					"No command type handler for %s",
					key));

		}

		String beanName =
			commandTypeHandlerBeanNamesByCommandType.get (key);

		return (CommandHandler)
			componentManager.getComponentRequired (
				beanName,
				CommandHandler.class);

	}

	@Override
	public
	InboxAttemptRec handle (
			@NonNull InboxRec inbox,
			@NonNull CommandRec command,
			@NonNull Optional<Long> ref,
			@NonNull String rest) {

		return getHandler (
			command.getCommandType ())

			.inbox (
				inbox)

			.command (
				command)

			.commandRef (
				ref)

			.rest (
				rest)

			.handle ();

	}

}
