package wbs.sms.magicnumber.daemon;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceRec;
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

	// dependencies

	@Inject
	Database database;

	@Inject
	SmsInboxLogic smsInboxLogic;

	@Inject
	MagicNumberObjectHelper magicNumberHelper;

	@Inject
	MagicNumberUseObjectHelper magicNumberUseHelper;

	@Inject
	MessageObjectHelper messageHelper;

	// indirect dependencies

	@Inject
	Provider<CommandManager> commandManagerProvider;

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
	InboxAttemptRec handle () {

		MessageRec message =
			inbox.getMessage ();

		// lookup the MagicNumber

		MagicNumberRec magicNumber =
			magicNumberHelper.findByNumber (
				message.getNumTo ());

		if (magicNumber == null) {

			return smsInboxLogic.inboxNotProcessed (
				inbox,
				Optional.<ServiceRec>absent (),
				Optional.<AffiliateRec>absent (),
				Optional.of (command),
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
				inbox,
				Optional.<ServiceRec>absent (),
				Optional.<AffiliateRec>absent (),
				Optional.of (command),
				"Magic number has not been used");

		}

		// and delegate

		return commandManagerProvider.get ().handle (
			inbox,
			magicNumberUse.getCommand (),
			Optional.of (
				magicNumberUse.getRefId ()),
			rest);

	}

}
