package wbs.sms.messageset.logic;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
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
	Provider <SmsMessageSender> messageSender;

	// implementation

	@Override
	public
	Long sendMessageSet (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageSetRec messageSet,
			Long threadId,
			@NonNull NumberRec number,
			ServiceRec service,
			AffiliateRec affiliate) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"sendMessageSet");

		) {

			for (
				MessageSetMessageRec messageSetMessage
					: messageSet.getMessages ()
			) {

				MessageRec message =
					messageSender.get ()

					.threadId (
						threadId)

					.number (
						number)

					.messageString (
						taskLogger,
						messageSetMessage.getMessage ())

					.numFrom (
						messageSetMessage.getNumber ())

					.route (
						messageSetMessage.getRoute ())

					.service (
						service)

					.affiliate (
						affiliate)

					.send (
						taskLogger);

				if (threadId == null) {

					threadId =
						message.getId ();

				}

			}

			return threadId;

		}

	}

}
