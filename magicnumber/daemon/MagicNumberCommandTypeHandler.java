package wbs.sms.magicnumber.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.sms.magicnumber.model.MagicNumberObjectHelper;
import wbs.sms.magicnumber.model.MagicNumberRec;
import wbs.sms.magicnumber.model.MagicNumberUseObjectHelper;
import wbs.sms.magicnumber.model.MagicNumberUseRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.CommandManager;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.daemon.ReceivedMessageImpl;
import wbs.sms.message.inbox.logic.InboxLogic;

@PrototypeComponent ("magicNumberCommandTypeHandler")
public
class MagicNumberCommandTypeHandler
	implements CommandHandler {

	// dependencies

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MagicNumberObjectHelper magicNumberHelper;

	@Inject
	MagicNumberUseObjectHelper magicNumberUseHelper;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	CommandManager commandManager;

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
	void handle (
			int commandId,
			ReceivedMessage receivedMessage) {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		// lookup the MagicNumber

		MagicNumberRec magicNumber =
			magicNumberHelper.findByNumber (
				message.getNumTo ());

		if (magicNumber == null) {

			inboxLogic.inboxNotProcessed (
				message,
				null,
				null,
				null,
				stringFormat (
					"Magic number does not exist",
					message.getNumTo ()));

			return;

		}

		// lookup the MagicNumberUse

		MagicNumberUseRec magicNumberUse =
			magicNumberUseHelper.find (
				magicNumber,
				message.getNumber ());

		if (magicNumberUse == null) {

			inboxLogic.inboxNotProcessed (
				message,
				null,
				null,
				null,
				"Magic number has not been used");

			return;

		}

		// and delegate

		transaction.close ();

		commandManager.handle (
			magicNumberUse.getCommand ().getId (),
			new ReceivedMessageImpl (
				receivedMessage,
				receivedMessage.getMessageId (),
				receivedMessage.getRest (),
				magicNumberUse.getRefId ()));

	}

}
