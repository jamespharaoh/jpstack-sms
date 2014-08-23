package wbs.sms.customer.logic;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.customer.model.SmsCustomerManagerRec;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerTemplateObjectHelper;
import wbs.sms.customer.model.SmsCustomerTemplateRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.MessageSender;

import com.google.common.base.Optional;

@SingletonComponent ("smsCustomerLogic")
public
class SmsCustomerLogicImpl
	implements SmsCustomerLogic {

	// dependencies

	@Inject
	SmsCustomerTemplateObjectHelper smsCustomerTemplateHelper;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSenderProvider;

	// implementation

	public
	void newCustomer (
			SmsCustomerRec customer,
			Optional<Integer> threadId) {

		sendWelcomeMessage (
			customer,
			threadId);

		sendWarningMessage (
			customer,
			threadId);

	}

	void sendWelcomeMessage (
			SmsCustomerRec customer,
			Optional<Integer> threadId) {

		if (customer.getWelcomeMessage () != null)
			return;

		SmsCustomerManagerRec manager =
			customer.getSmsCustomerManager ();

		SmsCustomerTemplateRec template =
			smsCustomerTemplateHelper.findByCode (
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

		customer

			.setWelcomeMessage (
				message);

		return;

	}

	void sendWarningMessage (
			SmsCustomerRec customer,
			Optional<Integer> threadId) {

		if (customer.getWarningMessage () != null)
			return;

		SmsCustomerManagerRec manager =
			customer.getSmsCustomerManager ();

		SmsCustomerTemplateRec template =
			smsCustomerTemplateHelper.findByCode (
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

		customer

			.setWarningMessage (
				message);

		return;

	}

}
