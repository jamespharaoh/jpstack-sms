package wbs.sms.message.inbox.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.database.Database;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.command.model.CommandTypeRec;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

import com.google.common.base.Optional;

@SingletonComponent ("commandManagerImpl")
public
class CommandManagerImpl
	implements CommandManagerMethods {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	CommandManagerProxy proxy;

	// collection dependencies

	@Inject
	Map<String,Provider<CommandHandler>> commandTypeHandlersByBeanName =
		Collections.emptyMap ();

	// state

	Map<String,String> commandTypeHandlerBeanNamesByCommandType;

	// life cycle

	@PostConstruct
	public
	void init ()
		throws Exception {

		proxy.setDelegate (this);

		commandTypeHandlerBeanNamesByCommandType =
			new HashMap<String,String> ();

		for (
			Map.Entry<String,Provider<CommandHandler>> entry
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
			commandType.getParentObjectType ().getCode (),
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
			applicationContext.getBean (
				beanName,
				CommandHandler.class);

	}

	@Override
	public
	InboxAttemptRec handle (
			@NonNull InboxRec inbox,
			@NonNull CommandRec command,
			@NonNull Optional<Integer> ref,
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
