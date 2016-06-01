package wbs.sms.customer.logic;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.sms.customer.model.SmsCustomerManagerRec;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSessionObjectHelper;
import wbs.sms.customer.model.SmsCustomerSessionRec;
import wbs.sms.customer.model.SmsCustomerTemplateObjectHelper;
import wbs.sms.customer.model.SmsCustomerTemplateRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.MessageSender;

@Log4j
@SingletonComponent ("smsCustomerLogic")
public
class SmsCustomerLogicImplementation
	implements SmsCustomerLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	SmsCustomerSessionObjectHelper smsCustomerSessionHelper;

	@Inject
	SmsCustomerTemplateObjectHelper smsCustomerTemplateHelper;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSenderProvider;

	// implementation

	@Override
	public
	void sessionStart (
			@NonNull SmsCustomerRec customer,
			@NonNull Optional<Long> threadId) {

		Transaction transaction =
			database.currentTransaction ();

		sessionTimeoutAuto (
			customer);

		if (customer.getActiveSession () != null) {

			customer

				.setLastActionTime (
					transaction.now ());

			return;

		}

		SmsCustomerSessionRec newSession =
			smsCustomerSessionHelper.insert (
				smsCustomerSessionHelper.createInstance ()

			.setCustomer (
				customer)

			.setIndex (
				(int) (long)
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
			newSession,
			threadId);

		sendWarningMessage (
			newSession,
			threadId);

	}

	void sendWelcomeMessage (
			SmsCustomerSessionRec session,
			Optional<Long> threadId) {

		SmsCustomerRec customer =
			session.getCustomer ();

		if (session.getWelcomeMessage () != null)
			return;

		SmsCustomerManagerRec manager =
			customer.getSmsCustomerManager ();

		SmsCustomerTemplateRec template =
			smsCustomerTemplateHelper.findByCodeOrNull (
				manager,
				"welcome");

		if (template == null)
			return;

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

			.send ();

		session

			.setWelcomeMessage (
				message);

		return;

	}

	void sendWarningMessage (
			SmsCustomerSessionRec session,
			Optional<Long> threadId) {

		SmsCustomerRec customer =
			session.getCustomer ();

		SmsCustomerManagerRec manager =
			customer.getSmsCustomerManager ();

		SmsCustomerTemplateRec template =
			smsCustomerTemplateHelper.findByCodeOrNull (
				manager,
				"warning");

		if (template == null)
			return;

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

			.send ();

		session

			.setWarningMessage (
				message);

		return;

	}

	public
	void sessionTimeoutAuto (
			@NonNull SmsCustomerRec customer) {

		if (customer.getActiveSession () == null)
			return;

		sessionTimeoutAuto (
			customer.getActiveSession ());

	}

	@Override
	public
	void sessionTimeoutAuto (
			@NonNull SmsCustomerSessionRec session) {

		log.debug (
			stringFormat (
				"Automatic session timeout for %s",
				session.getId ()));

		Transaction transaction =
			database.currentTransaction ();

		SmsCustomerRec customer =
			session.getCustomer ();

		SmsCustomerManagerRec manager =
			customer.getSmsCustomerManager ();

		if (session.getEndTime () != null) {

			log.debug (
				stringFormat (
					"Not timing out session %s ",
					session.getId (),
					"since it has already ended"));

			return;

		}

		if (manager.getSessionTimeout () == null) {

			log.debug (
				stringFormat (
					"Not timing out session %s ",
					session.getId (),
					"since manager %s ",
					manager.getId (),
					"has no timeout configured"));

			return;

		}

		Instant startTimeBefore =
			transaction.now ().minus (
				Duration.standardSeconds (
					manager.getSessionTimeout ()));

		if (! session.getStartTime ().isBefore (
				startTimeBefore)) {

			log.debug (
				stringFormat (
					"Not timing out session %s, ",
					session.getId (),
					"which started at %s, ",
					session.getStartTime (),
					"since that is not before %s",
					startTimeBefore));

			return;

		}

		log.warn (
			stringFormat (
				"Timing out sms customer session %s",
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

}
