package wbs.sms.keyword.daemon;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

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
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

@Accessors (fluent = true)
@PrototypeComponent ("keywordCommand")
public
class KeywordCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@WeakSingletonDependency
	CommandManager commandManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	KeywordFinder keywordFinder;

	@SingletonDependency
	KeywordLogic keywordLogic;

	@SingletonDependency
	KeywordSetFallbackObjectHelper keywordSetFallbackHelper;

	@SingletonDependency
	KeywordSetObjectHelper keywordSetHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Long> commandRef;

	@Getter @Setter
	String rest;

	// state

	KeywordSetRec keywordSet;
	MessageRec message;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"keyword_set.default"
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

		taskLogger.debugFormat (
			"About to handle message %s ",
			integerToDecimalString (
				inbox.getId ()),
			"with command %s",
			integerToDecimalString (
				command.getId ()));

		keywordSet =
			keywordSetHelper.findRequired (
				command.getParentId ());

		message =
			inbox.getMessage ();

		// try and find a keyword

		Optional <Pair <KeywordRec, String>> matchResult =
			performMatch ();

		if (matchResult.isPresent ()) {

			KeywordRec keywordRecord =
				matchResult.get ().getLeft ();

			CommandRec nextCommand =
				keywordRecord.getCommand ();

			if (nextCommand == null) {

				taskLogger.noticeFormat (
					"Keyword %s has no command, not processing message %s",
					objectManager.objectPathMini (
						keywordRecord),
					integerToDecimalString (
						message.getId ()));

				return smsInboxLogic.inboxNotProcessed (
					inbox,
					optionalAbsent (),
					optionalAbsent (),
					optionalOf (
						command),
					stringFormat (
						"No command for keyword %s",
						objectManager.objectPathMini (
							keywordRecord)));

			}

			String messageRest =
				keywordRecord.getLeaveIntact ()
					? rest
					: matchResult.get ().getRight ();

			taskLogger.debugFormat (
				"Found keyword %s ",
				objectManager.objectPathMini (
					keywordRecord),
				"for message %s",
				integerToDecimalString (
					inbox.getId ()));

			// set fallback if sticky

			if (keywordRecord.getSticky ()) {

				keywordLogic.createOrUpdateKeywordSetFallback (
					keywordSet,
					message.getNumber (),
					nextCommand);

			}

			// hand off

			return commandManager.handle (
				taskLogger,
				inbox,
				nextCommand,
				Optional.<Long>absent (),
				messageRest);

		}

		// ok that didn't work, try a fallback thingy

		Optional <InboxAttemptRec> keywordSetFallbackResult =
			tryKeywordSetFallback (
				taskLogger);

		if (keywordSetFallbackResult.isPresent ())
			return keywordSetFallbackResult.get ();

		// then try the keyword set's fallback

		if (keywordSet.getFallbackCommand () != null) {

			taskLogger.debugFormat (
				"Using fallback command for message %s",
				integerToDecimalString (
					inbox.getId ()));

			CommandRec nextCommand =
				keywordSet.getFallbackCommand ();

			return commandManager.handle (
				taskLogger,
				inbox,
				nextCommand,
				optionalAbsent (),
				rest);

		}

		// mark as not processed

		taskLogger.debugFormat (
			"Marking message %s ",
			integerToDecimalString (
				inbox.getId ()),
			"as not processed");

		return smsInboxLogic.inboxNotProcessed (
			inbox,
			optionalAbsent (),
			optionalAbsent (),
			optionalOf (
				command),
			stringFormat (
				"No keyword matched in keyword set %s",
				objectManager.objectPathMini (
					keywordSet)));

	}

	Optional<Pair<KeywordRec,String>> performMatch () {

		switch (keywordSet.getType ()) {

		case keyword:
			return matchKeyword ();

		case numTo:
			return matchNumTo ();

		default:
			throw new RuntimeException ();

		}

	}

	Optional<Pair<KeywordRec,String>> matchKeyword () {

		for (
			KeywordFinder.Match match
				: keywordFinder.find (
					rest)
		) {

			String keyword =
				match.simpleKeyword ();

			// lookup keyword

			KeywordRec keywordRecord =
				keywordSet.getKeywords ().get (keyword);

			if (keywordRecord != null) {

				return Optional.of (
					Pair.of (
						keywordRecord,
						match.rest ()));

			}

		}

		return Optional.absent ();

	}

	Optional<Pair<KeywordRec,String>> matchNumTo () {

		KeywordRec keywordRecord =
			keywordSet.getKeywords ().get (
				message.getNumTo ());

		if (keywordRecord == null)
			return Optional.absent ();

		return Optional.of (
			Pair.of (
				keywordRecord,
				rest));

	}

	Optional <InboxAttemptRec> tryKeywordSetFallback (
			@NonNull TaskLogger taskLogger) {

		Transaction transaction =
			database.currentTransaction ();

		// find fallback

		KeywordSetFallbackRec keywordSetFallback =
			keywordSetFallbackHelper.find (
				keywordSet,
				message.getNumber ());

		if (keywordSetFallback == null)
			return Optional.absent ();

		// check age

		if (keywordSet.getFallbackTimeout () != null) {

			Instant maxAge =
				transaction.now ().minus (
					Duration.standardSeconds (
						keywordSet.getFallbackTimeout ()));

			Instant timestamp =
				keywordSetFallback.getTimestamp ();

			if (timestamp.isBefore (maxAge))
				return Optional.absent ();

		}

		// debug

		taskLogger.debugFormat (
			"Using keyword set fallback %s ",
			integerToDecimalString (
				keywordSetFallback.getId ()),
			"for message %s",
			integerToDecimalString (inbox.getId ()));

		// chain fallback command

		CommandRec nextCommand =
			keywordSetFallback.getCommand ();

		return Optional.of (
			commandManager.handle (
				taskLogger,
				inbox,
				nextCommand,
				optionalAbsent (),
				rest));

	}

}
