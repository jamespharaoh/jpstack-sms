package wbs.sms.magicnumber.daemon;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.command.model.CommandRec;
import wbs.sms.magicnumber.model.MagicNumberObjectHelper;
import wbs.sms.magicnumber.model.MagicNumberRec;
import wbs.sms.magicnumber.model.MagicNumberUseObjectHelper;
import wbs.sms.magicnumber.model.MagicNumberUseRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.CommandManager;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

@Accessors (fluent = true)
@PrototypeComponent ("magicNumberCommandTypeHandler")
public
class MagicNumberCommandTypeHandler
	implements CommandHandler {

	// singleton dependencies

	@WeakSingletonDependency
	CommandManager commandManager;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MagicNumberObjectHelper magicNumberHelper;

	@SingletonDependency
	MagicNumberUseObjectHelper magicNumberUseHelper;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	MessageObjectHelper smsMessageHelper;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Long> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"root.magic_number"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handle");

		MessageRec message =
			inbox.getMessage ();

		// lookup the MagicNumber

		MagicNumberRec magicNumber =
			magicNumberHelper.findByNumber (
				message.getNumTo ());

		if (magicNumber == null) {

			return smsInboxLogic.inboxNotProcessed (
				taskLogger,
				inbox,
				optionalAbsent (),
				optionalAbsent (),
				optionalOf (
					command),
				stringFormat (
					"Magic number does not exist",
					message.getNumTo ()));

		}

		// lookup the MagicNumberUse

		MagicNumberUseRec magicNumberUse =
			magicNumberUseHelper.find (
				magicNumber,
				message.getNumber ());

		if (magicNumberUse == null) {

			return smsInboxLogic.inboxNotProcessed (
				taskLogger,
				inbox,
				optionalAbsent (),
				optionalAbsent (),
				optionalOf (
					command),
				"Magic number has not been used");

		}

		// and delegate

		return commandManager.handle (
			taskLogger,
			inbox,
			magicNumberUse.getCommand (),
			Optional.of (
				magicNumberUse.getRefId ()),
			rest);

	}

}
