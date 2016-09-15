package wbs.sms.customer.logic;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.event.logic.EventLogic;
import wbs.sms.customer.model.SmsCustomerAffiliateRec;
import wbs.sms.customer.model.SmsCustomerManagerRec;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSessionObjectHelper;
import wbs.sms.customer.model.SmsCustomerSessionRec;
import wbs.sms.customer.model.SmsCustomerTemplateObjectHelper;
import wbs.sms.customer.model.SmsCustomerTemplateRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;

@Log4j
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
			@NonNull SmsCustomerSessionRec session,
			@NonNull Optional<Long> threadId) {

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

			.send ();

		session

			.setWelcomeMessage (
				message);

		return;

	}

	void sendWarningMessage (
			@NonNull SmsCustomerSessionRec session,
			@NonNull Optional<Long> threadId) {

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
			@NonNull SmsCustomerRec customer,
			@NonNull SmsCustomerAffiliateRec affiliate,
			@NonNull MessageRec message) {

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
			"sms_customer_affiliate_update_message",
			customer,
			affiliate,
			message);

	}

}
