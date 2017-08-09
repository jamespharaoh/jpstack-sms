package wbs.sms.message.inbox.daemon;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

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
	Map <String, ComponentProvider <CommandHandler>>
		commandTypeHandlersByBeanName;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogLogic exceptionLogic;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	Map <String, String> commandTypeHandlerBeanNamesByCommandType;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			commandTypeHandlerBeanNamesByCommandType =
				new HashMap <String, String> ();

			for (
				Map.Entry <String, ComponentProvider <CommandHandler>> entry
					: commandTypeHandlersByBeanName.entrySet ()
			) {

				String beanName =
					entry.getKey ();

				CommandHandler commandTypeHandler =
					entry.getValue ().provide (
						taskLogger);

				String[] commandTypes =
					commandTypeHandler.getCommandTypes ();

				if (commandTypes == null) {

					throw new NullPointerException (
						stringFormat (
							"Command type handler factory %s ",
							beanName,
							"returned null from getCommandTypes ()"));

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

	}

	// implementation

	public
	CommandHandler getHandler (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull CommandTypeRec commandType) {

		return getHandler (
			parentTaskLogger,
			commandType.getParentType ().getCode (),
			commandType.getCode ());

	}

	public
	CommandHandler getHandler (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String parentObjectTypeCode,
			@NonNull String commandTypeCode) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getHandler");

		) {

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

			return genericCastUnchecked (
				componentManager.getComponentRequired (
					taskLogger,
					beanName,
					CommandHandler.class));

		}

	}

	@Override
	public
	InboxAttemptRec handle (
			@NonNull Transaction parentTransaction,
			@NonNull InboxRec inbox,
			@NonNull CommandRec command,
			@NonNull Optional<Long> ref,
			@NonNull String rest) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"handle");

		) {

			return getHandler (
				transaction,
				command.getCommandType ())

				.inbox (
					inbox)

				.command (
					command)

				.commandRef (
					ref)

				.rest (
					rest)

				.handle (
					transaction);

		}

	}

}
