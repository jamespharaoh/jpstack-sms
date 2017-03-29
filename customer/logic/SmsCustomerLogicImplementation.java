package wbs.sms.customer.logic;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.model.UserRec;

import wbs.sms.customer.model.SmsCustomerAffiliateRec;
import wbs.sms.customer.model.SmsCustomerManagerRec;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSessionObjectHelper;
import wbs.sms.customer.model.SmsCustomerSessionRec;
import wbs.sms.customer.model.SmsCustomerTemplateObjectHelper;
import wbs.sms.customer.model.SmsCustomerTemplateRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;

@SingletonComponent ("smsCustomerLogic")
public
class SmsCustomerLogicImplementation
	implements SmsCustomerLogic {

	// singleton dependencies

	@SingletonDependency
	AffiliateObjectHelper affiliateHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SmsCustomerSessionObjectHelper smsCustomerSessionHelper;

	@SingletonDependency
	SmsCustomerTemplateObjectHelper smsCustomerTemplateHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSenderProvider;

	// implementation

	@Override
	public
	void sessionStart (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SmsCustomerRec customer,
			@NonNull Optional<Long> threadId) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sessionStart");

		Transaction transaction =
			database.currentTransaction ();

		sessionTimeoutAuto (
			taskLogger,
			customer);

		if (customer.getActiveSession () != null) {

			customer

				.setLastActionTime (
					transaction.now ());

			return;

		}

		SmsCustomerSessionRec newSession =
			smsCustomerSessionHelper.insert (
				taskLogger,
				smsCustomerSessionHelper.createInstance ()

			.setCustomer (
				customer)

			.setIndex (
				customer.getNumSessions ())

			.setStartTime (
				transaction.now ())

		);

		customer

			.setActiveSession (
				newSession)

			.setLastActionTime (
				transaction.now ())

			.setNumSessions (
				customer.getNumSessions () + 1);

		sendWelcomeMessage (
			taskLogger,
			newSession,
			threadId);

		sendWarningMessage (
			taskLogger,
			newSession,
			threadId);

	}

	void sendWelcomeMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SmsCustomerSessionRec session,
			@NonNull Optional <Long> threadId) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendWelcomeMessage");

		// only send once per session

		if (
			isNotNull (
				session.getWelcomeMessage ())
		) {
			return;
		}

		// only send if welcome template is defined

		SmsCustomerRec customer =
			session.getCustomer ();

		SmsCustomerManagerRec manager =
			customer.getSmsCustomerManager ();

		Optional<SmsCustomerTemplateRec> templateOptional =
			smsCustomerTemplateHelper.findByCode (
				manager,
				"welcome");

		if (
			optionalIsNotPresent (
				templateOptional)
		) {
			return;
		}

		SmsCustomerTemplateRec template =
			templateOptional.get ();

		// send message

		MessageRec message =
			messageSenderProvider.get ()

			.threadId (
				threadId.orNull ())

			.number (
				customer.getNumber ())

			.messageText (
				template.getText ())

			.numFrom (
				template.getNumber ())

			.routerResolve (
				template.getRouter ())

			.serviceLookup (
				manager,
				"welcome")

			.affiliate (
				optionalOrNull (
					customerAffiliate (
						customer)))

			.send (
				taskLogger);

		session

			.setWelcomeMessage (
				message);

		return;

	}

	void sendWarningMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SmsCustomerSessionRec session,
			@NonNull Optional<Long> threadId) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendWarningMessage");

		SmsCustomerRec customer =
			session.getCustomer ();

		SmsCustomerManagerRec manager =
			customer.getSmsCustomerManager ();

		Optional<SmsCustomerTemplateRec> templateOptional =
			smsCustomerTemplateHelper.findByCode (
				manager,
				"warning");

		if (
			optionalIsNotPresent (
				templateOptional)
		) {
			return;
		}

		SmsCustomerTemplateRec template =
			templateOptional.get ();

		MessageRec message =
			messageSenderProvider.get ()

			.threadId (
				threadId.orNull ())

			.number (
				customer.getNumber ())

			.messageText (
				template.getText ())

			.numFrom (
				template.getNumber ())

			.routerResolve (
				template.getRouter ())

			.serviceLookup (
				manager,
				"warning")

			.affiliate (
				optionalOrNull (
					customerAffiliate (
						customer)))

			.send (
				taskLogger);

		session

			.setWarningMessage (
				message);

		return;

	}

	@Override
	public
	void sessionEndManually (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UserRec user,
			@NonNull SmsCustomerRec customer,
			@NonNull String reason) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sessionEndManually");

		Transaction transaction =
			database.currentTransaction ();

		if (
			isNull (
				customer.getActiveSession ())
		) {
			throw new IllegalStateException ();
		}

		SmsCustomerSessionRec session =
			customer.getActiveSession ();

		session

			.setEndTime (
				transaction.now ())

		;

		customer

			.setActiveSession (
				null)

		;

		eventLogic.createEvent (
			taskLogger,
			"sms_customer_session_end_manually",
			user,
			customer,
			reason);

	}

	public
	void sessionTimeoutAuto (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SmsCustomerRec customer) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sessionTimeoutAuto");

		if (customer.getActiveSession () == null)
			return;

		sessionTimeoutAuto (
			taskLogger,
			customer.getActiveSession ());

	}

	@Override
	public
	void sessionTimeoutAuto (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SmsCustomerSessionRec session) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sessionTimeoutAuto");

		taskLogger.debugFormat (
			"Automatic session timeout for %s",
			integerToDecimalString (
				session.getId ()));

		Transaction transaction =
			database.currentTransaction ();

		SmsCustomerRec customer =
			session.getCustomer ();

		SmsCustomerManagerRec manager =
			customer.getSmsCustomerManager ();

		if (session.getEndTime () != null) {

			taskLogger.debugFormat (
				"Not timing out session %s ",
				integerToDecimalString (
					session.getId ()),
				"since it has already ended");

			return;

		}

		if (manager.getSessionTimeout () == null) {

			taskLogger.debugFormat (
				"Not timing out session %s ",
				integerToDecimalString (
					session.getId ()),
				"since manager %s ",
				integerToDecimalString (
					manager.getId ()),
				"has no timeout configured");

			return;

		}

		Instant startTimeBefore =
			transaction.now ().minus (
				Duration.standardSeconds (
					manager.getSessionTimeout ()));

		if (! session.getStartTime ().isBefore (
				startTimeBefore)) {

			taskLogger.debugFormat (
				"Not timing out session %s, ",
				integerToDecimalString (
					session.getId ()),
				"which started at %s, ",
				session.getStartTime ().toString (),
				"since that is not before %s",
				startTimeBefore.toString ());

			return;

		}

		taskLogger.warningFormat (
			"Timing out sms customer session %s",
			integerToDecimalString (
				session.getId ()));

		session

			.setEndTime (
				transaction.now ())

		;

		customer

			.setActiveSession (
				null)

		;

	}

	@Override
	public
	Optional <AffiliateRec> customerAffiliate (
			@NonNull SmsCustomerRec customer) {

		return ifThenElse (
			isNotNull (
				customer.getSmsCustomerAffiliate ()),

			() -> Optional.of (
				affiliateHelper.findByCodeRequired (
					customer.getSmsCustomerAffiliate (),
					"default")),

			() -> Optional.absent ()

		);

	}

	@Override
	public
	void customerAffiliateUpdate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SmsCustomerRec customer,
			@NonNull SmsCustomerAffiliateRec affiliate,
			@NonNull MessageRec message) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"customerAffiliateUpdate");

		if (
			isNotNull (
				customer.getSmsCustomerAffiliate ())
		) {
			return;
		}

		customer

			.setSmsCustomerAffiliate (
				affiliate);

		eventLogic.createEvent (
			taskLogger,
			"sms_customer_affiliate_update_message",
			customer,
			affiliate,
			message);

	}

}
