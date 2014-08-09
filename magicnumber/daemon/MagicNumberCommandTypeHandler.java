package wbs.sms.magicnumber.daemon;

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

@PrototypeComponent ("magicNumberCommandTypeHandler")
public
class MagicNumberCommandTypeHandler
	implements CommandHandler {

	@Inject
	Database database;

	@Inject
	MagicNumberObjectHelper magicNumberHelper;

	@Inject
	MagicNumberUseObjectHelper magicNumberUseHelper;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	CommandManager commandManager;

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"root.magic_number"
		};

	}

	@Override
	public
	Status handle (
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

		if (magicNumber == null)
			return Status.notprocessed;

		// lookup the MagicNumberUse

		MagicNumberUseRec magicNumberUse =
			magicNumberUseHelper.find (
				magicNumber,
				message.getNumber ());

		if (magicNumberUse == null)
			return Status.notprocessed;

		// and delegate

		transaction.close ();

		return commandManager.handle (
			magicNumberUse.getCommand ().getId (),
			new ReceivedMessageImpl (
				receivedMessage,
				receivedMessage.getMessageId (),
				receivedMessage.getRest (),
				magicNumberUse.getRefId ()));

	}

}
