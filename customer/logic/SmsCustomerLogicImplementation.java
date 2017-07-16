package wbs.sms.customer.logic;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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
	ComponentProvider <SmsMessageSender> messageSenderProvider;

	// implementation

	@Override
	public
	void sessionStart (
			@NonNull Transaction parentTransaction,
			@NonNull SmsCustomerRec customer,
			@NonNull Optional <Long> threadId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sessionStart");

		) {

			sessionTimeoutAuto (
				transaction,
				customer);

			if (customer.getActiveSession () != null) {

				customer

					.setLastActionTime (
						transaction.now ());

				return;

			}

			SmsCustomerSessionRec newSession =
				smsCustomerSessionHelper.insert (
					transaction,
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
				transaction,
				newSession,
				threadId);

			sendWarningMessage (
				transaction,
				newSession,
				threadId);

		}

	}

	private
	void sendWelcomeMessage (
			@NonNull Transaction parentTransaction,
			@NonNull SmsCustomerSessionRec session,
			@NonNull Optional <Long> threadId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendWelcomeMessage");

		) {

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
					transaction,
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
				messageSenderProvider.provide (
					transaction)

				.threadId (
					threadId.orNull ())

				.number (
					customer.getNumber ())

				.messageText (
					template.getText ())

				.numFrom (
					template.getNumber ())

				.routerResolve (
					transaction,
					template.getRouter ())

				.serviceLookup (
					transaction,
					manager,
					"welcome")

				.affiliate (
					optionalOrNull (
						customerAffiliate (
							transaction,
							customer)))

				.send (
					transaction);

			session

				.setWelcomeMessage (
					message);

			return;

		}

	}

	void sendWarningMessage (
			@NonNull Transaction parentTransaction,
			@NonNull SmsCustomerSessionRec session,
			@NonNull Optional<Long> threadId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendWarningMessage");

		) {

			SmsCustomerRec customer =
				session.getCustomer ();

			SmsCustomerManagerRec manager =
				customer.getSmsCustomerManager ();

			Optional <SmsCustomerTemplateRec> templateOptional =
				smsCustomerTemplateHelper.findByCode (
					transaction,
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
				messageSenderProvider.provide (
					transaction)

				.threadId (
					threadId.orNull ())

				.number (
					customer.getNumber ())

				.messageText (
					template.getText ())

				.numFrom (
					template.getNumber ())

				.routerResolve (
					transaction,
					template.getRouter ())

				.serviceLookup (
					transaction,
					manager,
					"warning")

				.affiliate (
					optionalOrNull (
						customerAffiliate (
							transaction,
							customer)))

				.send (
					transaction);

			session

				.setWarningMessage (
					message);

			return;

		}

	}

	@Override
	public
	void sessionEndManually (
			@NonNull Transaction parentTransaction,
			@NonNull UserRec user,
			@NonNull SmsCustomerRec customer,
			@NonNull String reason) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sessionEndManually");

		) {

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
				transaction,
				"sms_customer_session_end_manually",
				user,
				customer,
				reason);

		}

	}

	public
	void sessionTimeoutAuto (
			@NonNull Transaction parentTransaction,
			@NonNull SmsCustomerRec customer) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sessionTimeoutAuto");

		) {

			if (customer.getActiveSession () == null)
				return;

			sessionTimeoutAuto (
				transaction,
				customer.getActiveSession ());

		}

	}

	@Override
	public
	void sessionTimeoutAuto (
			@NonNull Transaction parentTransaction,
			@NonNull SmsCustomerSessionRec session) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sessionTimeoutAuto");

		) {

			transaction.debugFormat (
				"Automatic session timeout for %s",
				integerToDecimalString (
					session.getId ()));

			SmsCustomerRec customer =
				session.getCustomer ();

			SmsCustomerManagerRec manager =
				customer.getSmsCustomerManager ();

			if (session.getEndTime () != null) {

				transaction.debugFormat (
					"Not timing out session %s ",
					integerToDecimalString (
						session.getId ()),
					"since it has already ended");

				return;

			}

			if (manager.getSessionTimeout () == null) {

				transaction.debugFormat (
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

				transaction.debugFormat (
					"Not timing out session %s, ",
					integerToDecimalString (
						session.getId ()),
					"which started at %s, ",
					session.getStartTime ().toString (),
					"since that is not before %s",
					startTimeBefore.toString ());

				return;

			}

			transaction.warningFormat (
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

	}

	@Override
	public
	Optional <AffiliateRec> customerAffiliate (
			@NonNull Transaction parentTransaction,
			@NonNull SmsCustomerRec customer) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"customerAffiliate");

		) {

			return ifThenElse (
				isNotNull (
					customer.getSmsCustomerAffiliate ()),

				() -> optionalOf (
					affiliateHelper.findByCodeRequired (
						transaction,
						customer.getSmsCustomerAffiliate (),
						"default")),

				() -> optionalAbsent ()

			);

		}

	}

	@Override
	public
	void customerAffiliateUpdate (
			@NonNull Transaction parentTransaction,
			@NonNull SmsCustomerRec customer,
			@NonNull SmsCustomerAffiliateRec affiliate,
			@NonNull MessageRec message) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"customerAffiliateUpdate");

		) {

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
				transaction,
				"sms_customer_affiliate_update_message",
				customer,
				affiliate,
				message);

		}

	}

}
