package wbs.sms.customer.daemon;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.customer.logic.SmsCustomerLogic;
import wbs.sms.customer.model.SmsCustomerManagerRec;
import wbs.sms.customer.model.SmsCustomerObjectHelper;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSessionRec;
import wbs.sms.customer.model.SmsCustomerTemplateObjectHelper;
import wbs.sms.customer.model.SmsCustomerTemplateRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.sms.number.list.logic.NumberListLogic;

@Accessors (fluent = true)
@PrototypeComponent ("smsCustomerStopCommand")
public
class SmsCustomerStopCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	NumberListLogic numberListLogic;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	SmsCustomerLogic smsCustomerLogic;

	@SingletonDependency
	SmsCustomerObjectHelper smsCustomerHelper;

	@SingletonDependency
	SmsCustomerTemplateObjectHelper smsCustomerTemplateHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSenderProvider;

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
			"sms_customer_manager.stop",
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle (
			@NonNull TaskLogger parentTaskLogger) {

		Transaction transaction =
			database.currentTransaction ();

		MessageRec inboundMessage =
			inbox.getMessage ();

		SmsCustomerManagerRec customerManager =
			genericCastUnchecked (
				objectManager.getParentRequired (
					command));

		SmsCustomerRec customer =
			smsCustomerHelper.findOrCreate (
				customerManager,
				inboundMessage.getNumber ());

		ServiceRec stopService =
			serviceHelper.findByCodeRequired (
				customerManager,
				"stop");

		// send stop message

		SmsCustomerTemplateRec stopTemplate =
			smsCustomerTemplateHelper.findByCodeRequired (
				customerManager,
				"stop");

		MessageRec outboundMessage;

		if (stopTemplate == null) {

			outboundMessage = null;

		} else {

			outboundMessage =
				messageSenderProvider.get ()

				.threadId (
					inboundMessage.getThreadId ())

				.number (
					customer.getNumber ())

				.messageText (
					stopTemplate.getText ())

				.numFrom (
					stopTemplate.getNumber ())

				.routerResolve (
					stopTemplate.getRouter ())

				.service (
					stopService)

				.affiliate (
					optionalOrNull (
						smsCustomerLogic.customerAffiliate (
							customer)))

				.send ();

		}

		// update session

		SmsCustomerSessionRec activeSession =
			customer.getActiveSession ();

		if (activeSession != null) {

			activeSession

				.setEndTime (
					transaction.now ())

				.setStopMessage (
					outboundMessage);

		}

		customer

			.setLastActionTime (
				transaction.now ())

			.setActiveSession (
				null);

		// add to number list

		if (
			isNotNull (
				customerManager.getStopNumberList ())
		) {

			numberListLogic.addDueToMessage (
				customerManager.getStopNumberList (),
				inboundMessage.getNumber (),
				inboundMessage,
				stopService);

		}

		// process message

		return smsInboxLogic.inboxProcessed (
			inbox,
			Optional.of (
				stopService),
			smsCustomerLogic.customerAffiliate (
				customer),
			command);

	}

}
