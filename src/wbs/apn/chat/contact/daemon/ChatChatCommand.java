package wbs.apn.chat.contact.daemon;

import static wbs.sms.gsm.GsmUtils.gsmStringSimplifyAllowNonGsm;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.model.ChatBlockObjectHelper;
import wbs.apn.chat.contact.model.ChatBlockRec;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.keyword.model.ChatKeywordObjectHelper;
import wbs.apn.chat.keyword.model.ChatKeywordRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.join.daemon.ChatJoiner;
import wbs.apn.chat.user.join.daemon.ChatJoiner.JoinType;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.CommandManager;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.outbox.model.FailedMessageObjectHelper;

@Accessors (fluent = true)
@Log4j
@PrototypeComponent ("chatChatCommand")
public
class ChatChatCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	ChatBlockObjectHelper chatBlockHelper;

	@SingletonDependency
	ChatKeywordObjectHelper chatKeywordHelper;

	@SingletonDependency
	ChatMessageLogic chatMessageLogic;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatUserDao chatUserDao;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	FailedMessageObjectHelper failedMessageHelper;

	@SingletonDependency
	KeywordFinder keywordFinder;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// indirect dependencies

	@WeakSingletonDependency
	CommandManager commandManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ChatJoiner> chatJoinerProvider;

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

	ChatRec chat;
	MessageRec message;
	ChatUserRec fromChatUser;
	AffiliateRec affiliate;
	ChatUserRec toChatUser;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat.chat"
		};

	}

	// implementation

	InboxAttemptRec doBlock () {

		Transaction transaction =
			database.currentTransaction ();

		ServiceRec defaultService =
			serviceHelper.findByCodeRequired (
				chat,
				"default");

		// create the chatblock, if it doesn't already exist

		ChatBlockRec chatBlock =
			chatBlockHelper.find (
				fromChatUser,
				toChatUser);

		if (chatBlock == null) {

			chatBlockHelper.insert (
				chatBlockHelper.createInstance ()

				.setChatUser (
					fromChatUser)

				.setBlockedChatUser (
					toChatUser)

				.setTimestamp (
					transaction.now ())

			);

		}

		// send message through magic number

		TextRec text =
			textHelper.findOrCreate (
				stringFormat (
					"User %s has now been blocked",
					toChatUser.getCode ()));

		CommandRec magicCommand =
			commandHelper.findByCodeRequired (
				chat,
				"magic");

		CommandRec helpCommand =
			commandHelper.findByCodeRequired (
				chat,
				"help");

		ServiceRec systemService =
			serviceHelper.findByCodeRequired (
				chat,
				"system");

		chatSendLogic.sendMessageMagic (
			fromChatUser,
			Optional.of (
				message.getThreadId ()),
			text,
			magicCommand,
			systemService,
			helpCommand.getId ());

		// process inbox

		return smsInboxLogic.inboxProcessed (
			inbox,
			Optional.of (
				defaultService),
			Optional.of (
				affiliate),
			command);

	}

	InboxAttemptRec doInfo () {

		// TODO why is there no code here?

		throw new RuntimeException ("TODO");

	}

	InboxAttemptRec doChat () {

		ServiceRec defaultService =
			serviceHelper.findByCodeRequired (
				chat,
				"default");

		if (toChatUser == null) {

			log.warn (
				stringFormat (
					"Message %s ",
					integerToDecimalString (
						inbox.getId ()),
					"ignored as recipient user id %s ",
					integerToDecimalString (
						commandRef.get ()),
					"does not exist"));

			return smsInboxLogic.inboxProcessed (
				inbox,
				Optional.of (
					defaultService),
				Optional.of (
					affiliate),
				command);

		}

		// process inbox

		String rejectedReason =
			chatMessageLogic.chatMessageSendFromUser (
				fromChatUser,
				toChatUser,
				rest,
				optionalOf (
					message.getThreadId ()),
				ChatMessageMethod.sms,
				ImmutableList.of ());

		if (rejectedReason != null) {

			failedMessageHelper.insert (
				failedMessageHelper.createInstance ()

				.setMessage (
					message)

				.setError (
					rejectedReason));

		}

		// do auto join

		if (chat.getAutoJoinOnSend ()) {

			chatMiscLogic.userAutoJoin (
				fromChatUser,
				message,
				true);

		}

		// process inbox

		return smsInboxLogic.inboxProcessed (
			inbox,
			Optional.of (
				defaultService),
			Optional.of (
				affiliate),
			command);

	}

	Optional <InboxAttemptRec> checkKeyword (
			@NonNull String keyword,
			@NonNull String rest) {

		Optional <ChatKeywordRec> chatKeywordOptional =
			chatKeywordHelper.findByCode (
				chat,
				gsmStringSimplifyAllowNonGsm (
					keyword));

		if (
			optionalIsNotPresent (
				chatKeywordOptional)
		) {
			return optionalAbsent ();
		}

		ChatKeywordRec chatKeyword =
			chatKeywordOptional.get ();

		if (chatKeyword.getChatBlock ()) {

			return optionalOf (
				doBlock ());

		}

		if (chatKeyword.getChatInfo ()) {

			return optionalOf (
				doInfo ());

		}

		if (
			chatKeyword.getGlobal ()
			&& chatKeyword.getCommand () != null
		) {

			return optionalOf (
				commandManager.handle (
					inbox,
					chatKeyword.getCommand (),
					optionalAbsent (),
					rest));

		}

		return optionalAbsent ();

	}

	Optional <InboxAttemptRec> tryJoin () {

		if (
			isNotNull (
				chatUserLogic.getAffiliateId (
					fromChatUser))
		) {
			return optionalAbsent ();
		}

		// TODO the scheme is set randomly here

		// TODO how does this code even get invoked? if a user has not
		// joined then how can they be replying to a magic number from a
		// user. maybe from a broadcast, but that is all a bit fucked up.

		log.warn (
			stringFormat (
				"Chat request from unjoined user %s",
				integerToDecimalString (
					fromChatUser.getId ())));

		ChatSchemeRec chatScheme =
			chat.getChatSchemes ().iterator ().next ();

		return Optional.of (
			chatJoinerProvider.get ()

			.chatId (
				chat.getId ())

			.joinType (
				JoinType.chatSimple)

			.chatSchemeId (
				chatScheme.getId ())

			.inbox (
				inbox)

			.rest (
				rest)

			.handleInbox (
				command)

		);

	}

	@Override
	public
	InboxAttemptRec handle () {

		chat =
			chatHelper.findRequired (
				command.getParentId ());

		message =
			inbox.getMessage ();

		fromChatUser =
			chatUserHelper.findOrCreate (
				chat,
				message);

		affiliate =
			chatUserLogic.getAffiliate (
				fromChatUser);

		toChatUser =
			chatUserHelper.findRequired (
				commandRef.get ());

		// treat as join if the user has no affiliate

		Optional<InboxAttemptRec> joinInboxAttempt =
			tryJoin ();

		if (joinInboxAttempt.isPresent ())
			return joinInboxAttempt.get ();

		// look for keywords to interpret

		for (
			KeywordFinder.Match match
				: keywordFinder.find (
					rest)
		) {

			if (! match.rest ().isEmpty ())
				continue;

			Optional<InboxAttemptRec> keywordInboxAttempt =
				checkKeyword (
					match.simpleKeyword (),
					"");

			if (keywordInboxAttempt.isPresent ())
				return keywordInboxAttempt.get ();

		}

		// send the message to the other user

		return doChat ();

	}

}