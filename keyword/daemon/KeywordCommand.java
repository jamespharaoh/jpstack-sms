package wbs.sms.keyword.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.keyword.logic.KeywordLogic;
import wbs.sms.keyword.model.KeywordRec;
import wbs.sms.keyword.model.KeywordSetFallbackObjectHelper;
import wbs.sms.keyword.model.KeywordSetFallbackRec;
import wbs.sms.keyword.model.KeywordSetObjectHelper;
import wbs.sms.keyword.model.KeywordSetRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.CommandManager;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.logic.InboxLogic;

@Log4j
@PrototypeComponent ("keywordCommand")
public
class KeywordCommand
	implements CommandHandler {

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	KeywordFinder keywordFinder;

	@Inject
	KeywordLogic keywordLogic;

	@Inject
	KeywordSetFallbackObjectHelper keywordSetFallbackHelper;

	@Inject
	KeywordSetObjectHelper keywordSetHelper;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	CommandManager commandManager;

	Status status;

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"keyword_set.default"
		};

	}

	@Override
	public
	Status handle (
			int commandId,
			ReceivedMessage receivedMessage) {

		if (log.isDebugEnabled ()) {

			log.debug (
				stringFormat (
					"About to handle message %s with command %s",
					receivedMessage.getMessageId (),
					commandId));

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		CommandRec invokedCommand =
			commandHelper.find (
				commandId);

		KeywordSetRec keywordSet =
			keywordSetHelper.find (
				invokedCommand.getParentObjectId ());

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		// try and find a keyword

		for (
			KeywordFinder.Match match
				: keywordFinder.find (
					receivedMessage.getRest ())
		) {

			String keyword =
				match.simpleKeyword ();

			// lookup keyword

			KeywordRec keywordRecord =
				keywordSet.getKeywords ().get (keyword);

			if (keywordRecord == null)
				continue;

			CommandRec nextCommand =
				keywordRecord.getCommand ();

			if (nextCommand == null) {

				log.info (
					stringFormat (
						"%s has no command, not processing %s",
						keywordRecord,
						message));

				inboxLogic.inboxNotProcessed (
					message,
					null,
					null,
					invokedCommand,
					stringFormat (
						"No command for %s",
						keywordRecord));

				transaction.commit ();

				return null;

			}

			String messageRest =
				keywordRecord.getLeaveIntact ()
					? receivedMessage.getRest ()
					: match.rest ();

			if (log.isDebugEnabled ()) {

				log.debug (
					stringFormat (
						"Found keyword %s for message %s",
						keywordRecord.getId (),
						receivedMessage.getMessageId ()));

			}

			// set fallback if sticky

			if (keywordRecord.getSticky ()) {

				keywordLogic.createOrUpdateKeywordSetFallback (
					keywordSet,
					message.getNumber (),
					nextCommand);

			}

			// hand off

			transaction.commit ();

			return commandManager.handle (
				nextCommand.getId (),
				receivedMessage,
				messageRest);

		}

		// ok that didn't work, try a fallback thingy

		KeywordSetFallbackRec keywordSetFallback =
			keywordSetFallbackHelper.find (
				keywordSet,
				message.getNumber ());

		if (keywordSetFallback != null) {

			if (log.isDebugEnabled ()) {

				log.debug (
					stringFormat (
						"Using keyword set fallback %s for message %s",
						keywordSetFallback.getId (),
						receivedMessage.getMessageId ()));

			}

			CommandRec nextCommand =
				keywordSetFallback.getCommand ();

			transaction.close ();

			return commandManager.handle (
				nextCommand.getId (),
				receivedMessage);

		}

		// then try the keyword set's fallback

		if (keywordSet.getFallbackCommand () != null) {

			if (log.isDebugEnabled ()) {

				log.debug (
					stringFormat (
						"Using fallback command for message %s",
						receivedMessage.getMessageId ()));

			}

			CommandRec nextCommand =
				keywordSet.getFallbackCommand ();

			transaction.close ();

			return commandManager.handle (
				nextCommand.getId (),
				receivedMessage);

		}

		// mark as not processed

		if (log.isDebugEnabled ()) {

			log.debug (
				stringFormat (
					"Marking message %s as not processed",
					receivedMessage.getMessageId ()));

		}

		inboxLogic.inboxNotProcessed (
			message,
			null,
			null,
			invokedCommand,
			stringFormat (
				"No keyword matched in %s",
				keywordSet));

		transaction.commit ();

		// fail to handle

		return null;

	}

}
