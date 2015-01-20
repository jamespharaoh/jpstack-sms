package wbs.sms.message.inbox.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.command.model.CommandTypeRec;

@SingletonComponent ("commandManagerImpl")
public
class CommandManagerImpl
	implements CommandManagerMethods {

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

	@Inject
	Map<String,Provider<CommandHandler>> commandTypeHandlersByBeanName =
		Collections.emptyMap ();

	private
	Map<String,String> commandTypeHandlerBeanNamesByCommandType;

	public
	CommandHandler get (
			CommandTypeRec commandType) {

		return get (
			commandType.getParentObjectType ().getCode (),
			commandType.getCode ());

	}

	public
	CommandHandler get (
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

	@PostConstruct
	public
	void init ()
		throws Exception {

		proxy.setDelegate (this);

		commandTypeHandlerBeanNamesByCommandType =
			new HashMap<String,String> ();

		for (Map.Entry<String,Provider<CommandHandler>> ent
				: commandTypeHandlersByBeanName.entrySet ()) {

			String beanName =
				ent.getKey ();

			CommandHandler commandTypeHandler =
				ent.getValue ().get ();

			String[] commandTypes =
				commandTypeHandler.getCommandTypes ();

			if (commandTypes == null) {

				throw new NullPointerException (
					stringFormat (
						"Command type handler factory %s returned null from ",
						beanName,
						"getCommandTypes ()"));

			}

			for (String commandType
					: commandTypeHandler.getCommandTypes ()) {

				commandTypeHandlerBeanNamesByCommandType.put (
					commandType,
					beanName);

			}

		}

	}

	@Override
	public
	void handle (
			int commandId,
			ReceivedMessage message) {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		CommandRec command =
			commandHelper.find (commandId);

		CommandTypeRec commandType =
			command.getCommandType ();

		CommandHandler handler =
			get (commandType);

		transaction.close ();

		handler.handle (
			commandId,
			message);

	}

	@Override
	public
	void handle (
			int commandId,
			ReceivedMessage message,
			String rest) {

		handle (
			commandId,
			new ReceivedMessageImpl (
				message,
				rest));

	}

}
