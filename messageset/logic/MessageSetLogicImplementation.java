package wbs.sms.messageset.logic;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceRec;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.sms.messageset.model.MessageSetMessageRec;
import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("messageSetLogic")
public
class MessageSetLogicImplementation
	implements MessageSetLogic {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <SmsMessageSender> messageSender;

	// implementation

	@Override
	public
	Long sendMessageSet (
			@NonNull Transaction parentTransaction,
			@NonNull MessageSetRec messageSet,
			@NonNull Optional <Long> providedThreadId,
			@NonNull NumberRec number,
			@NonNull ServiceRec service,
			@NonNull Optional <AffiliateRec> affiliate) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendMessageSet");

		) {

			Optional <Long> actualThreadId =
				providedThreadId;

			for (
				MessageSetMessageRec messageSetMessage
					: messageSet.getMessages ()
			) {

				MessageRec message =
					messageSender.provide (
						transaction)

					.threadId (
						optionalOrNull (
							actualThreadId))

					.number (
						number)

					.messageString (
						transaction,
						messageSetMessage.getMessage ())

					.numFrom (
						messageSetMessage.getNumber ())

					.route (
						messageSetMessage.getRoute ())

					.service (
						service)

					.affiliate (
						optionalOrNull (
							affiliate))

					.send (
						transaction);

				if (
					optionalIsNotPresent (
						actualThreadId)
				) {

					actualThreadId =
						optionalOf (
							message.getId ());

				}

			}

			return optionalGetRequired (
				actualThreadId);

		}

	}

}
